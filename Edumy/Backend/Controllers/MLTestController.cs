using EduMy.Backend.Services;
using Microsoft.AspNetCore.Mvc;

namespace EduMy.Backend.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class MLTestController : ControllerBase
    {
        private readonly IMachineLearningService _mlService;

        public MLTestController(IMachineLearningService mlService)
        {
            _mlService = mlService;
        }

        [HttpPost("sentiment")]
        public async Task<IActionResult> TestSentiment([FromBody] string text)
        {
            var result = await _mlService.AnalyzeSentimentAsync(text);
            return Ok(result);
        }

        [HttpPost("classify")]
        public async Task<IActionResult> TestClassify([FromBody] ClassifyTestRequest req)
        {
            var result = await _mlService.ClassifyCourseAsync(req.Title, req.Description);
            return Ok(result);
        }

        [HttpPost("recommend")]
        public async Task<IActionResult> TestRecommend([FromBody] int userId)
        {
            var result = await _mlService.RecommendCoursesAsync(userId);
            return Ok(result);
        }

        [HttpPost("course-classification")]
        public async Task<IActionResult> SuggestCategory([FromBody] ClassifyTestRequest req)
        {
            var classification = await _mlService.ClassifyCourseAsync(req.Title, req.Description);
            if (classification != null)
            {
                return Ok(new {
                    success = true,
                    suggestion = classification
                });
            }
            return Ok(new {
                success = false,
                code = "ML_SERVICE_UNAVAILABLE",
                message = "Không thể gợi ý danh mục lúc này. Vui lòng chọn danh mục thủ công."
            });
        }
    }

    public class ClassifyTestRequest
    {
        public string Title { get; set; } = string.Empty;
        public string Description { get; set; } = string.Empty;
    }
}
