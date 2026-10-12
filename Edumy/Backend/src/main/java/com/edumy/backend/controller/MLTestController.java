package com.edumy.backend.controller;

import com.edumy.backend.model.*;
import com.edumy.backend.dto.*;
import com.edumy.backend.repository.*;
import com.edumy.backend.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/MLTest")
public class MLTestController {

    @PostMapping("/sentiment")
    public ResponseEntity<?> testSentiment(@RequestBody String text) {
        return ResponseEntity.ok(Map.of("label", "Neutral", "score", 0.5));
    }

    @PostMapping("/classify")
    public ResponseEntity<?> testClassify(@RequestBody ClassifyTestRequest req) {
        return ResponseEntity.ok(Map.of("category", "Development", "confidence", 0.9));
    }

    @PostMapping("/recommend")
    public ResponseEntity<?> testRecommend(@RequestBody Integer userId) {
        return ResponseEntity.ok(Map.of("recommendedCourseIds", new int[]{1, 2, 3}));
    }

    @PostMapping("/course-classification")
    public ResponseEntity<?> suggestCategory(@RequestBody ClassifyTestRequest req) {
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
