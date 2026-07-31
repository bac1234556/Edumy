using EduMy.Backend.Data;
using EduMy.Backend.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;

namespace EduMy.Backend.Controllers
{
    [ApiController]
    [Route("api/")]
    public class CurriculumController : ControllerBase
    {
        private readonly ApplicationDbContext _context;

        public CurriculumController(ApplicationDbContext context)
        {
            _context = context;
        }

        // --- PUBLIC: Get Curriculum for a Course ---
        [HttpGet("courses/{courseId}/curriculum")]
        public async Task<IActionResult> GetCurriculum(int courseId)
        {
            var sections = await _context.Set<CourseSection>()
                .Where(s => s.CourseId == courseId)
                .Include(s => s.Lessons.OrderBy(l => l.OrderIndex))
                .Include(s => s.Quizzes)
                .OrderBy(s => s.OrderIndex)
                .ToListAsync();

            return Ok(sections);
        }

        // --- INSTRUCTOR/ADMIN: Manage Sections ---
        [Authorize(Roles = "Instructor,Admin")]
        [HttpPost("courses/{courseId}/sections")]
        public async Task<IActionResult> CreateSection(int courseId, [FromBody] CourseSection section)
        {
            var course = await _context.Courses.FindAsync(courseId);
            if (course == null) return NotFound("Course not found");

            // Verify owner
            var userIdStr = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (int.TryParse(userIdStr, out int userId) && course.InstructorId != userId && !User.IsInRole("Admin"))
                return Forbid();

            section.CourseId = courseId;
            _context.Set<CourseSection>().Add(section);
            await _context.SaveChangesAsync();

            return Ok(section);
        }

        // --- INSTRUCTOR/ADMIN: Manage Lessons ---
        [Authorize(Roles = "Instructor,Admin")]
        [HttpPost("sections/{sectionId}/lessons")]
        public async Task<IActionResult> CreateLesson(int sectionId, [FromBody] Lesson lesson)
        {
            var section = await _context.Set<CourseSection>()
                .Include(s => s.Course)
                .FirstOrDefaultAsync(s => s.SectionId == sectionId);
                
            if (section == null || section.Course == null) return NotFound("Section not found");

            // Verify owner
            var userIdStr = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (int.TryParse(userIdStr, out int userId) && section.Course.InstructorId != userId && !User.IsInRole("Admin"))
                return Forbid();

            lesson.SectionId = sectionId;
            _context.Set<Lesson>().Add(lesson);
            await _context.SaveChangesAsync();

            return Ok(lesson);
        }
    }
}
