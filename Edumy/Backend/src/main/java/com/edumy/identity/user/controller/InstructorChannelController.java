package com.edumy.identity.user.controller;

import com.edumy.authoring.model.Course;
import com.edumy.authoring.model.CourseCoInstructor;
import com.edumy.authoring.repository.CourseCoInstructorRepository;
import com.edumy.authoring.repository.CourseRepository;
import com.edumy.learning.enrollment.model.Enrollment;
import com.edumy.learning.enrollment.repository.EnrollmentRepository;
import com.edumy.learning.qa.repository.CourseQuestionRepository;
import com.edumy.learning.review.model.Review;
import com.edumy.learning.review.repository.ReviewRepository;
import com.edumy.payout.dto.EarningSummaryDto;
import com.edumy.payout.model.EarningEntry;
import com.edumy.payout.repository.EarningEntryRepository;
import com.edumy.payout.service.EarningService;
import com.edumy.shared.exception.BusinessException;
import com.edumy.shared.exception.ErrorCode;
import com.edumy.shared.response.PageResponse;
import com.edumy.shared.security.SecurityUtils;
import com.edumy.shared.time.TimeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/instructor")
@PreAuthorize("hasAnyRole('Instructor', 'Admin')")
public class InstructorChannelController {

    private final CourseRepository courseRepository;
    private final CourseCoInstructorRepository courseCoInstructorRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ReviewRepository reviewRepository;
    private final CourseQuestionRepository courseQuestionRepository;
    private final EarningEntryRepository earningEntryRepository;
    private final EarningService earningService;

    @Value("${edumy.time-zone:Asia/Ho_Chi_Minh}")
    private String timeZone;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.edumy.learning.enrollment.service.LearningProgressService learningProgressService;

    public InstructorChannelController(CourseRepository courseRepository,
                                       CourseCoInstructorRepository courseCoInstructorRepository,
                                       EnrollmentRepository enrollmentRepository,
                                       ReviewRepository reviewRepository,
                                       CourseQuestionRepository courseQuestionRepository,
                                       EarningEntryRepository earningEntryRepository,
                                       EarningService earningService) {
        this.courseRepository = courseRepository;
        this.courseCoInstructorRepository = courseCoInstructorRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.reviewRepository = reviewRepository;
        this.courseQuestionRepository = courseQuestionRepository;
        this.earningEntryRepository = earningEntryRepository;
        this.earningService = earningService;
    }

    public record TotalsDto(
            int courses,
            int publishedCourses,
            long students,
            Double averageRating,
            long ratingCount
    ) {}

    public record MoneyDto(
            long grossSales,
            long netEarnings,
            long availableBalance,
            long pendingBalance
    ) {}

    public record SeriesPointDto(
            String date,
            long enrollments,
            long netEarnings
    ) {}

    public record RecentReviewDto(
            Integer id,
            String courseTitle,
            String studentName,
            Integer rating,
            String comment,
            Instant createdAt
    ) {}

    public record InstructorOverviewDto(
            TotalsDto totals,
            MoneyDto money,
            List<SeriesPointDto> series30d,
            long unansweredQuestions,
            long pendingInvitations,
            List<RecentReviewDto> recentReviews
    ) {}

    public record InstructorCourseDto(
            Integer courseId,
            String title,
            String thumbnailUrl,
            String status,
            String role,
            int shareBps,
            long students,
            Double averageRating,
            long ratingCount,
            long netEarnings
    ) {}

    public record StudentRosterItemDto(
            Integer enrollmentId,
            Integer userId,
            String fullName,
            Instant enrolledAt,
            Integer progressPercentage,
            String status
    ) {}

    @GetMapping("/overview")
    public ResponseEntity<InstructorOverviewDto> getOverview() {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);

        List<Course> mainCourses = courseRepository.findByInstructorId(userId);
        List<CourseCoInstructor> acceptedCo = courseCoInstructorRepository.findByInstructorIdAndStatus(userId, "ACCEPTED");
        List<Integer> coCourseIds = acceptedCo.stream().map(CourseCoInstructor::getCourseId).toList();
        List<Course> coCourses = coCourseIds.isEmpty() ? Collections.emptyList() : courseRepository.findAllById(coCourseIds);

        Map<Integer, Course> allCoursesMap = new LinkedHashMap<>();
        mainCourses.forEach(c -> allCoursesMap.put(c.getCourseId(), c));
        coCourses.forEach(c -> allCoursesMap.putIfAbsent(c.getCourseId(), c));
        List<Integer> courseIds = new ArrayList<>(allCoursesMap.keySet());

        int publishedCount = (int) allCoursesMap.values().stream()
                .filter(c -> "Published".equalsIgnoreCase(c.getStatus()))
                .count();

        long totalStudents = courseIds.isEmpty() ? 0L : enrollmentRepository.countDistinctUsersByCourseIds(courseIds);
        Map<Integer, Long> ratingCounts = loadRatingCounts(courseIds);
        long totalRatingCount = ratingCounts.values().stream().mapToLong(Long::longValue).sum();

        Double averageRating = null;
        if (totalRatingCount > 0) {
            List<Double> ratedValues = allCoursesMap.values().stream()
                    .filter(c -> ratingCounts.getOrDefault(c.getCourseId(), 0L) > 0 && c.getAverageRating() != null)
                    .map(Course::getAverageRating)
                    .toList();
            if (!ratedValues.isEmpty()) {
                double avg = ratedValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                averageRating = Math.round(avg * 10.0) / 10.0;
            }
        }

        EarningSummaryDto earningSummary = earningService.getSummary(userId);
        long netEarnings = earningSummary.getTotalEarned();

        List<EarningEntry> myEntries = earningEntryRepository.findByInstructorId(userId);
        long grossSales = computeGrossSales(myEntries);

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(29);
        LocalDateTime startDt = startDate.atStartOfDay();

        List<Enrollment> recentEnrollments = courseIds.isEmpty()
                ? Collections.emptyList()
                : enrollmentRepository.findByCourseIdInAndEnrolledAtAfter(courseIds, startDt);
        Map<LocalDate, Long> enrollmentsByDay = recentEnrollments.stream()
                .filter(e -> e.getEnrolledAt() != null)
                .collect(Collectors.groupingBy(e -> e.getEnrolledAt().toLocalDate(), Collectors.counting()));

        Map<LocalDate, Long> earningsByDay = myEntries.stream()
                .filter(e -> e.getEarnedAt() != null && !e.getEarnedAt().toLocalDate().isBefore(startDate))
                .collect(Collectors.groupingBy(
                        e -> e.getEarnedAt().toLocalDate(),
                        Collectors.summingLong(e -> e.getAmount() != null ? e.getAmount() : 0L)
                ));

        List<SeriesPointDto> series30d = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            LocalDate d = startDate.plusDays(i);
            series30d.add(new SeriesPointDto(
                    d.toString(),
                    enrollmentsByDay.getOrDefault(d, 0L),
                    earningsByDay.getOrDefault(d, 0L)
            ));
        }

        long unansweredQuestions = courseIds.isEmpty() ? 0L : courseQuestionRepository.countUnansweredByCourseIds(courseIds);
        long pendingInvitations = courseCoInstructorRepository.findByInstructorIdAndStatus(userId, "INVITED").size();

        List<RecentReviewDto> recentReviews = courseIds.isEmpty()
                ? Collections.emptyList()
                : reviewRepository.findTop5ByCourseIdInOrderByCreatedAtDesc(courseIds).stream()
                        .map(r -> new RecentReviewDto(
                                r.getReviewId(),
                                r.getCourse() != null ? r.getCourse().getTitle() : "Khóa học #" + r.getCourseId(),
                                r.getUser() != null ? r.getUser().getFullName() : "Học viên Edumy",
                                r.getRating(),
                                r.getComment(),
                                TimeUtils.toInstant(r.getCreatedAt(), timeZone)
                        ))
                        .toList();

        return ResponseEntity.ok(new InstructorOverviewDto(
                new TotalsDto(allCoursesMap.size(), publishedCount, totalStudents, averageRating, totalRatingCount),
                new MoneyDto(grossSales, netEarnings, earningSummary.getAvailableBalance(), earningSummary.getPendingBalance()),
                series30d,
                unansweredQuestions,
                pendingInvitations,
                recentReviews
        ));
    }

    @GetMapping("/courses")
    public ResponseEntity<List<InstructorCourseDto>> getInstructorCourses() {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);

        List<Course> mainCourses = courseRepository.findByInstructorId(userId);
        List<CourseCoInstructor> acceptedCo = courseCoInstructorRepository.findByInstructorIdAndStatus(userId, "ACCEPTED");
        Map<Integer, Integer> coShareMap = acceptedCo.stream()
                .collect(Collectors.toMap(CourseCoInstructor::getCourseId, c -> c.getShareBps() != null ? c.getShareBps() : 0, (a, b) -> a));

        List<Course> coCourses = coShareMap.isEmpty()
                ? Collections.emptyList()
                : courseRepository.findAllById(coShareMap.keySet());

        Map<Integer, Course> allCoursesMap = new LinkedHashMap<>();
        mainCourses.forEach(c -> allCoursesMap.put(c.getCourseId(), c));
        coCourses.forEach(c -> allCoursesMap.putIfAbsent(c.getCourseId(), c));

        if (allCoursesMap.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<Integer> courseIds = new ArrayList<>(allCoursesMap.keySet());
        Map<Integer, Long> ratingCounts = loadRatingCounts(courseIds);
        Map<Integer, Long> studentCounts = new HashMap<>();
        for (Object[] row : enrollmentRepository.countByCourseIdsGrouped(courseIds)) {
            if (row != null && row.length >= 2 && row[0] instanceof Integer cid && row[1] instanceof Number cnt) {
                studentCounts.put(cid, cnt.longValue());
            }
        }

        Map<Integer, Long> netByCourse = earningEntryRepository.findByInstructorId(userId).stream()
                .collect(Collectors.groupingBy(
                        EarningEntry::getCourseId,
                        Collectors.summingLong(e -> e.getAmount() != null ? e.getAmount() : 0L)
                ));

        List<InstructorCourseDto> result = new ArrayList<>();
        for (Course c : allCoursesMap.values()) {
            Integer cid = c.getCourseId();
            boolean isMain = Objects.equals(c.getInstructorId(), userId);
            String role = isMain ? "MAIN" : "CO";
            int shareBps;
            if (isMain) {
                int coSum = courseCoInstructorRepository.findByCourseIdAndStatus(cid, "ACCEPTED").stream()
                        .mapToInt(ci -> ci.getShareBps() != null ? ci.getShareBps() : 0)
                        .sum();
                shareBps = Math.max(0, 10000 - coSum);
            } else {
                shareBps = coShareMap.getOrDefault(cid, 0);
            }

            long rCount = ratingCounts.getOrDefault(cid, 0L);
            Double avg = rCount > 0 ? c.getAverageRating() : null;
            result.add(new InstructorCourseDto(
                    cid,
                    c.getTitle(),
                    c.getThumbnailUrl(),
                    c.getStatus(),
                    role,
                    shareBps,
                    studentCounts.getOrDefault(cid, 0L),
                    avg,
                    rCount,
                    netByCourse.getOrDefault(cid, 0L)
            ));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/courses/{courseId}/students")
    public ResponseEntity<PageResponse<StudentRosterItemDto>> getCourseStudents(
            @PathVariable Integer courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học."));

        boolean isMain = Objects.equals(course.getInstructorId(), userId);
        boolean isCo = courseCoInstructorRepository.findFirstByCourseIdAndInstructorId(courseId, userId)
                .map(ci -> "ACCEPTED".equalsIgnoreCase(ci.getStatus()))
                .orElse(false);
        if (!isMain && !isCo && !SecurityUtils.hasRole("Admin")) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Bạn không có quyền quản lý khóa học này.");
        }

        int safePage = Math.max(0, page);
        int safeSize = Math.min(100, Math.max(1, size));
        Page<Enrollment> enrollmentPage = enrollmentRepository.findByCourseIdOrderByEnrolledAtDesc(
                courseId, PageRequest.of(safePage, safeSize));

        Page<StudentRosterItemDto> mapped = enrollmentPage.map(e -> new StudentRosterItemDto(
                e.getEnrollmentId(),
                e.getUserId(),
                e.getUser() != null ? e.getUser().getFullName() : "Học viên #" + e.getUserId(),
                TimeUtils.toInstant(e.getEnrolledAt(), timeZone),
                learningProgressService != null
                        ? learningProgressService.getProgress(e.getUserId(), courseId).progressPercentage()
                        : e.getProgressPercentage(),
                e.getStatus()
        ));
        return ResponseEntity.ok(PageResponse.from(mapped));
    }

    static long computeGrossSales(List<EarningEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return 0L;
        }
        return entries.stream()
                .mapToLong(e -> {
                    long unitPrice = e.getUnitPrice() != null ? e.getUnitPrice() : 0L;
                    int shareBps = (e.getShareBps() != null && e.getShareBps() > 0) ? e.getShareBps() : 10000;
                    if (unitPrice > 0) {
                        return (unitPrice * shareBps) / 10000;
                    }
                    long amt = e.getAmount() != null ? e.getAmount() : 0L;
                    int feeBps = (e.getPlatformFeeBps() != null && e.getPlatformFeeBps() < 10000) ? e.getPlatformFeeBps() : 3000;
                    return (amt * 10000L) / (10000 - feeBps);
                })
                .sum();
    }

    private Map<Integer, Long> loadRatingCounts(List<Integer> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) return Collections.emptyMap();
        Map<Integer, Long> map = new HashMap<>();
        for (Object[] row : reviewRepository.countByCourseIds(courseIds)) {
            if (row != null && row.length >= 2 && row[0] instanceof Integer cid && row[1] instanceof Number cnt) {
                map.put(cid, cnt.longValue());
            }
        }
        return map;
    }
}
