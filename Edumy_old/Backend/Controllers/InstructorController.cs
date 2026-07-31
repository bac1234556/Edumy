using EduMy.Backend.Data;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;

namespace EduMy.Backend.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize(Roles = "Instructor,Admin")]
    public class InstructorController : ControllerBase
    {
        private readonly ApplicationDbContext _context;

        public InstructorController(ApplicationDbContext context)
        {
            _context = context;
        }

        [HttpGet("stats")]
        public async Task<IActionResult> GetDashboardStats()
        {
            var userIdStr = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (!int.TryParse(userIdStr, out int instructorId)) return Unauthorized();

            var totalCourses = await _context.Courses
                .Where(c => c.InstructorId == instructorId)
                .CountAsync();

            var totalStudents = await _context.Enrollments
                .Include(e => e.Course)
                .Where(e => e.Course.InstructorId == instructorId)
                .Select(e => e.UserId)
                .Distinct()
                .CountAsync();

            var totalRevenue = await _context.OrderItems
                .Include(oi => oi.Course)
                .Where(oi => oi.Course.InstructorId == instructorId && oi.Order.Status == "Completed")
                .SumAsync(oi => oi.Price);

            var averageRating = await _context.Courses
                .Where(c => c.InstructorId == instructorId && c.AverageRating > 0)
                .AverageAsync(c => (double?)c.AverageRating) ?? 0.0;

            var recentReviews = await _context.Reviews
                .Include(r => r.Course)
                .Include(r => r.User)
                .Where(r => r.Course.InstructorId == instructorId)
                .OrderByDescending(r => r.CreatedAt)
                .Take(5)
                .Select(r => new {
                    r.ReviewId,
                    r.Rating,
                    r.Comment,
                    r.SentimentLabel,
                    CourseTitle = r.Course.Title,
                    StudentName = r.User.FullName,
                    r.CreatedAt
                })
                .ToListAsync();

            // 1. Revenue by Date (last 6 months)
            var revenueByDate = await _context.OrderItems
                .Include(oi => oi.Course)
                .Include(oi => oi.Order)
                .Where(oi => oi.Course.InstructorId == instructorId && oi.Order.Status == "Completed" && oi.Order.CreatedAt >= DateTime.UtcNow.AddMonths(-6))
                .GroupBy(oi => new { Year = oi.Order.CreatedAt.Year, Month = oi.Order.CreatedAt.Month })
                .Select(g => new {
                    Date = $"{g.Key.Year}-{g.Key.Month:D2}",
                    Revenue = g.Sum(oi => oi.Price)
                })
                .OrderBy(x => x.Date)
                .ToListAsync();

            // 2. Enrollments by Date (last 6 months)
            var enrollmentByDate = await _context.Enrollments
                .Include(e => e.Course)
                .Where(e => e.Course.InstructorId == instructorId && e.EnrolledAt >= DateTime.UtcNow.AddMonths(-6))
                .GroupBy(e => new { Year = e.EnrolledAt.Year, Month = e.EnrolledAt.Month })
                .Select(g => new {
                    Date = $"{g.Key.Year}-{g.Key.Month:D2}",
                    Enrollments = g.Count()
                })
                .OrderBy(x => x.Date)
                .ToListAsync();

            // 3. Sentiment breakdown
            var sentimentStats = await _context.Reviews
                .Include(r => r.Course)
                .Where(r => r.Course.InstructorId == instructorId)
                .GroupBy(r => r.SentimentLabel)
                .Select(g => new {
                    Label = g.Key,
                    Count = g.Count()
                })
                .ToListAsync();

            // 4. ML average quality score
            var mlScores = await _context.CourseMlAnalyses
                .Include(ma => ma.Course)
                .Where(ma => ma.Course.InstructorId == instructorId)
                .Select(ma => ma.QualityScore)
                .ToListAsync();
            var averageQualityScore = mlScores.Any() ? mlScores.Average() : 0.0;

            // 5. Actionable improvement recommendations
            var lowQualityCourses = await _context.Courses
                .Include(c => c.MlAnalyses)
                .Where(c => c.InstructorId == instructorId)
                .Select(c => new {
                    c.CourseId,
                    c.Title,
                    QualityScore = c.MlAnalyses.OrderByDescending(ma => ma.CreatedAt).Select(ma => (int?)ma.QualityScore).FirstOrDefault() ?? 100,
                    NeedsReanalysis = c.NeedsReanalysis
                })
                .Where(c => c.QualityScore < 70 || c.NeedsReanalysis)
                .ToListAsync();

            var recommendations = new List<string>();
            foreach (var lq in lowQualityCourses)
            {
                if (lq.NeedsReanalysis)
                {
                    recommendations.Add($"Course '{lq.Title}' has modified content. Trigger 'Analyze content' to update parameters.");
                }
                else if (lq.QualityScore < 70)
                {
                    recommendations.Add($"Course '{lq.Title}' has a low quality score ({lq.QualityScore}%). Try expanding the course description or lessons outcomes.");
                }
            }

            return Ok(new
            {
                totalCourses,
                totalStudents,
                totalRevenue,
                averageRating = Math.Round(averageRating, 1),
                recentReviews,
                revenueByDate,
                enrollmentByDate,
                sentimentStats,
                averageQualityScore = Math.Round(averageQualityScore, 1),
                recommendations
            });
        }
    }
}
