package com.edumy.backend.service;

import com.edumy.backend.dto.*;
import com.edumy.backend.model.*;
import com.edumy.backend.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseAiService {

    private static final Logger logger = LoggerFactory.getLogger(CourseAiService.class);

    private final RestTemplate restTemplate;
    private final CategoryRepository categoryRepository;
    private final CourseRepository courseRepository;
    private final ReviewRepository reviewRepository;

    @Value("${machine-learning.base-url:http://localhost:8000}")
    private String mlBaseUrl;

    public CourseAiService(RestTemplate restTemplate,
                           CategoryRepository categoryRepository,
                           CourseRepository courseRepository,
                           ReviewRepository reviewRepository) {
        this.restTemplate = restTemplate;
        this.categoryRepository = categoryRepository;
        this.courseRepository = courseRepository;
        this.reviewRepository = reviewRepository;
    }

    /**
     * Feature 1: AI Category & Curriculum Suggestion khi tạo khóa học
     */
    public AiCategorySuggestResponse suggestCategoryAndCurriculum(String title, String description) {
        AiCategorySuggestResponse response = null;

        try {
            String url = mlBaseUrl + "/api/v1/ai/suggest-category";
            Map<String, String> request = new HashMap<>();
            request.put("title", title != null ? title : "");
            request.put("description", description != null ? description : "");

            response = restTemplate.postForObject(url, request, AiCategorySuggestResponse.class);
        } catch (Exception ex) {
            logger.warn("FastAPI suggest-category unavailable or error: {}. Triggering Java-side intelligent fallback.", ex.getMessage());
        }

        // Fallback if FastAPI is offline
        if (response == null || response.getCategoryName() == null) {
            response = createFallbackCategorySuggestion(title, description);
        }

        // Match categoryName to real Database CategoryId
        matchCategoryWithDatabase(response);

        return response;
    }

    private void matchCategoryWithDatabase(AiCategorySuggestResponse response) {
        List<Category> allCats = categoryRepository.findAll();
        if (allCats.isEmpty()) return;

        String targetName = response.getCategoryName() != null ? response.getCategoryName().toLowerCase() : "";

        Category matched = allCats.stream()
                .filter(c -> c.getName() != null && c.getName().equalsIgnoreCase(targetName))
                .findFirst()
                .orElse(null);

        if (matched == null) {
            // Partial match
            matched = allCats.stream()
                    .filter(c -> c.getName() != null && (targetName.contains(c.getName().toLowerCase()) || c.getName().toLowerCase().contains(targetName)))
                    .findFirst()
                    .orElse(allCats.get(0)); // Default to first category if none match
        }

        if (matched != null) {
            response.setSuggestedCategoryId(matched.getCategoryId());
            response.setCategoryName(matched.getName());
        }
    }

    private AiCategorySuggestResponse createFallbackCategorySuggestion(String title, String description) {
        String combined = ((title != null ? title : "") + " " + (description != null ? description : "")).toLowerCase();
        String catName = "Development";
        List<String> tags = new ArrayList<>();

        if (combined.contains("figma") || combined.contains("ui") || combined.contains("ux") || combined.contains("thiết kế") || combined.contains("design")) {
            catName = "Design";
            tags.addAll(List.of("ui-ux", "figma", "product-design", "prototyping"));
        } else if (combined.contains("kinh doanh") || combined.contains("business") || combined.contains("tài chính") || combined.contains("startup")) {
            catName = "Business";
            tags.addAll(List.of("business", "startup", "quan-tri", "tai-chinh"));
        } else if (combined.contains("marketing") || combined.contains("seo") || combined.contains("quảng cáo") || combined.contains("ads")) {
            catName = "Marketing";
            tags.addAll(List.of("marketing", "seo", "digital-marketing", "content"));
        } else if (combined.contains("cloud") || combined.contains("aws") || combined.contains("devops") || combined.contains("docker") || combined.contains("linux")) {
            catName = "IT & Software";
            tags.addAll(List.of("devops", "cloud", "docker", "sysadmin"));
        } else {
            catName = "Development";
            tags.addAll(List.of("lap-trinh", "web-development", "backend", "fullstack"));
        }

        List<SectionOutlineDto> outline = new ArrayList<>();
        outline.add(new SectionOutlineDto("Chương 1: Tổng quan & Thiết lập môi trường dự án", List.of(
                new LessonOutlineDto("Giới thiệu khóa học & Lộ trình thực chiến", 15, true),
                new LessonOutlineDto("Cài đặt công cụ và khởi tạo dự án đầu tiên", 25, true)
        )));
        outline.add(new SectionOutlineDto("Chương 2: Kiến thức nền tảng & Lập trình tính năng", List.of(
                new LessonOutlineDto("Cú pháp cốt lõi và bài toán thực tế", 30, false),
                new LessonOutlineDto("Xây dựng module chức năng chính", 40, false)
        )));
        outline.add(new SectionOutlineDto("Chương 3: Hoàn thiện dự án & Triển khai Production", List.of(
                new LessonOutlineDto("Kiểm thử chất lượng & Tối ưu hóa", 35, false),
                new LessonOutlineDto("Đóng gói sản phẩm & Bàn giao", 30, false)
        )));

        AiCategorySuggestResponse fallback = new AiCategorySuggestResponse();
        fallback.setCategoryName(catName);
        fallback.setConfidenceScore(0.88);
        fallback.setSuggestedTags(tags);
        fallback.setCurriculumOutline(outline);
        fallback.setModelType("Java_RuleBased_Fallback");
        fallback.setIsFallback(true);
        return fallback;
    }

    /**
     * Feature 2: Review Sentiment Analysis
     */
    public SentimentAnalysisResponse analyzeSentiment(Integer rating, String comment) {
        if (comment == null) comment = "";
        int r = (rating != null && rating >= 1 && rating <= 5) ? rating : 5;

        try {
            String url = mlBaseUrl + "/api/v1/ai/sentiment-analysis";
            SentimentAnalysisRequest req = new SentimentAnalysisRequest(r, comment);
            SentimentAnalysisResponse res = restTemplate.postForObject(url, req, SentimentAnalysisResponse.class);
            if (res != null) {
                return res;
            }
        } catch (Exception ex) {
            logger.warn("FastAPI sentiment-analysis unavailable: {}. Using fallback.", ex.getMessage());
        }

        // Fallback rule-based
        String sentiment = "POSITIVE";
        String label = "Positive";
        double score = 0.85;

        if (r <= 2) {
            sentiment = "NEGATIVE";
            label = "Negative";
            score = 0.20;
        } else if (r == 3) {
            sentiment = "NEUTRAL";
            label = "Neutral";
            score = 0.50;
        }

        SentimentAnalysisResponse fallback = new SentimentAnalysisResponse();
        fallback.setSentiment(sentiment);
        fallback.setLabel(label);
        fallback.setScore(score);
        fallback.setConfidence(0.80);
        fallback.setModelVersion("Java_Fallback");
        fallback.setIsFallback(true);
        return fallback;
    }

    /**
     * Feature 3: Gợi ý khóa học tương tự dựa trên đánh giá tích cực
     */
    public List<SimilarCourseDto> getSimilarCourses(Integer courseId, int limit) {
        Course currentCourse = courseRepository.findById(courseId).orElse(null);
        if (currentCourse == null) return Collections.emptyList();

        List<Course> allCourses = courseRepository.findAll();

        // 1. Filter courses same category or related, excluding current course
        List<Course> candidates = allCourses.stream()
                .filter(c -> !c.getCourseId().equals(courseId))
                .filter(c -> "PUBLISHED".equalsIgnoreCase(c.getStatus()) || c.getStatus() == null)
                .sorted((c1, c2) -> {
                    boolean catMatch1 = currentCourse.getCategoryId() != null && currentCourse.getCategoryId().equals(c1.getCategoryId());
                    boolean catMatch2 = currentCourse.getCategoryId() != null && currentCourse.getCategoryId().equals(c2.getCategoryId());
                    if (catMatch1 && !catMatch2) return -1;
                    if (!catMatch1 && catMatch2) return 1;
                    double r1 = c1.getAverageRating() != null ? c1.getAverageRating() : 0.0;
                    double r2 = c2.getAverageRating() != null ? c2.getAverageRating() : 0.0;
                    return Double.compare(r2, r1);
                })
                .limit(Math.max(limit, 4))
                .collect(Collectors.toList());

        List<SimilarCourseDto> result = new ArrayList<>();
        for (Course c : candidates) {
            List<Review> revs = reviewRepository.findByCourseId(c.getCourseId());
            int totalRevs = revs != null ? revs.size() : 0;
            int posCount = 0;
            if (revs != null) {
                for (Review r : revs) {
                    if ((r.getRating() != null && r.getRating() >= 4) ||
                        "POSITIVE".equalsIgnoreCase(r.getSentimentLabel()) ||
                        "Positive".equalsIgnoreCase(r.getSentimentLabel())) {
                        posCount++;
                    }
                }
            }
            int positivePercent = totalRevs > 0 ? (int) Math.round((double) posCount * 100 / totalRevs) : 100;

            String instName = c.getInstructor() != null ? c.getInstructor().getFullName() : "Giảng viên Edumy";
            String catName = c.getCategory() != null ? c.getCategory().getName() : "Chung";
            double cPrice = c.getPrice() != null ? c.getPrice().doubleValue() : 0.0;
            double cOrigPrice = cPrice > 0 ? cPrice * 1.5 : 0.0;
            String descSnippet = c.getDescription() != null && c.getDescription().length() > 120
                    ? c.getDescription().substring(0, 120) + "..."
                    : (c.getDescription() != null ? c.getDescription() : "");

            SimilarCourseDto dto = SimilarCourseDto.builder()
                    .courseId(c.getCourseId())
                    .title(c.getTitle())
                    .shortDescription(descSnippet)
                    .price(cPrice)
                    .originalPrice(cOrigPrice)
                    .thumbnailUrl(c.getThumbnailUrl())
                    .averageRating(c.getAverageRating() != null ? c.getAverageRating() : 5.0)
                    .reviewCount(totalRevs)
                    .enrollmentCount(c.getStudentCount() != null ? c.getStudentCount() : 0)
                    .positiveRatingPercent(positivePercent)
                    .instructorName(instName)
                    .instructorHeadline(c.getInstructor() != null && c.getInstructor().getBio() != null ? c.getInstructor().getBio() : "Chuyên gia đào tạo")
                    .categoryName(catName)
                    .recommendationReason(totalRevs > 0 ? ("Cùng lộ trình & " + positivePercent + "% đánh giá hài lòng") : "Cùng danh mục & phù hợp lộ trình học")
                    .build();

            result.add(dto);
        }

        return result;
    }

    /**
     * Feature 4: Gợi ý Combo khóa học (Bundle Package Recommendation)
     */
    public CourseBundleResponse getBundleRecommendation(Integer courseId) {
        Course mainCourse = courseRepository.findById(courseId).orElse(null);
        if (mainCourse == null) return null;

        List<Course> allCourses = courseRepository.findAll();

        // Find bundled course in same category, or next best course
        Course bundledCourse = allCourses.stream()
                .filter(c -> !c.getCourseId().equals(courseId))
                .filter(c -> mainCourse.getCategoryId() != null && mainCourse.getCategoryId().equals(c.getCategoryId()))
                .filter(c -> c.getPrice() != null && c.getPrice().compareTo(java.math.BigDecimal.ZERO) > 0)
                .findFirst()
                .orElse(null);

        if (bundledCourse == null) {
            bundledCourse = allCourses.stream()
                    .filter(c -> !c.getCourseId().equals(courseId))
                    .filter(c -> c.getPrice() != null && c.getPrice().compareTo(java.math.BigDecimal.ZERO) > 0)
                    .findFirst()
                    .orElse(null);
        }

        if (bundledCourse == null) return null;

        int price1 = (mainCourse.getPrice() != null) ? mainCourse.getPrice().intValue() : 0;
        int price2 = (bundledCourse.getPrice() != null) ? bundledCourse.getPrice().intValue() : 0;
        if (price1 <= 0 && price2 <= 0) return null;

        int originalTotal = price1 + price2;
        int discountPercent = 20; // 20% discount on bundle
        int bundlePrice = (int) Math.round(originalTotal * 0.80);
        int savings = originalTotal - bundlePrice;

        return CourseBundleResponse.builder()
                .mainCourse(mainCourse)
                .bundledCourse(bundledCourse)
                .originalTotal(originalTotal)
                .bundlePrice(bundlePrice)
                .discountPercent(discountPercent)
                .savings(savings)
                .build();
    }
}
