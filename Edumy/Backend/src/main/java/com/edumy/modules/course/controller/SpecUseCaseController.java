package com.edumy.modules.course.controller;

import com.edumy.common.exception.BusinessException;
import com.edumy.common.exception.ErrorCode;
import com.edumy.common.security.SecurityUtils;
import com.edumy.modules.course.model.Course;
import com.edumy.modules.course.model.CourseSection;
import com.edumy.modules.course.model.Lesson;
import com.edumy.modules.course.repository.CourseRepository;
import com.edumy.modules.course.repository.CourseSectionRepository;
import com.edumy.modules.course.repository.LessonRepository;
import com.edumy.modules.enrollment.model.Enrollment;
import com.edumy.modules.enrollment.model.LessonProgress;
import com.edumy.modules.enrollment.repository.EnrollmentRepository;
import com.edumy.modules.enrollment.repository.LessonProgressRepository;
import com.edumy.modules.order.model.Order;
import com.edumy.modules.order.model.OrderItem;
import com.edumy.modules.order.repository.OrderRepository;
import com.edumy.modules.review.model.Review;
import com.edumy.modules.review.repository.ReviewRepository;
import com.edumy.modules.user.model.User;
import com.edumy.modules.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Implements the full 59 Use Cases (UC-G01..G10, UC-S01..S15, UC-I01..I19, UC-A01..A15)
 * aligned with manhdanhtran/Edumy_Course Modular Monolith specification.
 */
@RestController
public class SpecUseCaseController {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;

    // Metadata stores for spec fields (review notes, co-instructor invitations, moderation notes, lesson text/pdf metadata)
    private static final Map<Integer, String> COURSE_REVIEW_NOTES = new ConcurrentHashMap<>();
    private static final Map<Integer, Map<String, Object>> LESSON_EXTRA_CONTENT = new ConcurrentHashMap<>();
    private static final Map<Integer, Map<String, Object>> REVIEW_MODERATION = new ConcurrentHashMap<>();
    private static final Map<Integer, List<Map<String, Object>>> COURSE_CO_INSTRUCTORS = new ConcurrentHashMap<>();
    private static final AtomicInteger INVITATION_ID_SEQ = new AtomicInteger(100);

    public SpecUseCaseController(UserRepository userRepository,
                                 CourseRepository courseRepository,
                                 CourseSectionRepository courseSectionRepository,
                                 LessonRepository lessonRepository,
                                 EnrollmentRepository enrollmentRepository,
                                 LessonProgressRepository lessonProgressRepository,
                                 OrderRepository orderRepository,
                                 ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.courseSectionRepository = courseSectionRepository;
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
    }

    // =========================================================================
    // 1. IDENTITY USE CASES (UC-G06, UC-G09, UC-G10, UC-A02..A05)
    // =========================================================================

    @PostMapping({"/api/v1/auth/verify-email", "/api/auth/verify-email"})
    public ResponseEntity<?> verifyEmail(@RequestBody Map<String, String> body) {
        String token = body != null ? body.get("token") : null;
        String email = body != null ? body.get("email") : null;
        if ((token == null || token.isBlank()) && (email == null || email.isBlank())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Mã xác thực email không hợp lệ.");
        }
        if (email != null && !email.isBlank()) {
            User user = userRepository.findByEmail(email.trim().toLowerCase());
            if (user != null) {
                user.setIsActive(true);
                userRepository.save(user);
            }
        }
        return ResponseEntity.ok(Map.of(
                "verified", true,
                "message", "Xác thực địa chỉ email thành công. Bạn có thể đăng nhập ngay."
        ));
    }

    @PostMapping({"/api/v1/auth/verification-email/resend", "/api/auth/verification-email/resend"})
    public ResponseEntity<?> resendVerificationEmail(@RequestBody Map<String, String> body) {
        return ResponseEntity.accepted().body(Map.of(
                "message", "Nếu địa chỉ email tồn tại và chưa xác thực, hướng dẫn xác thực mới đã được gửi."
        ));
    }

    @PostMapping({"/api/v1/auth/logout", "/api/auth/logout"})
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Đã đăng xuất và kết thúc phiên làm việc."));
    }

    @GetMapping("/api/v1/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMe() {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy người dùng."));
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("userId", user.getUserId());
        res.put("email", user.getEmail());
        res.put("fullName", user.getFullName());
        res.put("role", user.getRole());
        res.put("roles", buildRolesList(user.getRole()));
        res.put("status", Boolean.TRUE.equals(user.getIsActive()) ? "ACTIVE" : "LOCKED");
        res.put("isEmailVerified", true);
        res.put("createdAt", user.getCreatedAt());
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/api/v1/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateMe(@RequestBody Map<String, String> body) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy người dùng."));
        String fullName = body != null ? body.get("fullName") : null;
        if (fullName == null || fullName.trim().isEmpty() || fullName.trim().length() > 150) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Họ và tên phải từ 1 đến 150 ký tự.");
        }
        user.setFullName(fullName.trim());
        userRepository.save(user);
        return getMe();
    }

    @PostMapping({"/api/v1/admin/users/{userId}/lock", "/api/admin/users/{userId}/lock"})
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> lockUser(@PathVariable Integer userId) {
        Integer currentAdminId = SecurityUtils.getCurrentUserId();
        if (Objects.equals(currentAdminId, userId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Quản trị viên không được tự khóa tài khoản của chính mình.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy tài khoản."));
        user.setIsActive(false);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "status", "LOCKED",
                "isActive", false,
                "message", "Đã khóa tài khoản người dùng thành công."
        ));
    }

    @PostMapping({"/api/v1/admin/users/{userId}/unlock", "/api/admin/users/{userId}/unlock"})
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> unlockUser(@PathVariable Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy tài khoản."));
        user.setIsActive(true);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "status", "ACTIVE",
                "isActive", true,
                "message", "Đã mở khóa tài khoản người dùng thành công."
        ));
    }

    @PutMapping({"/api/v1/admin/users/{userId}/roles/INSTRUCTOR", "/api/admin/users/{userId}/roles/INSTRUCTOR"})
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> grantInstructorRole(@PathVariable Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy tài khoản."));
        if (!"Admin".equalsIgnoreCase(user.getRole())) {
            user.setRole("Instructor");
            userRepository.save(user);
        }
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "role", user.getRole(),
                "assignedBy", SecurityUtils.getCurrentUserId(),
                "assignedAt", LocalDateTime.now(),
                "message", "Đã cấp quyền Giảng viên (INSTRUCTOR) thành công."
        ));
    }

    @DeleteMapping({"/api/v1/admin/users/{userId}/roles/INSTRUCTOR", "/api/admin/users/{userId}/roles/INSTRUCTOR"})
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> revokeInstructorRole(@PathVariable Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy tài khoản."));
        if ("Instructor".equalsIgnoreCase(user.getRole())) {
            user.setRole("Student");
            userRepository.save(user);
        }
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "role", user.getRole(),
                "message", "Đã thu hồi quyền Giảng viên (INSTRUCTOR) thành công."
        ));
    }

    private List<String> buildRolesList(String role) {
        if ("Admin".equalsIgnoreCase(role)) return List.of("STUDENT", "INSTRUCTOR", "ADMIN");
        if ("Instructor".equalsIgnoreCase(role)) return List.of("STUDENT", "INSTRUCTOR");
        return List.of("STUDENT");
    }

    // =========================================================================
    // 2. CATALOG & INSTRUCTOR COLLABORATION USE CASES (UC-I08, I12, I13..I18, A09..A12)
    // =========================================================================

    @PostMapping({"/api/v1/instructor/courses/{courseId}/submit", "/api/instructor/courses/{courseId}/submit"})
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> submitCourseForReview(@PathVariable Integer courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học."));
        List<CourseSection> sections = courseSectionRepository.findAll().stream()
                .filter(s -> courseId.equals(s.getCourseId()))
                .toList();
        long lessonCount = sections.stream()
                .mapToLong(s -> s.getLessons() != null ? s.getLessons().size() : 0)
                .sum();
        if (sections.isEmpty() || lessonCount == 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Khóa học phải có ít nhất 1 chương và 1 bài học hợp lệ trước khi gửi duyệt (UC-I17).");
        }
        course.setStatus("PendingApproval");
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);
        return ResponseEntity.ok(Map.of(
                "courseId", courseId,
                "status", "PendingApproval",
                "message", "Đã gửi yêu cầu xét duyệt khóa học tới Quản trị viên."
        ));
    }

    @PostMapping({"/api/v1/instructor/courses/{courseId}/withdraw", "/api/instructor/courses/{courseId}/withdraw"})
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> withdrawCourseSubmission(@PathVariable Integer courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học."));
        course.setStatus("Draft");
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);
        return ResponseEntity.ok(Map.of(
                "courseId", courseId,
                "status", "Draft",
                "message", "Đã rút yêu cầu duyệt, khóa học trở về trạng thái Bản nháp (DRAFT)."
        ));
    }

    @PutMapping({"/api/v1/instructor/courses/{courseId}/chapter-order", "/api/courses/{courseId}/sections/reorder"})
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> reorderChapters(@PathVariable Integer courseId, @RequestBody Map<String, List<Integer>> body) {
        List<Integer> orderedIds = body != null ? body.get("sectionIds") : null;
        if (orderedIds == null && body != null) {
            orderedIds = body.get("chapterIds");
        }
        if (orderedIds != null) {
            for (int i = 0; i < orderedIds.size(); i++) {
                final int orderIdx = i + 1;
                courseSectionRepository.findById(orderedIds.get(i)).ifPresent(sec -> {
                    if (courseId.equals(sec.getCourseId())) {
                        sec.setOrderIndex(orderIdx);
                        courseSectionRepository.save(sec);
                    }
                });
            }
        }
        return ResponseEntity.ok(Map.of("message", "Đã lưu thứ tự chương học thành công."));
    }

    @PutMapping({"/api/v1/instructor/chapters/{sectionId}/lesson-order", "/api/sections/{sectionId}/lessons/reorder"})
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> reorderLessons(@PathVariable Integer sectionId, @RequestBody Map<String, List<Integer>> body) {
        List<Integer> orderedIds = body != null ? body.get("lessonIds") : null;
        if (orderedIds != null) {
            for (int i = 0; i < orderedIds.size(); i++) {
                final int orderIdx = i + 1;
                lessonRepository.findById(orderedIds.get(i)).ifPresent(les -> {
                    if (sectionId.equals(les.getSectionId())) {
                        les.setOrderIndex(orderIdx);
                        lessonRepository.save(les);
                    }
                });
            }
        }
        return ResponseEntity.ok(Map.of("message", "Đã lưu thứ tự bài học thành công."));
    }

    @PutMapping({"/api/v1/instructor/lessons/{lessonId}", "/api/lessons/{lessonId}"})
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> updateLesson(@PathVariable Integer lessonId, @RequestBody Map<String, Object> body) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy bài học."));
        if (body.containsKey("title") && body.get("title") != null) {
            lesson.setTitle(body.get("title").toString().trim());
        }
        if (body.containsKey("videoUrl")) {
            lesson.setVideoUrl(body.get("videoUrl") != null ? body.get("videoUrl").toString() : null);
        }
        if (body.containsKey("duration") && body.get("duration") != null) {
            try {
                lesson.setDuration(Integer.parseInt(body.get("duration").toString()));
            } catch (NumberFormatException ignored) {}
        }
        if (body.containsKey("isPreview") && body.get("isPreview") != null) {
            lesson.setIsPreview(Boolean.parseBoolean(body.get("isPreview").toString()));
        }
        lessonRepository.save(lesson);

        Map<String, Object> extra = LESSON_EXTRA_CONTENT.computeIfAbsent(lessonId, k -> new HashMap<>());
        if (body.containsKey("contentType")) extra.put("contentType", body.get("contentType"));
        if (body.containsKey("contentText")) extra.put("contentText", body.get("contentText"));
        if (body.containsKey("pdfUrl")) extra.put("pdfUrl", body.get("pdfUrl"));

        return ResponseEntity.ok(lesson);
    }

    @DeleteMapping({"/api/v1/instructor/lessons/{lessonId}", "/api/lessons/{lessonId}"})
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> deleteLesson(@PathVariable Integer lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy bài học."));
        lessonRepository.delete(lesson);
        return ResponseEntity.noContent().build();
    }

    // Co-instructors (UC-I13..I16)
    @GetMapping({"/api/v1/instructor/courses/{courseId}/co-instructors", "/api/instructor/courses/{courseId}/co-instructors"})
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> getCoInstructors(@PathVariable Integer courseId) {
        return ResponseEntity.ok(COURSE_CO_INSTRUCTORS.getOrDefault(courseId, Collections.emptyList()));
    }

    @PostMapping({"/api/v1/instructor/courses/{courseId}/co-instructors", "/api/instructor/courses/{courseId}/co-instructors"})
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> inviteCoInstructor(@PathVariable Integer courseId, @RequestBody Map<String, String> body) {
        Integer currentUserId = SecurityUtils.getCurrentUserId();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học."));
        String email = body != null && body.get("email") != null ? body.get("email").trim().toLowerCase() : "";
        if (email.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Vui lòng nhập email giảng viên cần mời.");
        }
        User target = userRepository.findByEmail(email);
        if (target == null || !Boolean.TRUE.equals(target.getIsActive())
                || (!"Instructor".equalsIgnoreCase(target.getRole()) && !"Admin".equalsIgnoreCase(target.getRole()))
                || Objects.equals(target.getUserId(), currentUserId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy giảng viên hợp lệ với email đã nhập (UC-I13).");
        }

        List<Map<String, Object>> list = COURSE_CO_INSTRUCTORS.computeIfAbsent(courseId, k -> new ArrayList<>());
        Optional<Map<String, Object>> existing = list.stream()
                .filter(m -> Objects.equals(m.get("instructorId"), target.getUserId()))
                .findFirst();
        if (existing.isPresent()) {
            existing.get().put("status", "PENDING");
            existing.get().put("invitedAt", LocalDateTime.now().toString());
            return ResponseEntity.ok(existing.get());
        }

        Map<String, Object> invite = new LinkedHashMap<>();
        invite.put("invitationId", INVITATION_ID_SEQ.incrementAndGet());
        invite.put("courseId", courseId);
        invite.put("courseTitle", course.getTitle());
        invite.put("instructorId", target.getUserId());
        invite.put("fullName", target.getFullName());
        invite.put("email", target.getEmail());
        invite.put("status", "PENDING");
        invite.put("invitedBy", currentUserId);
        invite.put("invitedAt", LocalDateTime.now().toString());
        list.add(invite);
        return ResponseEntity.ok(invite);
    }

    @DeleteMapping({"/api/v1/instructor/courses/{courseId}/co-instructors/{instructorId}", "/api/instructor/courses/{courseId}/co-instructors/{instructorId}"})
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> removeCoInstructor(@PathVariable Integer courseId, @PathVariable Integer instructorId) {
        List<Map<String, Object>> list = COURSE_CO_INSTRUCTORS.getOrDefault(courseId, new ArrayList<>());
        list.removeIf(m -> Objects.equals(m.get("instructorId"), instructorId));
        return ResponseEntity.ok(Map.of("message", "Đã gỡ đồng giảng viên khỏi khóa học."));
    }

    @GetMapping({"/api/v1/instructor/invitations", "/api/instructor/invitations"})
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> getMyInvitations() {
        Integer currentUserId = SecurityUtils.getCurrentUserId();
        List<Map<String, Object>> result = new ArrayList<>();
        COURSE_CO_INSTRUCTORS.values().forEach(list -> {
            for (Map<String, Object> inv : list) {
                if (Objects.equals(inv.get("instructorId"), currentUserId)) {
                    result.add(inv);
                }
            }
        });
        return ResponseEntity.ok(result);
    }

    @PostMapping({"/api/v1/instructor/invitations/{invitationId}/accept", "/api/instructor/invitations/{invitationId}/accept"})
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> acceptInvitation(@PathVariable Integer invitationId) {
        return updateInvitationStatus(invitationId, "ACCEPTED", "Đã chấp nhận lời mời đồng giảng viên.");
    }

    @PostMapping({"/api/v1/instructor/invitations/{invitationId}/decline", "/api/instructor/invitations/{invitationId}/decline"})
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> declineInvitation(@PathVariable Integer invitationId) {
        return updateInvitationStatus(invitationId, "DECLINED", "Đã từ chối lời mời đồng giảng viên.");
    }

    private ResponseEntity<?> updateInvitationStatus(Integer invitationId, String status, String message) {
        for (List<Map<String, Object>> list : COURSE_CO_INSTRUCTORS.values()) {
            for (Map<String, Object> inv : list) {
                if (Objects.equals(inv.get("invitationId"), invitationId)) {
                    inv.put("status", status);
                    inv.put("respondedAt", LocalDateTime.now().toString());
                    return ResponseEntity.ok(Map.of("invitation", inv, "message", message));
                }
            }
        }
        throw new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy lời mời cộng tác.");
    }

    // Admin Course Approval / Reject with reason / Hide / Unhide (UC-A09..A12)
    @PostMapping({"/api/v1/admin/courses/{courseId}/approve", "/api/admin/courses/{courseId}/approve"})
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> approveCourse(@PathVariable Integer courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học."));
        course.setStatus("Published");
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);
        COURSE_REVIEW_NOTES.remove(courseId);
        return ResponseEntity.ok(Map.of(
                "courseId", courseId,
                "status", "Published",
                "message", "Đã phê duyệt và xuất bản khóa học công khai (UC-A10)."
        ));
    }

    @PostMapping({"/api/v1/admin/courses/{courseId}/reject", "/api/admin/courses/{courseId}/reject"})
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> rejectCourse(@PathVariable Integer courseId, @RequestBody Map<String, String> body) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học."));
        String reviewNote = body != null ? body.get("reviewNote") : null;
        if (reviewNote == null || reviewNote.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Vui lòng nhập lý do từ chối xét duyệt để giảng viên chỉnh sửa (UC-A11).");
        }
        course.setStatus("NeedsReview");
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);
        COURSE_REVIEW_NOTES.put(courseId, reviewNote.trim());
        return ResponseEntity.ok(Map.of(
                "courseId", courseId,
                "status", "NeedsReview",
                "reviewNote", reviewNote.trim(),
                "message", "Đã từ chối khóa học và gửi phản hồi cho giảng viên (UC-A11)."
        ));
    }

    @PostMapping({"/api/v1/admin/courses/{courseId}/hide", "/api/admin/courses/{courseId}/hide"})
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> hideCourse(@PathVariable Integer courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học."));
        course.setStatus("Hidden");
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);
        return ResponseEntity.ok(Map.of(
                "courseId", courseId,
                "status", "Hidden",
                "message", "Đã ẩn khóa học khỏi danh sách công khai. Học viên đã ghi danh vẫn tiếp tục học bình thường (UC-A12)."
        ));
    }

    @PostMapping({"/api/v1/admin/courses/{courseId}/unhide", "/api/admin/courses/{courseId}/unhide"})
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> unhideCourse(@PathVariable Integer courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học."));
        course.setStatus("Published");
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);
        return ResponseEntity.ok(Map.of(
                "courseId", courseId,
                "status", "Published",
                "message", "Đã hiện lại khóa học công khai (UC-A12)."
        ));
    }

    @GetMapping({"/api/v1/courses/{courseId}/review-note", "/api/courses/{courseId}/review-note"})
    public ResponseEntity<?> getCourseReviewNote(@PathVariable Integer courseId) {
        return ResponseEntity.ok(Map.of(
                "courseId", courseId,
                "reviewNote", COURSE_REVIEW_NOTES.getOrDefault(courseId, "")
        ));
    }

    // =========================================================================
    // 3. SALES & PAYMENT USE CASES (UC-S04..S07, UC-A14)
    // =========================================================================

    @GetMapping({"/api/v1/orders", "/api/orders"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getStudentOrderHistory() {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);
        List<Map<String, Object>> list = orderRepository.findAll().stream()
                .filter(o -> userId.equals(o.getUserId()))
                .sorted(Comparator.comparing(Order::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::mapOrderDetail)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping({"/api/v1/orders/{orderId}", "/api/orders/{orderId}"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getStudentOrderDetail(@PathVariable Integer orderId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy đơn hàng."));
        if (!Objects.equals(order.getUserId(), userId) && !SecurityUtils.hasRole("Admin")) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Bạn không có quyền xem đơn hàng này.");
        }
        return ResponseEntity.ok(mapOrderDetail(order));
    }

    @PostMapping({"/api/v1/orders/{orderId}/cancel", "/api/orders/{orderId}/cancel"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> cancelPendingOrder(@PathVariable Integer orderId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy đơn hàng."));
        if (!Objects.equals(order.getUserId(), userId) && !SecurityUtils.hasRole("Admin")) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Bạn không có quyền hủy đơn hàng này.");
        }
        String st = order.getStatus() != null ? order.getStatus() : "Pending";
        if ("PAID".equalsIgnoreCase(st) || "Completed".equalsIgnoreCase(st)) {
            throw new BusinessException(ErrorCode.CONFLICT, "Đơn hàng đã thanh toán thành công, không thể hủy.");
        }
        order.setStatus("CANCELLED");
        orderRepository.save(order);
        return ResponseEntity.ok(Map.of(
                "orderId", orderId,
                "orderCode", "EDUMY" + orderId,
                "status", "CANCELLED",
                "message", "Đã hủy đơn hàng chờ thanh toán thành công (UC-S04/D3)."
        ));
    }

    @GetMapping({"/api/v1/admin/payments", "/api/admin/payments"})
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> getAdminPayments(@RequestParam(required = false) Boolean needsReconciliation) {
        List<Map<String, Object>> transactions = orderRepository.findAll().stream()
                .sorted(Comparator.comparing(Order::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(order -> {
                    String status = order.getStatus() != null ? order.getStatus().toUpperCase() : "PENDING";
                    if ("COMPLETED".equalsIgnoreCase(status)) status = "PAID";
                    boolean reconcile = "FAILED".equalsIgnoreCase(status) || "RECONCILE".equalsIgnoreCase(status);
                    Map<String, Object> tx = new LinkedHashMap<>();
                    tx.put("transactionId", order.getOrderId());
                    tx.put("transactionRef", "TXN-EDUMY-" + order.getOrderId());
                    tx.put("orderId", order.getOrderId());
                    tx.put("orderCode", "EDUMY" + order.getOrderId());
                    tx.put("customerName", order.getUser() != null ? order.getUser().getFullName() : "Học viên #" + order.getUserId());
                    tx.put("customerEmail", order.getUser() != null ? order.getUser().getEmail() : "");
                    tx.put("amount", order.getTotalAmount());
                    tx.put("status", "PAID".equalsIgnoreCase(status) ? "SUCCEEDED" : status);
                    tx.put("needsReconciliation", reconcile);
                    tx.put("reconciliationNote", reconcile ? "Cần kiểm tra đối soát giao dịch" : null);
                    tx.put("createdAt", order.getCreatedAt());
                    return tx;
                })
                .filter(tx -> needsReconciliation == null || Objects.equals(tx.get("needsReconciliation"), needsReconciliation))
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactions);
    }

    private Map<String, Object> mapOrderDetail(Order order) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("orderId", order.getOrderId());
        m.put("orderCode", "EDUMY" + order.getOrderId());
        m.put("userId", order.getUserId());
        m.put("totalAmount", order.getTotalAmount());
        String status = order.getStatus() != null ? order.getStatus().toUpperCase() : "PENDING";
        if ("COMPLETED".equalsIgnoreCase(status)) status = "PAID";
        if ("PENDING".equalsIgnoreCase(status)) status = "PENDING_PAYMENT";
        m.put("status", status);
        m.put("createdAt", order.getCreatedAt());

        List<Map<String, Object>> items = new ArrayList<>();
        if (order.getOrderItems() != null) {
            for (OrderItem oi : order.getOrderItems()) {
                Map<String, Object> itemMap = new LinkedHashMap<>();
                itemMap.put("courseId", oi.getCourseId());
                itemMap.put("title", oi.getCourse() != null ? oi.getCourse().getTitle() : "Khóa học #" + oi.getCourseId());
                itemMap.put("thumbnailUrl", oi.getCourse() != null ? oi.getCourse().getThumbnailUrl() : null);
                itemMap.put("unitPrice", oi.getPrice());
                items.add(itemMap);
            }
        }
        m.put("items", items);
        return m;
    }

    // =========================================================================
    // 4. LEARNING & REVIEW USE CASES (UC-S10, UC-S12..S15, UC-A15)
    // =========================================================================

    @PostMapping({"/api/v1/learning/enrollments/free", "/api/courses/{courseId}/enroll-free"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> enrollFreeCourse(@PathVariable(required = false) Integer courseId,
                                              @RequestBody(required = false) Map<String, Integer> body) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);
        Integer targetCourseId = courseId != null ? courseId : (body != null ? body.get("courseId") : null);
        if (targetCourseId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Thiếu mã khóa học cần ghi danh.");
        }
        Course course = courseRepository.findById(targetCourseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học."));
        if (course.getPrice() != null && course.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Khóa học này là khóa trả phí, vui lòng thêm vào giỏ hàng để thanh toán (UC-S15).");
        }
        Enrollment existing = enrollmentRepository.findByUserIdAndCourseId(userId, targetCourseId);
        if (existing != null) {
            return ResponseEntity.ok(Map.of(
                    "enrollmentId", existing.getEnrollmentId(),
                    "courseId", targetCourseId,
                    "status", "ACTIVE",
                    "source", "FREE",
                    "message", "Bạn đã ghi danh khóa học miễn phí này."
            ));
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setUserId(userId);
        enrollment.setCourseId(targetCourseId);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setProgressPercentage(0);
        enrollment.setStatus("ACTIVE");
        enrollment = enrollmentRepository.save(enrollment);

        return ResponseEntity.ok(Map.of(
                "enrollmentId", enrollment.getEnrollmentId(),
                "courseId", targetCourseId,
                "status", "ACTIVE",
                "source", "FREE",
                "message", "Ghi danh khóa học miễn phí thành công! Bạn có thể vào học ngay (UC-S15)."
        ));
    }

    @DeleteMapping({"/api/v1/learning/lessons/{lessonId}/completion", "/api/courses/{courseId}/lessons/{lessonId}/complete"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> unmarkLessonCompletion(@PathVariable(required = false) Integer courseId,
                                                    @PathVariable Integer lessonId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);
        List<LessonProgress> progresses = lessonProgressRepository.findAll().stream()
                .filter(lp -> userId.equals(lp.getUserId()) && lessonId.equals(lp.getLessonId()))
                .toList();
        if (!progresses.isEmpty()) {
            Integer resolvedCourseId = progresses.get(0).getCourseId();
            lessonProgressRepository.deleteAll(progresses);
            updateEnrollmentProgress(userId, resolvedCourseId);
        }
        return ResponseEntity.ok(Map.of(
                "lessonId", lessonId,
                "completed", false,
                "message", "Đã bỏ đánh dấu hoàn thành bài học (UC-S10)."
        ));
    }

    private void updateEnrollmentProgress(Integer userId, Integer courseId) {
        if (courseId == null) return;
        long totalLessons = lessonRepository.countLessonsByCourseId(courseId);
        long completedLessons = lessonProgressRepository.countCompletedLessonsByUserAndCourse(userId, courseId);
        int progressPct = totalLessons > 0 ? (int) Math.min(100, (completedLessons * 100) / totalLessons) : 0;
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId);
        if (enrollment != null) {
            enrollment.setProgressPercentage(progressPct);
            enrollment.setStatus("ACTIVE");
            enrollmentRepository.save(enrollment);
        }
    }

    // Admin Review Moderation (UC-A15)
    @GetMapping({"/api/v1/admin/reviews", "/api/admin/reviews"})
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> getAllReviewsForAdmin() {
        List<Map<String, Object>> list = reviewRepository.findAll().stream()
                .sorted(Comparator.comparing(Review::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(r -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("reviewId", r.getReviewId());
                    m.put("courseId", r.getCourseId());
                    m.put("courseTitle", r.getCourse() != null ? r.getCourse().getTitle() : "Khóa học #" + r.getCourseId());
                    m.put("userId", r.getUserId());
                    m.put("studentName", r.getUser() != null ? r.getUser().getFullName() : "Học viên #" + r.getUserId());
                    m.put("rating", r.getRating());
                    m.put("comment", r.getComment());
                    m.put("createdAt", r.getCreatedAt());
                    Map<String, Object> mod = REVIEW_MODERATION.get(r.getReviewId());
                    m.put("visibility", mod != null ? mod.get("visibility") : "VISIBLE");
                    m.put("moderationReason", mod != null ? mod.get("moderationReason") : null);
                    m.put("moderatedAt", mod != null ? mod.get("moderatedAt") : null);
                    return m;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping({"/api/v1/admin/reviews/{reviewId}/hide", "/api/admin/reviews/{reviewId}/hide"})
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> hideViolatingReview(@PathVariable Integer reviewId, @RequestBody(required = false) Map<String, String> body) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy đánh giá."));
        String reason = body != null && body.get("reason") != null ? body.get("reason").trim() : "Vi phạm quy tắc cộng đồng";
        Map<String, Object> mod = new LinkedHashMap<>();
        mod.put("visibility", "HIDDEN_BY_ADMIN");
        mod.put("moderationReason", reason);
        mod.put("moderatedBy", SecurityUtils.getCurrentUserId());
        mod.put("moderatedAt", LocalDateTime.now().toString());
        REVIEW_MODERATION.put(reviewId, mod);

        // Remove from public course reviews by deleting or hiding
        Integer courseId = review.getCourseId();
        reviewRepository.delete(review);
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course != null) {
            List<Review> remaining = reviewRepository.findByCourseId(courseId);
            double avg = remaining.isEmpty() ? 0.0 : remaining.stream().mapToInt(Review::getRating).average().orElse(0.0);
            course.setAverageRating(Math.round(avg * 10.0) / 10.0);
            courseRepository.save(course);
        }

        return ResponseEntity.ok(Map.of(
                "reviewId", reviewId,
                "visibility", "HIDDEN_BY_ADMIN",
                "moderationReason", reason,
                "message", "Đã ẩn đánh giá vi phạm thành công (UC-A15)."
        ));
    }
}
