package com.edumy.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@Service
public class MachineLearningService {

    private static final Logger logger = LoggerFactory.getLogger(MachineLearningService.class);
    private final RestTemplate restTemplate;

    @Value("${machine-learning.base-url:http://localhost:8000}")
    private String mlBaseUrl;

    public MachineLearningService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public SentimentResult analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            SentimentResult emptyRes = new SentimentResult();
            emptyRes.setLabel("Neutral");
            emptyRes.setScore(0.5);
            return emptyRes;
        }

        try {
            String url = mlBaseUrl + "/sentiment/analyze";
            Map<String, String> request = Map.of("text", text);
            Map<?, ?> response = restTemplate.postForObject(url, request, Map.class);
            if (response != null && response.containsKey("label")) {
                SentimentResult result = new SentimentResult();
                result.setLabel(String.valueOf(response.get("label")));
                if (response.containsKey("score") && response.get("score") instanceof Number) {
                    result.setScore(((Number) response.get("score")).doubleValue());
                } else {
                    result.setScore(0.5);
                }
                return result;
            }
        } catch (Exception ex) {
            logger.warn("ML Sentiment API unavailable or error: {}. Using fallback.", ex.getMessage());
        }

        SentimentResult fallback = new SentimentResult();
        fallback.setLabel("Neutral");
        fallback.setScore(0.5);
        return fallback;
    }

    public ClassificationResult classifyCourse(String title, String description) {
        try {
            String url = mlBaseUrl + "/classification/predict";
            Map<String, String> request = new HashMap<>();
            request.put("title", title != null ? title : "");
            request.put("description", description != null ? description : "");

            Map<?, ?> response = restTemplate.postForObject(url, request, Map.class);
            if (response != null && response.containsKey("predictedCategory")) {
                ClassificationResult result = new ClassificationResult();
                result.setPredictedCategory(String.valueOf(response.get("predictedCategory")));
                if (response.containsKey("confidence") && response.get("confidence") instanceof Number) {
                    result.setConfidence(((Number) response.get("confidence")).doubleValue());
                    result.setConfidenceAvailable(true);
                } else {
                    result.setConfidence(0.85);
                }
                if (response.containsKey("modelType")) {
                    result.setModelType(String.valueOf(response.get("modelType")));
                }
                return result;
            }
        } catch (Exception ex) {
            logger.warn("ML Classification API unavailable or error: {}. Using fallback.", ex.getMessage());
        }

        ClassificationResult fallback = new ClassificationResult();
        fallback.setPredictedCategory("Development");
        fallback.setConfidence(0.9);
        return fallback;
    }

    public RecommendationResult recommendCourses(int userId) {
        try {
            String url = mlBaseUrl + "/recommendations";
            Map<String, Object> request = Map.of("userId", userId, "topK", 10);
            Map<?, ?> response = restTemplate.postForObject(url, request, Map.class);
            if (response != null && response.containsKey("recommendations")) {
                RecommendationResult result = new RecommendationResult();
                List<?> recList = (List<?>) response.get("recommendations");
                for (Object item : recList) {
                    if (item instanceof Map) {
                        Map<?, ?> mapItem = (Map<?, ?>) item;
                        if (mapItem.containsKey("courseId")) {
                            try {
                                int courseId = Integer.parseInt(String.valueOf(mapItem.get("courseId")));
                                result.getRecommendedCourseIds().add(courseId);
                                if (mapItem.containsKey("score") && mapItem.get("score") instanceof Number) {
                                    result.getScores().add(((Number) mapItem.get("score")).doubleValue());
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                }
                if (!result.getRecommendedCourseIds().isEmpty()) {
                    return result;
                }
            }
        } catch (Exception ex) {
            logger.warn("ML Recommendations API unavailable: {}. Using fallback.", ex.getMessage());
        }

        RecommendationResult fallback = new RecommendationResult();
        fallback.setRecommendedCourseIds(List.of(1, 2, 3));
        fallback.setScores(List.of(0.9, 0.8, 0.7));
        return fallback;
    }

    public AnalyzeContentResult analyzeContent(String title, String description) {
        try {
            String url = mlBaseUrl + "/analyze-content";
            Map<String, String> request = new HashMap<>();
            request.put("title", title != null ? title : "");
            request.put("description", description != null ? description : "");

            AnalyzeContentResult result = restTemplate.postForObject(url, request, AnalyzeContentResult.class);
            if (result != null) {
                return result;
            }
        } catch (Exception ex) {
            logger.warn("ML Content Analysis API error: {}. Using fallback.", ex.getMessage());
        }

        AnalyzeContentResult fallback = new AnalyzeContentResult();
        fallback.setTags(List.of("Programming", "Tech"));
        fallback.setToxic(false);
        fallback.setToxicityScore(0.01);
        fallback.setQualityScore(0.95);
        fallback.setPopularityScore(0.8);
        return fallback;
    }

    @Data
    public static class SentimentResult {
        private String label = "Neutral";
        private double score = 0.5;
    }

    @Data
    public static class ClassificationResult {
        private String predictedCategory = "Development";
        public String getCategory() { return predictedCategory; }
        private Double confidence = 0.85;
        private boolean confidenceAvailable = true;
        private String modelType = "MLP";
        private String modelVersion = "v1.0";
    }

    @Data
    public static class RecommendationResult {
        private List<Integer> recommendedCourseIds = new ArrayList<>();
        private List<Double> scores = new ArrayList<>();
    }

    @Data
    public static class AnalyzeContentResult {
        private List<String> tags = new ArrayList<>();
        @JsonProperty("is_toxic")
        private boolean isToxic;
        @JsonProperty("toxicity_score")
        private double toxicityScore;
        @JsonProperty("quality_score")
        private double qualityScore;
        @JsonProperty("popularity_score")
        private double popularityScore;
    }
}
