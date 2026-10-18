package com.edumy.backend.controller;

import com.edumy.backend.dto.*;
import com.edumy.backend.service.CourseAiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class CourseAiController {

    private final CourseAiService courseAiService;

    public CourseAiController(CourseAiService courseAiService) {
        this.courseAiService = courseAiService;
    }

    /**
     * Feature 1: AI Category & Curriculum Suggestion khi tạo khóa học
     */
    @PostMapping({"/api/v1/ai/suggest-category", "/api/ai/suggest-category", "/api/courses/ai/suggest"})
    public ResponseEntity<AiCategorySuggestResponse> suggestCategory(@RequestBody AiCategorySuggestRequest req) {
        String title = req != null ? req.getTitle() : "";
        String desc = req != null ? req.getDescription() : "";
        AiCategorySuggestResponse res = courseAiService.suggestCategoryAndCurriculum(title, desc);
        return ResponseEntity.ok(res);
    }

    /**
     * Feature 2: Phân loại nhãn cảm xúc thông minh trên Đánh giá
     */
    @PostMapping({"/api/v1/ai/sentiment-analysis", "/api/ai/sentiment-analysis"})
    public ResponseEntity<SentimentAnalysisResponse> analyzeSentiment(@RequestBody SentimentAnalysisRequest req) {
        Integer rating = req != null ? req.getRating() : 5;
        String comment = req != null ? req.getComment() : "";
        SentimentAnalysisResponse res = courseAiService.analyzeSentiment(rating, comment);
        return ResponseEntity.ok(res);
    }

    /**
     * Feature 3: Gợi ý khóa học tương tự dựa trên đánh giá tích cực
     */
    @GetMapping({"/api/v1/courses/{id}/similar", "/api/courses/{id}/similar"})
    public ResponseEntity<List<SimilarCourseDto>> getSimilarCourses(
            @PathVariable("id") Integer id,
            @RequestParam(value = "limit", defaultValue = "4") int limit) {
        List<SimilarCourseDto> similar = courseAiService.getSimilarCourses(id, limit);
        return ResponseEntity.ok(similar);
    }

    /**
     * Feature 4: Gợi ý Combo khóa học (Bundle Package Recommendation)
     */
    @GetMapping({"/api/v1/courses/{id}/bundle-recommendation", "/api/courses/{id}/bundle-recommendation"})
    public ResponseEntity<?> getBundleRecommendation(@PathVariable("id") Integer id) {
        CourseBundleResponse bundle = courseAiService.getBundleRecommendation(id);
        if (bundle == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(bundle);
    }
}
