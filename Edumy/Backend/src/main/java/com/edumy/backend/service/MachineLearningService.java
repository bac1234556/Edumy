package com.edumy.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.ArrayList;

@Service
public class MachineLearningService {

    private static final Logger logger = LoggerFactory.getLogger(MachineLearningService.class);
    private final RestTemplate restTemplate;

    public MachineLearningService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public SentimentResult analyzeSentiment(String text) {
        try {
            SentimentResult result = new SentimentResult();
            result.setLabel("Neutral");
            result.setScore(0.5);
            return result;
        } catch (Exception ex) {
            logger.error("Error calling ML Sentiment API", ex);
        }
        return null;
    }

    public ClassificationResult classifyCourse(String title, String description) {
        try {
            ClassificationResult result = new ClassificationResult();
            result.setPredictedCategory("Development");
            result.setConfidence(0.9);
            return result;
        } catch (Exception ex) {
            logger.error("Error calling ML Classify API", ex);
        }
        return null;
    }

    public RecommendationResult recommendCourses(int userId) {
        try {
            RecommendationResult result = new RecommendationResult();
            result.setRecommendedCourseIds(List.of(1, 2, 3));
            result.setScores(List.of(0.9, 0.8, 0.7));
            return result;
        } catch (Exception ex) {
            logger.error("Error calling ML /recommendations API", ex);
        }
        return null;
    }

    public AnalyzeContentResult analyzeContent(String title, String description) {
        try {
            AnalyzeContentResult result = new AnalyzeContentResult();
            result.setTags(List.of("Programming", "Tech"));
            result.setToxic(false);
            result.setToxicityScore(0.01);
            result.setQualityScore(0.95);
            result.setPopularityScore(0.8);
            return result;
        } catch (Exception ex) {
            logger.error("Error calling ML Analyze Content API", ex);
        }
        return null;
    }

    @Data
    public static class SentimentResult {
        private String label = "";
        private double score;
    }

    @Data
    public static class ClassificationResult {
        private String predictedCategory = "";
        public String getCategory() { return predictedCategory; }
        private Double confidence;
        private boolean confidenceAvailable;
        private String modelType = "";
        private String modelVersion = "";
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
