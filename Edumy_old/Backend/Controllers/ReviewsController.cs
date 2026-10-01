using EduMy.Backend.Data;
using EduMy.Backend.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;

namespace EduMy.Backend.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class ReviewsController : ControllerBase
    {
        private readonly ApplicationDbContext _context;
        private readonly EduMy.Backend.Services.IMachineLearningService _mlService;

        public ReviewsController(ApplicationDbContext context, EduMy.Backend.Services.IMachineLearningService mlService)
        {
            _context = context;
            _mlService = mlService;
        }

        [HttpGet("course/{courseId}")]
        public async Task<IActionResult> GetCourseReviews(int courseId)
        {
            var reviews = await _context.Reviews
                .Include(r => r.User)
                .Where(r => r.CourseId == courseId)
                .OrderByDescending(r => r.CreatedAt)
                .ToListAsync();

            return Ok(reviews);
        }

        [Authorize(Roles = "Student")]
        [HttpPost("course/{courseId}")]
        public async Task<IActionResult> CreateReview(int courseId, [FromBody] Review review)
        {
            var userIdStr = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (!int.TryParse(userIdStr, out int userId)) return Unauthorized();

            // Check if already enrolled
            var isEnrolled = await _context.Enrollments.AnyAsync(e => e.CourseId == courseId && e.UserId == userId);
            if (!isEnrolled) return BadRequest(new { message = "You must be enrolled to review this course." });

            // ML Integration: Analyze Sentiment
            var sentimentResult = await _mlService.AnalyzeSentimentAsync(review.Comment);
            if (sentimentResult != null)
            {
                review.SentimentLabel = sentimentResult.Label;
                review.SentimentScore = sentimentResult.Score;
            }
            else
            {
                review.SentimentLabel = "Unavailable";
                review.SentimentScore = null;
            }

            review.CourseId = courseId;
            review.UserId = userId;
            review.CreatedAt = DateTime.UtcNow;

            _context.Reviews.Add(review);
            await _context.SaveChangesAsync();

            // Update course average rating
            var averageRating = await _context.Reviews.Where(r => r.CourseId == courseId).AverageAsync(r => r.Rating);
            var course = await _context.Courses.FindAsync(courseId);
            if (course != null)
            {
                course.AverageRating = Math.Round(averageRating, 1);
                await _context.SaveChangesAsync();
            }

            return CreatedAtAction(nameof(GetCourseReviews), new { courseId = review.CourseId }, review);
        }

        [Authorize]
        [HttpPut("{id}")]
        public async Task<IActionResult> UpdateReview(int id, [FromBody] Review review)
        {
            if (id != review.ReviewId) return BadRequest();

            var existing = await _context.Reviews.AsNoTracking().FirstOrDefaultAsync(r => r.ReviewId == id);
            if (existing == null) return NotFound();

            var userIdStr = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (int.TryParse(userIdStr, out int userId) && existing.UserId != userId && !User.IsInRole("Admin"))
            {
                return Forbid();
            }

            // Keep the original created at, course id, user id
            review.UserId = existing.UserId;
            review.CourseId = existing.CourseId;
            review.CreatedAt = existing.CreatedAt;
            
            _context.Entry(review).State = EntityState.Modified;
            
            try
            {
                await _context.SaveChangesAsync();
                
                // Update course average rating
                var averageRating = await _context.Reviews.Where(r => r.CourseId == review.CourseId).AverageAsync(r => r.Rating);
                var course = await _context.Courses.FindAsync(review.CourseId);
                if (course != null)
                {
                    course.AverageRating = Math.Round(averageRating, 1);
                    await _context.SaveChangesAsync();
                }
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!ReviewExists(id)) return NotFound();
                else throw;
            }

            return NoContent();
        }

        [Authorize]
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteReview(int id)
        {
            var review = await _context.Reviews.FindAsync(id);
            if (review == null) return NotFound();

            var userIdStr = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (int.TryParse(userIdStr, out int userId) && review.UserId != userId && !User.IsInRole("Admin"))
            {
                return Forbid();
            }

            int courseId = review.CourseId;

            _context.Reviews.Remove(review);
            await _context.SaveChangesAsync();

            // Update course average rating
            var remainingReviews = await _context.Reviews.Where(r => r.CourseId == courseId).ToListAsync();
            var course = await _context.Courses.FindAsync(courseId);
            if (course != null)
            {
                course.AverageRating = remainingReviews.Any() ? Math.Round(remainingReviews.Average(r => r.Rating), 1) : 0;
                await _context.SaveChangesAsync();
            }

            return NoContent();
        }

        private bool ReviewExists(int id)
        {
            return _context.Reviews.Any(e => e.ReviewId == id);
        }
    }
}
