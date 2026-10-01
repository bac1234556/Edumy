using EduMy.Backend.Data;
using EduMy.Backend.DTOs;
using EduMy.Backend.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;

namespace EduMy.Backend.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class CoursesController : ControllerBase
    {
        private readonly ApplicationDbContext _context;
        private readonly EduMy.Backend.Services.IMachineLearningService _mlService;

        public CoursesController(ApplicationDbContext context, EduMy.Backend.Services.IMachineLearningService mlService)
        {
            _context = context;
            _mlService = mlService;
        }

        [HttpGet("recommend")]
        [Authorize(Roles = "Student")]
        public async Task<IActionResult> GetRecommendations()
        {
            var userIdStr = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (!int.TryParse(userIdStr, out int userId)) return Unauthorized();

            var recs = await _mlService.RecommendCoursesAsync(userId);
            if (recs != null && recs.RecommendedCourseIds.Any())
            {
                var recommendedCourses = await _context.Courses
                    .Where(c => recs.RecommendedCourseIds.Contains(c.CourseId))
                    .Include(c => c.Instructor)
                    .ToListAsync();
                
                // Sort to match recommendation order
                var orderedCourses = recs.RecommendedCourseIds
                    .Select(id => recommendedCourses.FirstOrDefault(c => c.CourseId == id))
                    .Where(c => c != null)
                    .ToList();

                return Ok(orderedCourses);
            }

            return Ok(new List<Course>());
        }

        [HttpGet]
        public async Task<IActionResult> GetCourses([FromQuery] CourseQueryDto queryDto)
        {
            var query = _context.Courses.AsQueryable();

            if (!string.IsNullOrEmpty(queryDto.Search))
            {
                query = query.Where(c => c.Title.Contains(queryDto.Search) || (c.Description != null && c.Description.Contains(queryDto.Search)));
            }

            if (queryDto.CategoryId.HasValue)
            {
                query = query.Where(c => c.CategoryId == queryDto.CategoryId.Value);
            }

            if (!string.IsNullOrEmpty(queryDto.Level))
            {
                query = query.Where(c => c.Level == queryDto.Level);
            }

            if (queryDto.MinPrice.HasValue)
            {
                query = query.Where(c => c.Price >= queryDto.MinPrice.Value);
            }

            if (queryDto.MaxPrice.HasValue)
            {
                query = query.Where(c => c.Price <= queryDto.MaxPrice.Value);
            }

            query = queryDto.SortBy?.ToLower() switch
            {
                "price" => queryDto.SortOrder?.ToLower() == "asc" ? query.OrderBy(c => c.Price) : query.OrderByDescending(c => c.Price),
                "rating" => queryDto.SortOrder?.ToLower() == "asc" ? query.OrderBy(c => c.AverageRating) : query.OrderByDescending(c => c.AverageRating),
                "newest" => queryDto.SortOrder?.ToLower() == "asc" ? query.OrderBy(c => c.CreatedAt) : query.OrderByDescending(c => c.CreatedAt),
                _ => query.OrderByDescending(c => c.CreatedAt) // Default sorting
            };

            var totalItems = await query.CountAsync();
            var totalPages = (int)Math.Ceiling(totalItems / (double)queryDto.PageSize);

            var items = await query
                .Skip((queryDto.PageNumber - 1) * queryDto.PageSize)
                .Take(queryDto.PageSize)
                .Include(c => c.Category)
                .Include(c => c.Instructor)
                .ToListAsync();

            var result = new PagedResultDto<Course>
            {
                Items = items,
                PageNumber = queryDto.PageNumber,
                PageSize = queryDto.PageSize,
                TotalItems = totalItems,
                TotalPages = totalPages
            };

            return Ok(result);
        }

        [HttpGet("my-courses")]
        [Authorize(Roles = "Instructor,Admin")]
        public async Task<IActionResult> GetMyCourses()
        {
            var userIdStr = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (!int.TryParse(userIdStr, out int userId)) return Unauthorized();

            var courses = await _context.Courses
                .Where(c => c.InstructorId == userId)
                .Include(c => c.Category)
                .OrderByDescending(c => c.CreatedAt)
                .ToListAsync();

            return Ok(courses);
        }

        [HttpGet("enrolled")]
        [Authorize(Roles = "Student,Instructor,Admin")]
        public async Task<IActionResult> GetEnrolledCourses()
        {
            var userIdStr = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (!int.TryParse(userIdStr, out int userId)) return Unauthorized();

            var enrollments = await _context.Enrollments
                .Where(e => e.UserId == userId)
                .Include(e => e.Course)
                    .ThenInclude(c => c.Instructor)
                .OrderByDescending(e => e.EnrolledAt)
                .ToListAsync();

            var courses = enrollments.Select(e => new {
                e.CourseId,
                e.Course?.Title,
                e.Course?.ThumbnailUrl,
                e.Course?.Instructor?.FullName,
                e.EnrolledAt,
                e.ProgressPercentage
            });

            return Ok(courses);
        }

        [HttpGet("{id}")]
        public async Task<IActionResult> GetCourse(int id)
        {
            var course = await _context.Courses
                .Include(c => c.Category)
                .Include(c => c.Instructor)
                .Include(c => c.Sections)
                    .ThenInclude(s => s.Lessons)
                .Include(c => c.Reviews)
                    .ThenInclude(r => r.User)
                .Include(c => c.CourseTags)
                    .ThenInclude(ct => ct.Tag)
                .FirstOrDefaultAsync(c => c.CourseId == id);

            if (course == null) return NotFound();
            return Ok(course);
        }

        [Authorize(Roles = "Instructor,Admin")]
        [HttpPost]
        public async Task<IActionResult> CreateCourse([FromBody] CreateCourseDto dto)
        {
            var userIdStr = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (!int.TryParse(userIdStr, out int userId)) return Unauthorized();

            var slug = GenerateSlug(dto.Title);

            var course = new Course
            {
                InstructorId = userId,
                Title = dto.Title,
                Description = dto.Description,
                Price = dto.Price,
                Level = dto.Level,
                ThumbnailUrl = dto.ThumbnailUrl,
                Slug = slug,
                Status = "Draft", // Start as Draft instead of Published
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow
            };

            // Set category if explicitly provided
            if (dto.CategoryId.HasValue && dto.CategoryId.Value > 0)
            {
                course.CategoryId = dto.CategoryId.Value;
            }
            else
            {
                // ML Integration: Predict Category if not provided
                var classification = await _mlService.ClassifyCourseAsync(course.Title, course.Description ?? "");
                if (classification != null && !string.IsNullOrEmpty(classification.Category))
                {
                    var cat = await _context.Categories.FirstOrDefaultAsync(c => c.Name == classification.Category);
                    if (cat != null) course.CategoryId = cat.CategoryId;
                }
            }

            _context.Courses.Add(course);
            await _context.SaveChangesAsync();
            return CreatedAtAction(nameof(GetCourse), new { id = course.CourseId }, course);
        }

        [Authorize(Roles = "Instructor,Admin")]
        [HttpPut("{id}")]
        public async Task<IActionResult> UpdateCourse(int id, [FromBody] Course course)
        {
            if (id != course.CourseId) return BadRequest();

            // Verify owner
            var existing = await _context.Courses.AsNoTracking().FirstOrDefaultAsync(c => c.CourseId == id);
            if (existing == null) return NotFound();
            
            var userIdStr = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (int.TryParse(userIdStr, out int userId) && existing.InstructorId != userId && !User.IsInRole("Admin"))
            {
                return Forbid();
            }

            if (existing.Title != course.Title || existing.Description != course.Description)
            {
                course.NeedsReanalysis = true;
            }
            else
            {
                course.NeedsReanalysis = existing.NeedsReanalysis;
            }

            course.UpdatedAt = DateTime.UtcNow;
            _context.Entry(course).State = EntityState.Modified;
            
            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!CourseExists(id)) return NotFound();
                else throw;
            }

            return NoContent();
        }

        [Authorize(Roles = "Instructor,Admin")]
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteCourse(int id)
        {
            var course = await _context.Courses.FindAsync(id);
            if (course == null) return NotFound();

            var userIdStr = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (int.TryParse(userIdStr, out int userId) && course.InstructorId != userId && !User.IsInRole("Admin"))
            {
                return Forbid();
            }

            _context.Courses.Remove(course);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        [Authorize(Roles = "Instructor,Admin")]
        [HttpPost("{id}/status")]
        public async Task<IActionResult> UpdateStatus(int id, [FromBody] string newStatus)
        {
            var validStatuses = new[] { "Draft", "Analyzing", "NeedsReview", "PendingApproval", "Published" };
            if (!validStatuses.Contains(newStatus))
                return BadRequest("Invalid status.");

            var course = await _context.Courses
                .Include(c => c.CourseTags)
                .Include(c => c.MlAnalyses)
                .FirstOrDefaultAsync(c => c.CourseId == id);

            if (course == null) return NotFound();

            var userIdStr = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (int.TryParse(userIdStr, out int userId) && course.InstructorId != userId && !User.IsInRole("Admin"))
            {
                return Forbid();
            }
            
            if (newStatus == "Published" && !User.IsInRole("Admin"))
            {
                return Forbid("Only admins can publish courses.");
            }

            if (newStatus == "Analyzing")
            {
                course.Status = "Analyzing";
                await _context.SaveChangesAsync();

                var classification = await _mlService.ClassifyCourseAsync(course.Title, course.Description ?? "");
                var contentAnalysis = await _mlService.AnalyzeContentAsync(course.Title, course.Description ?? "");

                if (classification != null && contentAnalysis != null)
                {
                    var cat = await _context.Categories.FirstOrDefaultAsync(c => c.Name == classification.Category);
                    if (cat != null) 
                    {
                        course.CategoryId = cat.CategoryId;
                    }

                    var oldTags = await _context.Set<CourseTag>().Where(ct => ct.CourseId == id).ToListAsync();
                    if (oldTags.Any()) _context.Set<CourseTag>().RemoveRange(oldTags);
                    
                    foreach (var tagName in contentAnalysis.Tags)
                    {
                        var tag = await _context.Tags.FirstOrDefaultAsync(t => t.Name == tagName);
                        if (tag == null)
                        {
                            tag = new Tag { Name = tagName };
                            _context.Tags.Add(tag);
                            await _context.SaveChangesAsync();
                        }
                        course.CourseTags.Add(new CourseTag { CourseId = id, TagId = tag.Id });
                    }

                    var isToxic = contentAnalysis.IsToxic || contentAnalysis.ToxicityScore > 0.5;
                    double confidence = classification.Confidence ?? 0.0;

                    string analysisStatus = "AutoApproved";
                    string finalCourseStatus = "PendingApproval";

                    if (isToxic)
                    {
                        analysisStatus = "NeedsManualReview";
                        finalCourseStatus = "NeedsReview";
                    }
                    else if (confidence < 0.65)
                    {
                        analysisStatus = "NeedsManualReview";
                        finalCourseStatus = "NeedsReview";
                    }
                    else if (confidence < 0.85)
                    {
                        analysisStatus = "InstructorConfirmationRequired";
                        finalCourseStatus = "NeedsReview";
                    }

                    var mlAnalysis = new CourseMlAnalysis
                    {
                        CourseId = id,
                        PrimaryCategory = classification.Category,
                        SubCategory = classification.Category + " Sub",
                        SuggestedLevel = "Intermediate",
                        Confidence = confidence,
                        QualityScore = (int)(contentAnalysis.QualityScore * 100),
                        RiskLevel = isToxic ? "High" : "Low",
                        RawResponseJson = System.Text.Json.JsonSerializer.Serialize(new {
                            classification,
                            contentAnalysis
                        }),
                        Status = analysisStatus,
                        CreatedAt = DateTime.UtcNow
                    };
                    course.MlAnalyses.Add(mlAnalysis);

                    course.Status = finalCourseStatus;
                    course.NeedsReanalysis = false;
                    course.UpdatedAt = DateTime.UtcNow;
                    await _context.SaveChangesAsync();

                    return Ok(new { 
                        message = $"Analysis completed. Course set to {finalCourseStatus}.", 
                        categoryId = course.CategoryId,
                        analysis = new {
                            primaryCategory = mlAnalysis.PrimaryCategory,
                            confidence = mlAnalysis.Confidence,
                            qualityScore = mlAnalysis.QualityScore,
                            riskLevel = mlAnalysis.RiskLevel,
                            status = mlAnalysis.Status
                        }
                    });
                }
                else
                {
                    course.Status = "NeedsReview";
                    course.UpdatedAt = DateTime.UtcNow;
                    await _context.SaveChangesAsync();
                    return Ok(new { message = "ML Service unavailable. Set status to NeedsReview for manual moderation." });
                }
            }

            course.Status = newStatus;
            course.UpdatedAt = DateTime.UtcNow;
            await _context.SaveChangesAsync();

            return Ok(new { message = $"Status updated to {newStatus}" });
        }

        private static string GenerateSlug(string title)
        {
            if (string.IsNullOrEmpty(title)) return "";
            
            string[] vietnameseSigns = new string[]
            {
                "aAeEoOuUiIdDyY",
                "áàạảãâấầậẩẫăắằặẳẵ",
                "ÁÀẠẢÃÂẤẦẬẨẪĂẮẰẶẲẴ",
                "éèẹẻẽêếềệểễ",
                "ÉÈẸẺẼÊẾỀỆỂỄ",
                "óòọỏõôốồộổỗơớờợởỡ",
                "ÓÒỌỎÕÔỐỒỘỔỖƠỚỜỢỞỠ",
                "úùụủũưứừựửữ",
                "ÚÙỤỦŨƯỨỪỰỬỮ",
                "íìịỉĩ",
                "ÍÌỊỈĨ",
                "đ",
                "Đ",
                "ýỳỵỷỹ",
                "ÝỲỴỶỸ"
            };

            for (int i = 1; i < vietnameseSigns.Length; i++)
            {
                for (int j = 0; j < vietnameseSigns[i].Length; j++)
                {
                    title = title.Replace(vietnameseSigns[i][j], vietnameseSigns[0][i - 1]);
                }
            }

            var slug = title.ToLower().Trim()
                .Replace(" ", "-")
                .Replace("--", "-");
            slug = System.Text.RegularExpressions.Regex.Replace(slug, @"[^a-z0-9\-]", "");
            slug += "-" + Guid.NewGuid().ToString("N")[..6];
            return slug;
        }

        private bool CourseExists(int id)
        {
            return _context.Courses.Any(e => e.CourseId == id);
        }
        [HttpPost("{courseId}/lessons/{lessonId}/complete")]
        [Authorize(Roles = "Student")]
        public async Task<IActionResult> CompleteLesson(int courseId, int lessonId)
        {
            var userIdStr = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (!int.TryParse(userIdStr, out int userId)) return Unauthorized();

            // Verify enrollment
            var isEnrolled = await _context.Enrollments.AnyAsync(e => e.CourseId == courseId && e.UserId == userId);
            if (!isEnrolled) return Forbid("You must be enrolled to complete lessons.");

            var progress = await _context.LessonProgresses
                .FirstOrDefaultAsync(lp => lp.UserId == userId && lp.CourseId == courseId && lp.LessonId == lessonId);

            if (progress == null)
            {
                _context.LessonProgresses.Add(new LessonProgress
                {
                    UserId = userId,
                    CourseId = courseId,
                    LessonId = lessonId,
                    CompletedAt = DateTime.UtcNow
                });
                await _context.SaveChangesAsync();
                
                // Check if all lessons are completed
                var totalLessons = await _context.Lessons.CountAsync(l => l.Section.CourseId == courseId);
                var completedLessons = await _context.LessonProgresses.CountAsync(lp => lp.UserId == userId && lp.CourseId == courseId);
                
                if (totalLessons > 0 && completedLessons == totalLessons)
                {
                    // Update enrollment progress
                    var enrollment = await _context.Enrollments.FirstOrDefaultAsync(e => e.CourseId == courseId && e.UserId == userId);
                    if (enrollment != null)
                    {
                        enrollment.ProgressPercentage = 100;
                        _context.Entry(enrollment).State = EntityState.Modified;
                    }
                    
                    // Generate Certificate
                    var hasCert = await _context.Certificates.AnyAsync(c => c.UserId == userId && c.CourseId == courseId);
                    if (!hasCert)
                    {
                        var cert = new Certificate
                        {
                            UserId = userId,
                            CourseId = courseId,
                            IssuedAt = DateTime.UtcNow,
                            CertificateUrl = Guid.NewGuid().ToString("N") // Unique cert ID for public URL
                        };
                        _context.Certificates.Add(cert);
                    }
                    await _context.SaveChangesAsync();
                }
            }

            return Ok(new { message = "Lesson completed" });
        }

        [HttpGet("{courseId}/progress")]
        [Authorize(Roles = "Student")]
        public async Task<IActionResult> GetCourseProgress(int courseId)
        {
            var userIdStr = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (!int.TryParse(userIdStr, out int userId)) return Unauthorized();

            var totalLessons = await _context.Lessons
                .Where(l => l.Section.CourseId == courseId)
                .CountAsync();

            var completedLessons = await _context.LessonProgresses
                .Where(lp => lp.UserId == userId && lp.CourseId == courseId)
                .Select(lp => lp.LessonId)
                .ToListAsync();

            double percentage = totalLessons > 0 ? (double)completedLessons.Count / totalLessons * 100 : 0;

            var cert = await _context.Certificates.FirstOrDefaultAsync(c => c.UserId == userId && c.CourseId == courseId);

            return Ok(new { 
                completedLessonIds = completedLessons,
                totalLessons = totalLessons,
                progressPercentage = Math.Round(percentage, 2),
                certificateId = cert?.Id,
                certificateUrl = cert?.CertificateUrl
            });
        }

        [HttpPost("{id}/reviews")]
        [Authorize(Roles = "Student")]
        public async Task<IActionResult> AddReview(int id, [FromBody] CreateReviewDto dto)
        {
            var userIdStr = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (!int.TryParse(userIdStr, out int userId)) return Unauthorized();

            // Verify course exists
            var course = await _context.Courses.Include(c => c.Reviews).FirstOrDefaultAsync(c => c.CourseId == id);
            if (course == null) return NotFound("Course not found");

            // Verify enrollment
            var isEnrolled = await _context.Enrollments.AnyAsync(e => e.CourseId == id && e.UserId == userId);
            if (!isEnrolled) return Forbid("You must be enrolled to leave a review.");

            // Verify not already reviewed
            if (course.Reviews.Any(r => r.UserId == userId))
            {
                return BadRequest("You have already reviewed this course.");
            }

            // ML Integration: Analyze Sentiment
            string sentimentLabel = "Unavailable";
            double? sentimentScore = null;

            var sentimentResult = await _mlService.AnalyzeSentimentAsync(dto.Comment);
            if (sentimentResult != null)
            {
                sentimentLabel = sentimentResult.Label;
                sentimentScore = sentimentResult.Score;
            }

            var review = new Review
            {
                UserId = userId,
                CourseId = id,
                Rating = dto.Rating,
                Comment = dto.Comment,
                SentimentLabel = sentimentLabel,
                SentimentScore = sentimentScore,
                CreatedAt = DateTime.UtcNow
            };

            _context.Reviews.Add(review);
            
            // Update Course Average Rating
            course.Reviews.Add(review);
            course.AverageRating = course.Reviews.Average(r => r.Rating);
            _context.Entry(course).State = EntityState.Modified;

            await _context.SaveChangesAsync();

            return Ok(review);
        }
    }

    public class CreateReviewDto
    {
        public int Rating { get; set; }
        public string Comment { get; set; } = string.Empty;
    }
}
