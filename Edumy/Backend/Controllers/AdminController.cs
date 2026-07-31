using EduMy.Backend.Data;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;

namespace EduMy.Backend.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize(Roles = "Admin")]
    public class AdminController : ControllerBase
    {
        private readonly ApplicationDbContext _context;

        public AdminController(ApplicationDbContext context)
        {
            _context = context;
        }

        [HttpGet("stats")]
        public async Task<IActionResult> GetDashboardStats()
        {
            var totalUsers = await _context.Users.CountAsync();
            var totalCourses = await _context.Courses.CountAsync();
            
            // Calculate total revenue from completed orders
            var totalRevenue = await _context.Orders
                .Where(o => o.Status == "Completed")
                .SumAsync(o => o.TotalAmount);

            return Ok(new
            {
                TotalUsers = totalUsers,
                TotalCourses = totalCourses,
                TotalRevenue = totalRevenue
            });
        }

        [HttpGet("users")]
        public async Task<IActionResult> GetAllUsers()
        {
            var users = await _context.Users
                .Select(u => new { u.UserId, u.FullName, u.Email, u.Role, u.CreatedAt })
                .OrderByDescending(u => u.CreatedAt)
                .ToListAsync();

            return Ok(users);
        }

        [HttpGet("courses")]
        public async Task<IActionResult> GetAllCourses()
        {
            var courses = await _context.Courses
                .Include(c => c.Instructor)
                .Include(c => c.Category)
                .Include(c => c.MlAnalyses)
                .Include(c => c.CourseTags)
                    .ThenInclude(ct => ct.Tag)
                .OrderByDescending(c => c.CreatedAt)
                .ToListAsync();

            return Ok(courses);
        }

        [HttpPut("courses/{id}/status")]
        public async Task<IActionResult> UpdateCourseStatus(int id, [FromBody] string status)
        {
            var course = await _context.Courses.FindAsync(id);
            if (course == null) return NotFound();

            course.Status = status;
            await _context.SaveChangesAsync();

            return Ok(new { message = "Status updated" });
        }

        [HttpPut("users/{id}/toggle-status")]
        public async Task<IActionResult> ToggleUserStatus(int id)
        {
            var user = await _context.Users.FindAsync(id);
            if (user == null) return NotFound();

            user.IsActive = !user.IsActive;
            await _context.SaveChangesAsync();

            return Ok(new { message = $"User status updated to {(user.IsActive ? "Active" : "Blocked")}", isActive = user.IsActive });
        }

        [HttpPut("users/{id}/role")]
        public async Task<IActionResult> UpdateUserRole(int id, [FromBody] string newRole)
        {
            var validRoles = new[] { "Admin", "Instructor", "Student" };
            if (!validRoles.Contains(newRole)) return BadRequest("Invalid role.");

            var user = await _context.Users.FindAsync(id);
            if (user == null) return NotFound();

            user.Role = newRole;
            await _context.SaveChangesAsync();

            return Ok(new { message = $"User role updated to {newRole}" });
        }

        [HttpGet("ml-monitoring")]
        public async Task<IActionResult> GetMlMonitoring()
        {
            var totalAnalyses = await _context.CourseMlAnalyses.CountAsync();
            var highRiskCount = await _context.CourseMlAnalyses.CountAsync(a => a.RiskLevel == "High");
            var pendingReviews = await _context.CourseMlAnalyses.CountAsync(a => a.Status == "NeedsManualReview" || a.Status == "InstructorConfirmationRequired");
            
            var totalReviews = await _context.Reviews.CountAsync();
            var sentimentStats = await _context.Reviews
                .GroupBy(r => r.SentimentLabel)
                .Select(g => new { Label = g.Key, Count = g.Count() })
                .ToListAsync();

            var analysesHistory = await _context.CourseMlAnalyses
                .Include(a => a.Course)
                .OrderByDescending(a => a.CreatedAt)
                .Take(20)
                .Select(a => new {
                    a.Id,
                    a.CourseId,
                    CourseTitle = a.Course.Title,
                    a.PrimaryCategory,
                    a.Confidence,
                    a.QualityScore,
                    a.RiskLevel,
                    a.Status,
                    a.CreatedAt
                })
                .ToListAsync();

            return Ok(new
            {
                TotalAnalyses = totalAnalyses,
                HighRiskCount = highRiskCount,
                PendingReviews = pendingReviews,
                TotalReviews = totalReviews,
                SentimentStats = sentimentStats,
                AnalysesHistory = analysesHistory
            });
        }

        [HttpPost("ml-analyses/{id}/approve")]
        public async Task<IActionResult> ApproveMlAnalysis(int id)
        {
            var analysis = await _context.CourseMlAnalyses
                .Include(a => a.Course)
                .FirstOrDefaultAsync(a => a.Id == id);

            if (analysis == null) return NotFound("Analysis record not found.");

            analysis.Status = "Approved";
            analysis.ApprovedAt = DateTime.UtcNow;

            var userIdStr = User.FindFirstValue(System.Security.Claims.ClaimTypes.NameIdentifier);
            if (int.TryParse(userIdStr, out int adminId))
            {
                analysis.ApprovedByUserId = adminId;
            }

            analysis.Course.Status = "PendingApproval";
            await _context.SaveChangesAsync();

            return Ok(new { message = "ML classification approved." });
        }

        [HttpPost("ml-analyses/{id}/override")]
        public async Task<IActionResult> OverrideMlAnalysis(int id, [FromBody] OverrideMlDto dto)
        {
            var analysis = await _context.CourseMlAnalyses
                .Include(a => a.Course)
                .FirstOrDefaultAsync(a => a.Id == id);

            if (analysis == null) return NotFound("Analysis record not found.");

            analysis.Status = "Overridden";
            analysis.ApprovedAt = DateTime.UtcNow;

            var userIdStr = User.FindFirstValue(System.Security.Claims.ClaimTypes.NameIdentifier);
            if (int.TryParse(userIdStr, out int adminId))
            {
                analysis.ApprovedByUserId = adminId;
            }

            var cat = await _context.Categories.FirstOrDefaultAsync(c => c.Name == dto.CategoryName);
            if (cat != null)
            {
                analysis.Course.CategoryId = cat.CategoryId;
            }

            analysis.Course.Status = "PendingApproval";
            await _context.SaveChangesAsync();

            return Ok(new { message = "ML classification overridden and course updated." });
        }
    }

    public class OverrideMlDto
    {
        public string CategoryName { get; set; } = string.Empty;
    }
}
