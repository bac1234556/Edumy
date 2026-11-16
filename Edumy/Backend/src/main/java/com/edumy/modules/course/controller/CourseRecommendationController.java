package com.edumy.modules.course.controller;

import com.edumy.modules.course.dto.CourseBundleResponse;
import com.edumy.modules.course.dto.SimilarCourseDto;
import com.edumy.modules.course.model.Course;
import com.edumy.modules.course.repository.CourseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
public class CourseRecommendationController {

    private final CourseRepository courseRepository;

    public CourseRecommendationController(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    /**
     * Gợi ý khóa học liên quan dựa trên cùng danh mục / chuyên mục
     */
    @GetMapping({"/api/v1/courses/{id}/similar", "/api/courses/{id}/similar"})
    public ResponseEntity<List<SimilarCourseDto>> getSimilarCourses(
            @PathVariable("id") Integer id,
            @RequestParam(value = "limit", defaultValue = "4") int limit) {
        Course current = courseRepository.findById(id).orElse(null);
        if (current == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<Course> pool = courseRepository.findAll().stream()
                .filter(c -> !c.getCourseId().equals(id))
                .filter(c -> "Published".equalsIgnoreCase(c.getStatus()))
                .sorted((c1, c2) -> {
                    boolean sameCat1 = Objects.equals(c1.getCategoryId(), current.getCategoryId());
                    boolean sameCat2 = Objects.equals(c2.getCategoryId(), current.getCategoryId());
                    if (sameCat1 && !sameCat2) return -1;
                    if (!sameCat1 && sameCat2) return 1;
                    double r1 = c1.getAverageRating() != null ? c1.getAverageRating() : 0.0;
                    double r2 = c2.getAverageRating() != null ? c2.getAverageRating() : 0.0;
                    return Double.compare(r2, r1);
                })
                .limit(limit)
                .collect(Collectors.toList());

        List<SimilarCourseDto> dtos = pool.stream().map(c -> {
            String catName = c.getCategory() != null ? c.getCategory().getName() : "Chuyên môn";
            String instName = c.getInstructor() != null ? c.getInstructor().getFullName() : "Giảng viên Edumy";
            String instHead = c.getInstructor() != null ? c.getInstructor().getBio() : "Chuyên gia đào tạo";

            double price = c.getPrice() != null ? c.getPrice().doubleValue() : 0.0;
            return SimilarCourseDto.builder()
                    .courseId(c.getCourseId())
                    .title(c.getTitle())
                    .shortDescription(c.getDescription())
                    .price(price)
                    .originalPrice(price * 1.3)
                    .thumbnailUrl(c.getThumbnailUrl())
                    .averageRating(c.getAverageRating() != null ? c.getAverageRating() : 5.0)
                    .reviewCount(c.getReviews() != null ? c.getReviews().size() : 12)
                    .enrollmentCount(c.getStudentCount() != null ? c.getStudentCount() : 150)
                    .positiveRatingPercent(95)
                    .instructorName(instName)
                    .instructorHeadline(instHead)
                    .categoryName(catName)
                    .recommendationReason("Cùng chuyên mục " + catName + " đánh giá cao")
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Gợi ý combo khóa học cùng chuyên mục
     */
    @GetMapping({"/api/v1/courses/{id}/bundle-recommendation", "/api/courses/{id}/bundle-recommendation"})
    public ResponseEntity<?> getBundleRecommendation(@PathVariable("id") Integer id) {
        Course current = courseRepository.findById(id).orElse(null);
        if (current == null) return ResponseEntity.noContent().build();

        Course bundlePartner = courseRepository.findAll().stream()
                .filter(c -> !c.getCourseId().equals(id))
                .filter(c -> "Published".equalsIgnoreCase(c.getStatus()))
                .filter(c -> Objects.equals(c.getCategoryId(), current.getCategoryId()))
                .findFirst()
                .orElse(null);

        if (bundlePartner == null) {
            return ResponseEntity.noContent().build();
        }

        int p1 = current.getPrice() != null ? current.getPrice().intValue() : 0;
        int p2 = bundlePartner.getPrice() != null ? bundlePartner.getPrice().intValue() : 0;
        int total = p1 + p2;
        int bundlePrice = (int) (total * 0.85);

        CourseBundleResponse response = CourseBundleResponse.builder()
                .mainCourse(current)
                .bundledCourse(bundlePartner)
                .originalTotal(total)
                .bundlePrice(bundlePrice)
                .discountPercent(15)
                .savings(total - bundlePrice)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Gợi ý khóa học hàng đầu cho trang chủ
     */
    @GetMapping({"/api/courses/recommend", "/api/v1/courses/recommend"})
    public ResponseEntity<List<Course>> getStudentRecommendations() {
        List<Course> topCourses = courseRepository.findAll().stream()
                .filter(c -> "Published".equalsIgnoreCase(c.getStatus()))
                .sorted((c1, c2) -> {
                    double r1 = c1.getAverageRating() != null ? c1.getAverageRating() : 0.0;
                    double r2 = c2.getAverageRating() != null ? c2.getAverageRating() : 0.0;
                    return Double.compare(r2, r1);
                })
                .limit(4)
                .collect(Collectors.toList());
        return ResponseEntity.ok(topCourses);
    }
}
