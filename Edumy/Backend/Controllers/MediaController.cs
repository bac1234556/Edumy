using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace EduMy.Backend.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class MediaController : ControllerBase
    {
        private readonly IWebHostEnvironment _env;

        public MediaController(IWebHostEnvironment env)
        {
            _env = env;
        }

        [HttpPost("upload")]
        [Authorize(Roles = "Instructor,Admin")]
        // Consider limiting max size via config in Program.cs or using attributes for video files
        [RequestSizeLimit(100_000_000)] // 100MB limit for demo purposes
        public async Task<IActionResult> UploadFile(IFormFile file)
        {
            if (file == null || file.Length == 0)
                return BadRequest("No file uploaded.");

            // Create uploads folder if not exists
            var uploadsPath = Path.Combine(_env.WebRootPath ?? Path.Combine(Directory.GetCurrentDirectory(), "wwwroot"), "uploads");
            if (!Directory.Exists(uploadsPath))
            {
                Directory.CreateDirectory(uploadsPath);
            }

            // Generate unique filename to prevent overwriting
            var ext = Path.GetExtension(file.FileName);
            var safeFilename = Guid.NewGuid().ToString("N") + ext;
            var filePath = Path.Combine(uploadsPath, safeFilename);

            using (var stream = new FileStream(filePath, FileMode.Create))
            {
                await file.CopyToAsync(stream);
            }

            // Return the relative URL
            var fileUrl = $"/uploads/{safeFilename}";
            return Ok(new { url = fileUrl });
        }
    }
}
