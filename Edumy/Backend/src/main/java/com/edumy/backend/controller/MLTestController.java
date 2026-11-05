package com.edumy.backend.controller;

import com.edumy.backend.model.*;
import com.edumy.backend.dto.*;
import com.edumy.backend.repository.*;
import com.edumy.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping({"/api/MLTest", "/api/mltest"})
public class MLTestController {

    @Autowired
    private com.edumy.backend.service.MachineLearningService machineLearningService;

    @PostMapping("/sentiment")
    public ResponseEntity<?> testSentiment(@RequestBody Map<String, String> body) {
        String text = body != null ? body.getOrDefault("text", "") : "";
        var res = machineLearningService.analyzeSentiment(text);
        return ResponseEntity.ok(res != null ? res : Map.of("label", "Neutral", "score", 0.5));
    }

    @PostMapping("/classify")
    public ResponseEntity<?> testClassify(@RequestBody ClassifyTestRequest req) {
        if (req != null) {
            var res = machineLearningService.classifyCourse(req.getTitle(), req.getDescription());
            if (res != null) return ResponseEntity.ok(res);
        }
        return ResponseEntity.ok(Map.of("category", "Development", "confidence", 0.9));
    }

    @PostMapping("/recommend")
    public ResponseEntity<?> testRecommend(@RequestBody Map<String, Object> req) {
        Integer userId = req != null && req.get("userId") != null ? (Integer) req.get("userId") : 1;
        var res = machineLearningService.recommendCourses(userId);
        return ResponseEntity.ok(Map.of("recommendedCourseIds", res != null ? res.getRecommendedCourseIds() : List.of()));
    }

    @PostMapping("/course-classification")
    public ResponseEntity<?> suggestCategory(@RequestBody ClassifyTestRequest req) {
        if (req != null) {
            var res = machineLearningService.classifyCourse(req.getTitle(), req.getDescription());
            if (res != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "category", res.getPredictedCategory(),
                    "confidence", res.getConfidence()
                ));
            }
        }
        return ResponseEntity.ok(Map.of(
            "success", false,
            "code", "ML_SERVICE_UNAVAILABLE",
            "message", "Không thể gợi ý danh mục lúc này. Vui lòng chọn danh mục thủ công."
        ));
    }
}

class ClassifyTestRequest {
    private String title;
    private String description;
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
