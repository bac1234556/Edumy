using System.Net.Http.Json;
using System.Text.Json.Serialization;

namespace EduMy.Backend.Services
{
    public interface IMachineLearningService
    {
        Task<SentimentResult?> AnalyzeSentimentAsync(string text);
        Task<ClassificationResult?> ClassifyCourseAsync(string title, string description);
        Task<RecommendationResult?> RecommendCoursesAsync(int userId);
        Task<AnalyzeContentResult?> AnalyzeContentAsync(string title, string description);
    }

    public class MachineLearningService : IMachineLearningService
    {
        private readonly HttpClient _httpClient;
        private readonly ILogger<MachineLearningService> _logger;

        public MachineLearningService(HttpClient httpClient, ILogger<MachineLearningService> logger)
        {
            _httpClient = httpClient;
            _logger = logger;
        }

        public async Task<SentimentResult?> AnalyzeSentimentAsync(string text)
        {
            try
            {
                var response = await _httpClient.PostAsJsonAsync("/sentiment/analyze", new { text });
                if (response.IsSuccessStatusCode)
                {
                    return await response.Content.ReadFromJsonAsync<SentimentResult>();
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error calling ML Sentiment API");
            }
            return null;
        }

        public async Task<ClassificationResult?> ClassifyCourseAsync(string title, string description)
        {
            try
            {
                var response = await _httpClient.PostAsJsonAsync("/classification/course", new { title, description });
                if (response.IsSuccessStatusCode)
                {
                    return await response.Content.ReadFromJsonAsync<ClassificationResult>();
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error calling ML Classify API");
            }
            return null;
        }

        public async Task<RecommendationResult?> RecommendCoursesAsync(int userId)
        {
            try
            {
                var response = await _httpClient.PostAsJsonAsync("/recommendations", new { userId, topK = 10 });
                if (response.IsSuccessStatusCode)
                {
                    var resultV2 = await response.Content.ReadFromJsonAsync<RecommendationResponseV2>();
                    if (resultV2 != null)
                    {
                        var ids = resultV2.Recommendations
                            .Select(r => int.TryParse(r.CourseId, out int id) ? id : 0)
                            .Where(id => id > 0)
                            .ToList();
                        var scores = resultV2.Recommendations.Select(r => r.Score).ToList();
                        
                        return new RecommendationResult
                        {
                            RecommendedCourseIds = ids,
                            Scores = scores
                        };
                    }
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error calling ML /recommendations API");
            }
            return null;
        }

        public async Task<AnalyzeContentResult?> AnalyzeContentAsync(string title, string description)
        {
            try
            {
                var response = await _httpClient.PostAsJsonAsync("/course/analyze-content", new { title, description });
                if (response.IsSuccessStatusCode)
                {
                    return await response.Content.ReadFromJsonAsync<AnalyzeContentResult>();
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error calling ML Analyze Content API");
            }
            return null;
        }
    }

    public class SentimentResult
    {
        [JsonPropertyName("label")]
        public string Label { get; set; } = string.Empty;
        
        [JsonPropertyName("score")]
        public double Score { get; set; }
    }

    public class ClassificationResult
    {
        [JsonPropertyName("predictedCategory")]
        public string PredictedCategory { get; set; } = string.Empty;

        public string Category => PredictedCategory;
        
        [JsonPropertyName("confidence")]
        public double? Confidence { get; set; }

        [JsonPropertyName("confidenceAvailable")]
        public bool ConfidenceAvailable { get; set; }

        [JsonPropertyName("modelType")]
        public string ModelType { get; set; } = string.Empty;

        [JsonPropertyName("modelVersion")]
        public string ModelVersion { get; set; } = string.Empty;
    }

    public class RecommendationResult
    {
        [JsonPropertyName("recommendedCourseIds")]
        public List<int> RecommendedCourseIds { get; set; } = new List<int>();
        
        [JsonPropertyName("scores")]
        public List<double> Scores { get; set; } = new List<double>();
    }

    public class AnalyzeContentResult
    {
        [JsonPropertyName("tags")]
        public List<string> Tags { get; set; } = new List<string>();

        [JsonPropertyName("is_toxic")]
        public bool IsToxic { get; set; }

        [JsonPropertyName("toxicity_score")]
        public double ToxicityScore { get; set; }

        [JsonPropertyName("quality_score")]
        public double QualityScore { get; set; }

        [JsonPropertyName("popularity_score")]
        public double PopularityScore { get; set; }
    }

    public class RecommendItemV2
    {
        [JsonPropertyName("courseId")]
        public string CourseId { get; set; } = string.Empty;

        [JsonPropertyName("score")]
        public double Score { get; set; }
    }

    public class RecommendationResponseV2
    {
        [JsonPropertyName("modelVersion")]
        public string ModelVersion { get; set; } = string.Empty;

        [JsonPropertyName("trainingTimestamp")]
        public string TrainingTimestamp { get; set; } = string.Empty;

        [JsonPropertyName("recommendations")]
        public List<RecommendItemV2> Recommendations { get; set; } = new List<RecommendItemV2>();
    }
}
