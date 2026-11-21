package com.edumy.authoring.controller;

import com.edumy.shared.exception.BusinessException;
import com.edumy.shared.exception.ErrorCode;
import com.edumy.shared.security.SecurityUtils;
import com.edumy.authoring.model.Course;
import com.edumy.authoring.model.CourseCoInstructor;
import com.edumy.authoring.model.CourseSection;
import com.edumy.authoring.model.Lesson;
import com.edumy.authoring.repository.CourseCoInstructorRepository;
import com.edumy.authoring.repository.CourseRepository;
import com.edumy.authoring.repository.CourseSectionRepository;
import com.edumy.authoring.repository.LessonRepository;
import com.edumy.learning.enrollment.model.Enrollment;
import com.edumy.learning.enrollment.model.LessonProgress;
import com.edumy.learning.enrollment.repository.EnrollmentRepository;
import com.edumy.learning.enrollment.repository.LessonProgressRepository;
import com.edumy.commerce.order.model.Order;
import com.edumy.commerce.order.model.OrderItem;
import com.edumy.commerce.order.repository.OrderRepository;
import com.edumy.learning.review.model.Review;
import com.edumy.learning.review.repository.ReviewRepository;
import com.edumy.identity.user.model.User;
import com.edumy.identity.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implements the remaining Use Cases (Admin moderation, Co-instructors, Orders, Free enrollment).
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
    private final com.edumy.notification.service.MailService mailService;

    @org.springframework.beans.factory.annotation.Autowired
    private com.edumy.learning.enrollment.service.LearningProgressService learningProgressService;

    @org.springframework.beans.factory.annotation.Autowired
    private CourseCoInstructorRepository courseCoInstructorRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.edumy.authoring.repository.CategoryRepository categoryRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.edumy.learning.enrollment.repository.CertificateRepository certificateRepository;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.edumy.identity.auth.repository.RefreshTokenRepository refreshTokenRepository;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.edumy.commerce.order.repository.OrderItemRepository orderItemRepository;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.edumy.payout.api.EarningApi earningApi;

    public SpecUseCaseController(UserRepository userRepository,
                                 CourseRepository courseRepository,
                                 CourseSectionRepository courseSectionRepository,
                                 LessonRepository lessonRepository,
                                 EnrollmentRepository enrollmentRepository,
                                 LessonProgressRepository lessonProgressRepository,
                                 OrderRepository orderRepository,
                                 ReviewRepository reviewRepository,
                                 com.edumy.notification.service.MailService mailService) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.courseSectionRepository = courseSectionRepository;
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.mailService = mailService;
    }

    // =========================================================================
    // 1. IDENTITY USE CASES (UC-G06, UC-G09, UC-G10, UC-A02..A05)
    // =========================================================================

    @PostMapping({"/api/v1/auth/logout", "/api/auth/logout"})
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Đã đăng xuất và kết thúc phiên làm việc."));
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
        if ("Admin".equalsIgnoreCase(user.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Không được phép khóa tài khoản Quản trị viên khác.");
        }
        user.setIsActive(false);
        userRepository.save(user);

        if (refreshTokenRepository != null) {
            var tokens = refreshTokenRepository.findByUserId(userId);
            LocalDateTime now = LocalDateTime.now();
            for (var t : tokens) {
                if (t.isActive()) {
                    t.setRevoked(now);
                }
            }
            refreshTokenRepository.saveAll(tokens);
        }

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
            user.setInstructorRevoked(false);
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
            user.setInstructorRevoked(true);
            userRepository.save(user);
        } else if (!"Admin".equalsIgnoreCase(user.getRole())) {
            user.setInstructorRevoked(true);
            userRepository.save(user);
        }
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "role", user.getRole(),
                "message", "Đã thu hồi quyền Giảng viên (INSTRUCTOR) thành công."
        ));
    }

    private void requireCourseOwnerOrAdmin(Course course) {
        Integer currentUserId = SecurityUtils.getCurrentUserId();
        if (SecurityUtils.hasRole("Admin")) return;
        if (currentUserId == null || !Objects.equals(course.getInstructorId(), currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này trên khóa học của giảng viên khác.");
        }
    }

    private void requireCourseEditorOrAdmin(Course course) {
        Integer currentUserId = SecurityUtils.getCurrentUserId();
        if (SecurityUtils.hasRole("Admin")) return;
        if (currentUserId != null && Objects.equals(course.getInstructorId(), currentUserId)) return;
        if (currentUserId != null && courseCoInstructorRepository != null) {
            boolean isCo = courseCoInstructorRepository.findFirstByCourseIdAndInstructorId(course.getCourseId(), currentUserId)
                    .map(ci -> "ACCEPTED".equalsIgnoreCase(ci.getStatus()))
                    .orElse(false);
            if (isCo) return;
        }
        throw new BusinessException(ErrorCode.FORBIDDEN, "Bạn không có quyền chỉnh sửa khóa học này.");
    }

    private List<String> buildRolesList(String role) {
        if ("Admin".equalsIgnoreCase(role)) return List.of("STUDENT", "INSTRUCTOR", "ADMIN");
        if ("Instructor".equalsIgnoreCase(role)) return List.of("STUDENT", "INSTRUCTOR");
        return List.of("STUDENT");
    }

    // =========================================================================
    // 2. CATALOG & INSTRUCTOR COLLABORATION USE CASES (UC-I08, I12, I13..I18, A09..A12)
    // =========================================================================

    @PostMapping({"/api/v1/instructor/courses/{courseId}/submit", "/api/instructor/courses/{courseId}/submit", "/api/courses/{courseId}/submit-review"})
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> submitCourseForReview(@PathVariable Integer courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học."));
        requireCourseOwnerOrAdmin(course);

        String st = course.getStatus() != null ? course.getStatus() : "Draft";
        if (!"Draft".equalsIgnoreCase(st) && !"NeedsReview".equalsIgnoreCase(st) && !"Rejected".equalsIgnoreCase(st)) {
            throw new BusinessException(ErrorCode.CONFLICT, "Chỉ khóa học ở trạng thái Nháp (Draft) hoặc Cần chỉnh sửa (NeedsReview) mới có thể gửi duyệt.");
        }

        // BR-07 Checklist kiểm tra trước khi gửi duyệt
        if (course.getTitle() == null || course.getTitle().trim().length() < 5) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Tiêu đề khóa học phải có ít nhất 5 ký tự hợp lệ.");
        }
        if (course.getDescription() == null || course.getDescription().trim().length() < 10) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Mô tả khóa học phải có ít nhất 10 ký tự trước khi nộp duyệt.");
        }
        if (course.getCategoryId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Khóa học phải thuộc về một danh mục hợp lệ.");
        }
        if (course.getPrice() != null && course.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            long p = course.getPrice().longValue();
            if (p < 10000 || p % 1000 != 0) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Giá khóa học trả phí phải từ 10.000 VNĐ trở lên và là bội số của 1.000 VNĐ.");
            }
        }

        List<CourseSection> sections = courseSectionRepository.findAll().stream()
                .filter(s -> courseId.equals(s.getCourseId()))
                .toList();
        if (sections.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Khóa học phải có ít nhất 1 chương (section) trước khi nộp duyệt.");
        }

        // Kiểm tra không có chương nào bị rỗng bài học
        List<Lesson> allLessons = lessonRepository.findAll();
        for (CourseSection sec : sections) {
            List<Lesson> secLessons = allLessons.stream()
                    .filter(l -> sec.getSectionId().equals(l.getSectionId()))
                    .toList();
            if (secLessons.isEmpty()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Chương '" + sec.getTitle() + "' chưa có bài học nào. Vui lòng thêm bài học hoặc xóa chương trống.");
            }
            for (Lesson les : secLessons) {
                boolean hasContent = (les.getVideoUrl() != null && !les.getVideoUrl().trim().isEmpty())
                        || (les.getContentText() != null && !les.getContentText().trim().isEmpty())
                        || (les.getPdfUrl() != null && !les.getPdfUrl().trim().isEmpty());
                if (!hasContent) {
                    throw new BusinessException(ErrorCode.BAD_REQUEST, "Bài học '" + les.getTitle() + "' thuộc chương '" + sec.getTitle() + "' chưa có nội dung hoặc liên kết video.");
                }
            }
        }

        course.setStatus("PendingApproval");
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);
        return ResponseEntity.ok(Map.of(
                "courseId", courseId,
                "status", "PendingApproval",
                "message", "Đã kiểm tra checklist thành công và gửi yêu cầu xét duyệt khóa học tới Quản trị viên."
        ));
    }

    @PostMapping({"/api/v1/instructor/courses/{courseId}/withdraw", "/api/instructor/courses/{courseId}/withdraw"})
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> withdrawCourseSubmission(@PathVariable Integer courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học."));
        requireCourseOwnerOrAdmin(course);
        if (!"PendingApproval".equalsIgnoreCase(course.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT, "Chỉ có thể rút yêu cầu duyệt khi khóa học đang ở trạng thái chờ duyệt (PendingApproval).");
        }
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
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học."));
        requireCourseEditorOrAdmin(course);
        if ("PendingApproval".equalsIgnoreCase(course.getStatus()) && !SecurityUtils.hasRole("Admin")) {
            throw new BusinessException(ErrorCode.CONFLICT, "Khóa học đang chờ duyệt, không thể sắp xếp lại chương.");
        }
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
        CourseSection section = courseSectionRepository.findById(sectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy chương học."));
        Course course = courseRepository.findById(section.getCourseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học."));
        requireCourseEditorOrAdmin(course);
        if ("PendingApproval".equalsIgnoreCase(course.getStatus()) && !SecurityUtils.hasRole("Admin")) {
            throw new BusinessException(ErrorCode.CONFLICT, "Khóa học đang chờ duyệt, không thể sắp xếp lại bài học.");
        }
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
        CourseSection section = lesson.getSectionId() != null ? courseSectionRepository.findById(lesson.getSectionId()).orElse(null) : null;
        Course course = (section != null && section.getCourseId() != null) ? courseRepository.findById(section.getCourseId()).orElse(null) : null;
        if (course != null) {
            requireCourseEditorOrAdmin(course);
            if ("PendingApproval".equalsIgnoreCase(course.getStatus()) && !SecurityUtils.hasRole("Admin")) {
                throw new BusinessException(ErrorCode.CONFLICT, "Khóa học đang chờ duyệt, không thể chỉnh sửa bài học.");
            }
        }
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
        if (body.containsKey("contentType")) {
            lesson.setContentType(body.get("contentType") != null ? body.get("contentType").toString() : null);
        }
        if (body.containsKey("contentText")) {
            lesson.setContentText(body.get("contentText") != null ? body.get("contentText").toString() : null);
        }
        if (body.containsKey("pdfUrl")) {
            lesson.setPdfUrl(body.get("pdfUrl") != null ? body.get("pdfUrl").toString() : null);
        }
        lessonRepository.save(lesson);

        return ResponseEntity.ok(lesson);
    }

    @DeleteMapping({"/api/v1/instructor/lessons/{lessonId}", "/api/lessons/{lessonId}"})
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> deleteLesson(@PathVariable Integer lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy bài học."));
        CourseSection section = lesson.getSectionId() != null ? courseSectionRepository.findById(lesson.getSectionId()).orElse(null) : null;
        Course course = (section != null && section.getCourseId() != null) ? courseRepository.findById(section.getCourseId()).orElse(null) : null;
        if (course != null) {
            requireCourseEditorOrAdmin(course);
            if ("PendingApproval".equalsIgnoreCase(course.getStatus()) && !SecurityUtils.hasRole("Admin")) {
                throw new BusinessException(ErrorCode.CONFLICT, "Khóa học đang chờ duyệt, không thể xóa bài học.");
            }
        }
        lessonRepository.delete(lesson);
        return ResponseEntity.noContent().build();
    }

    // Co-instructors (UC-I13..I16, BR-43..46)
    @GetMapping({"/api/v1/instructor/courses/{courseId}/co-instructors", "/api/instructor/courses/{courseId}/co-instructors"})
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> getCoInstructors(@PathVariable Integer courseId) {
        List<CourseCoInstructor> rows = courseCoInstructorRepository.findByCourseIdAndStatusNot(courseId, "REMOVED");
        List<Map<String, Object>> list = toCoInstructorDtos(rows);
        int totalAccepted = rows.stream()
                .filter(r -> "ACCEPTED".equalsIgnoreCase(r.getStatus()))
                .mapToInt(r -> r.getShareBps() != null ? r.getShareBps() : 0)
                .sum();
        int mainShareBps = Math.max(0, 10000 - totalAccepted);
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("coInstructors", list);
        res.put("mainShareBps", mainShareBps);
        return ResponseEntity.ok(res);
    }

    @PostMapping({"/api/v1/instructor/courses/{courseId}/co-instructors", "/api/instructor/courses/{courseId}/co-instructors"})
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> inviteCoInstructor(@PathVariable Integer courseId, @RequestBody Map<String, Object> body) {
        Integer currentUserId = SecurityUtils.getCurrentUserId();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học."));
        requireCourseOwnerOrAdmin(course);
        String email = body != null && body.get("email") != null ? String.valueOf(body.get("email")).trim().toLowerCase() : "";
        if (email.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Vui lòng nhập email giảng viên cần mời.");
        }

        int shareBps = parseShareBps(body != null ? body.get("shareBps") : null);
        if (shareBps < 0 || shareBps > 10000) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_SHARE_BPS", "Tỷ lệ chia sẻ phải từ 0 đến 10.000 bps.");
        }

        User target = userRepository.findByEmail(email);
        if (target == null || !Boolean.TRUE.equals(target.getIsActive())
                || (!"Instructor".equalsIgnoreCase(target.getRole()) && !"Admin".equalsIgnoreCase(target.getRole()))
                || Objects.equals(target.getUserId(), currentUserId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy giảng viên hợp lệ với email đã nhập.");
        }

        List<CourseCoInstructor> rows = courseCoInstructorRepository.findByCourseIdAndStatusNot(courseId, "REMOVED");
        int currentTotal = rows.stream()
                .filter(r -> !Objects.equals(r.getInstructorId(), target.getUserId()))
                .filter(r -> "ACCEPTED".equalsIgnoreCase(r.getStatus()) || "INVITED".equalsIgnoreCase(r.getStatus()))
                .mapToInt(r -> r.getShareBps() != null ? r.getShareBps() : 0)
                .sum();

        if (currentTotal + shareBps > 10000) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "CO_INSTRUCTOR_SHARE_EXCEEDS_TOTAL",
                    "Tổng tỷ lệ chia sẻ của các đồng giảng viên vượt quá 100%.");
        }

        CourseCoInstructor entity = courseCoInstructorRepository
                .findFirstByCourseIdAndInstructorId(courseId, target.getUserId())
                .orElseGet(() -> CourseCoInstructor.builder()
                        .courseId(courseId)
                        .instructorId(target.getUserId())
                        .build());

        entity.setStatus("INVITED");
        entity.setShareBps(shareBps);
        entity.setInvitedAt(LocalDateTime.now());
        entity.setRespondedAt(null);
        entity = courseCoInstructorRepository.save(entity);

        User inviter = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;
        String inviterName = inviter != null ? inviter.getFullName() : "Giảng viên phụ trách";
        mailService.sendCoInstructorInvited(target.getEmail(), target.getFullName(), inviterName, course.getTitle());

        return ResponseEntity.ok(toCoInstructorDto(entity, course, target));
    }

    @PutMapping({"/api/v1/instructor/courses/{courseId}/co-instructors/{instructorId}/share",
                 "/api/instructor/courses/{courseId}/co-instructors/{instructorId}/share"})
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> updateCoInstructorShare(
            @PathVariable Integer courseId,
            @PathVariable Integer instructorId,
            @RequestBody Map<String, Object> body) {
        Integer currentUserId = SecurityUtils.getCurrentUserId();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học."));
        if (!Objects.equals(course.getInstructorId(), currentUserId) && !SecurityUtils.hasRole("Admin")) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Chỉ giảng viên chính mới có quyền thay đổi tỷ lệ chia sẻ.");
        }

        int newShareBps = parseShareBps(body != null ? body.get("shareBps") : null);
        if (newShareBps < 0 || newShareBps > 10000) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_SHARE_BPS", "Tỷ lệ chia sẻ phải từ 0 đến 10.000 bps (0% - 100%).");
        }

        List<CourseCoInstructor> rows = courseCoInstructorRepository.findByCourseIdAndStatusNot(courseId, "REMOVED");
        CourseCoInstructor targetRow = null;
        int otherTotal = 0;
        for (CourseCoInstructor r : rows) {
            if (Objects.equals(r.getInstructorId(), instructorId)) {
                targetRow = r;
            } else if ("ACCEPTED".equalsIgnoreCase(r.getStatus()) || "INVITED".equalsIgnoreCase(r.getStatus())) {
                otherTotal += r.getShareBps() != null ? r.getShareBps() : 0;
            }
        }

        if (targetRow == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy đồng giảng viên này trong khóa học.");
        }
        if (otherTotal + newShareBps > 10000) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "CO_INSTRUCTOR_SHARE_EXCEEDS_TOTAL",
                    "Tổng tỷ lệ chia sẻ của các đồng giảng viên vượt quá 100%.");
        }

        targetRow.setShareBps(newShareBps);
        targetRow = courseCoInstructorRepository.save(targetRow);

        int totalAccepted = rows.stream()
                .filter(r -> "ACCEPTED".equalsIgnoreCase(r.getStatus()))
                .mapToInt(r -> Objects.equals(r.getInstructorId(), instructorId) ? newShareBps : (r.getShareBps() != null ? r.getShareBps() : 0))
                .sum();
        int mainShareBps = Math.max(0, 10000 - totalAccepted);

        User u = userRepository.findById(instructorId).orElse(null);
        Map<String, Object> res = new LinkedHashMap<>(toCoInstructorDto(targetRow, course, u));
        res.put("mainShareBps", mainShareBps);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping({"/api/v1/instructor/courses/{courseId}/co-instructors/{instructorId}", "/api/instructor/courses/{courseId}/co-instructors/{instructorId}"})
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> removeCoInstructor(@PathVariable Integer courseId, @PathVariable Integer instructorId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học."));
        requireCourseOwnerOrAdmin(course);
        courseCoInstructorRepository.findFirstByCourseIdAndInstructorId(courseId, instructorId)
                .ifPresent(ci -> {
                    ci.setStatus("REMOVED");
                    ci.setShareBps(0);
                    ci.setRespondedAt(LocalDateTime.now());
                    courseCoInstructorRepository.save(ci);
                });
        return ResponseEntity.ok(Map.of("message", "Đã gỡ đồng giảng viên khỏi khóa học."));
    }

    // UC-I18: Co-instructor leaves a course voluntarily
    @PostMapping({"/api/v1/instructor/courses/{courseId}/co-instructors/leave", "/api/instructor/courses/{courseId}/co-instructors/leave"})
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<?> leaveCoInstructorCourse(@PathVariable Integer courseId) {
        Integer currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học."));
        if (Objects.equals(course.getInstructorId(), currentUserId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Giảng viên chính không thể tự rời khỏi khóa học của mình.");
        }
        CourseCoInstructor membership = courseCoInstructorRepository
                .findFirstByCourseIdAndInstructorId(courseId, currentUserId)
                .filter(ci -> "ACCEPTED".equalsIgnoreCase(ci.getStatus()))
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN, "Bạn không phải đồng giảng viên đang hoạt động của khóa học này."));
        membership.setStatus("REMOVED");
        membership.setShareBps(0);
        membership.setRespondedAt(LocalDateTime.now());
        courseCoInstructorRepository.save(membership);
        return ResponseEntity.ok(Map.of(
                "courseId", courseId,
                "instructorId", currentUserId,
                "status", "LEFT",
                "message", "Bạn đã rời khỏi danh sách đồng giảng viên của khóa học thành công."
        ));
    }

    public record CoInstructorInvitationDto(
            Integer id,
            Integer invitationId,
            Integer courseId,
            String courseTitle,
            Integer instructorId,
            String fullName,
            String email,
            String status,
            Integer shareBps,
            String invitedAt,
            String respondedAt
    ) {}

    public record InvitationActionResponse(
            CoInstructorInvitationDto invitation,
            String message
    ) {}

    @GetMapping({"/api/v1/instructor/invitations", "/api/instructor/invitations"})
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<List<CoInstructorInvitationDto>> getMyInvitations() {
        Integer currentUserId = SecurityUtils.getCurrentUserId();
        List<CourseCoInstructor> rows = courseCoInstructorRepository.findByInstructorIdAndStatusNot(currentUserId, "REMOVED");
        return ResponseEntity.ok(toCoInstructorInvitationDtos(rows));
    }

    @PostMapping({"/api/v1/instructor/invitations/{invitationId}/accept", "/api/instructor/invitations/{invitationId}/accept"})
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<InvitationActionResponse> acceptInvitation(@PathVariable Integer invitationId) {
        return updateInvitationStatus(invitationId, "ACCEPTED", "Đã chấp nhận lời mời đồng giảng viên.");
    }

    @PostMapping({"/api/v1/instructor/invitations/{invitationId}/decline", "/api/instructor/invitations/{invitationId}/decline"})
    @PreAuthorize("hasAnyRole('Instructor', 'Admin')")
    public ResponseEntity<InvitationActionResponse> declineInvitation(@PathVariable Integer invitationId) {
        return updateInvitationStatus(invitationId, "DECLINED", "Đã từ chối lời mời đồng giảng viên.");
    }

    private ResponseEntity<InvitationActionResponse> updateInvitationStatus(Integer invitationId, String status, String message) {
        Integer currentUserId = SecurityUtils.getCurrentUserId();
        CourseCoInstructor inv = courseCoInstructorRepository.findById(invitationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy lời mời cộng tác."));
        if (currentUserId != null && !Objects.equals(inv.getInstructorId(), currentUserId) && !SecurityUtils.hasRole("Admin")) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Bạn không có quyền xử lý lời mời này.");
        }
        inv.setStatus(status);
        inv.setRespondedAt(LocalDateTime.now());
        inv = courseCoInstructorRepository.save(inv);

        Course course = courseRepository.findById(inv.getCourseId()).orElse(null);
        User user = userRepository.findById(inv.getInstructorId()).orElse(null);
        return ResponseEntity.ok(new InvitationActionResponse(toCoInstructorInvitationDto(inv, course, user), message));
    }

    private List<CoInstructorInvitationDto> toCoInstructorInvitationDtos(List<CourseCoInstructor> rows) {
        if (rows == null || rows.isEmpty()) return Collections.emptyList();
        Set<Integer> courseIds = rows.stream().map(CourseCoInstructor::getCourseId).collect(Collectors.toSet());
        Set<Integer> userIds = rows.stream().map(CourseCoInstructor::getInstructorId).collect(Collectors.toSet());
        Map<Integer, Course> courseMap = courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getCourseId, c -> c));
        Map<Integer, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));
        return rows.stream()
                .map(r -> toCoInstructorInvitationDto(r, courseMap.get(r.getCourseId()), userMap.get(r.getInstructorId())))
                .collect(Collectors.toList());
    }

    private CoInstructorInvitationDto toCoInstructorInvitationDto(CourseCoInstructor r, Course course, User user) {
        return new CoInstructorInvitationDto(
                r.getId(),
                r.getId(),
                r.getCourseId(),
                course != null ? course.getTitle() : "Khóa học #" + r.getCourseId(),
                r.getInstructorId(),
                user != null ? user.getFullName() : null,
                user != null ? user.getEmail() : null,
                r.getStatus(),
                r.getShareBps(),
                r.getInvitedAt() != null ? r.getInvitedAt().toString() : null,
                r.getRespondedAt() != null ? r.getRespondedAt().toString() : null
        );
    }

    private int parseShareBps(Object s) {
        if (s instanceof Number n) return n.intValue();
        if (s != null) {
            try {
                return Integer.parseInt(s.toString());
            } catch (Exception ignored) {}
        }
        return 0;
    }

    private List<Map<String, Object>> toCoInstructorDtos(List<CourseCoInstructor> rows) {
        if (rows == null || rows.isEmpty()) return Collections.emptyList();
        Set<Integer> courseIds = rows.stream().map(CourseCoInstructor::getCourseId).collect(Collectors.toSet());
        Set<Integer> userIds = rows.stream().map(CourseCoInstructor::getInstructorId).collect(Collectors.toSet());
        Map<Integer, Course> courseMap = courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getCourseId, c -> c));
        Map<Integer, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));
        return rows.stream()
                .map(r -> toCoInstructorDto(r, courseMap.get(r.getCourseId()), userMap.get(r.getInstructorId())))
                .collect(Collectors.toList());
    }

    private Map<String, Object> toCoInstructorDto(CourseCoInstructor r, Course course, User user) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("invitationId", r.getId());
        m.put("courseId", r.getCourseId());
        m.put("courseTitle", course != null ? course.getTitle() : "Khóa học #" + r.getCourseId());
        m.put("instructorId", r.getInstructorId());
        m.put("fullName", user != null ? user.getFullName() : null);
        m.put("email", user != null ? user.getEmail() : null);
        m.put("status", r.getStatus());
        m.put("shareBps", r.getShareBps());
        m.put("invitedAt", r.getInvitedAt() != null ? r.getInvitedAt().toString() : null);
        m.put("respondedAt", r.getRespondedAt() != null ? r.getRespondedAt().toString() : null);
        return m;
    }

    // Admin Course Approval / Reject with reason / Hide / Unhide (UC-A09..A12)
    @PostMapping({"/api/v1/admin/courses/{courseId}/approve", "/api/admin/courses/{courseId}/approve"})
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> approveCourse(@PathVariable Integer courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học."));
        if (!"PendingApproval".equalsIgnoreCase(course.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT, "Chỉ có thể duyệt khóa học đang ở trạng thái chờ duyệt (PendingApproval).");
        }
        course.setStatus("Published");
        course.setReviewNote(null);
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);

        if (course.getInstructorId() != null) {
            userRepository.findById(course.getInstructorId()).ifPresent(inst ->
                    mailService.sendCourseApproved(inst.getEmail(), inst.getFullName(), course.getTitle(), course.getSlug()));
        }

        return ResponseEntity.ok(Map.of(
                "courseId", courseId,
                "status", "Published",
                "message", "Đã phê duyệt và xuất bản khóa học công khai."
        ));
    }

    @PostMapping({"/api/v1/admin/courses/{courseId}/reject", "/api/admin/courses/{courseId}/reject"})
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> rejectCourse(@PathVariable Integer courseId, @RequestBody Map<String, String> body) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học."));
        if (!"PendingApproval".equalsIgnoreCase(course.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT, "Chỉ có thể từ chối khóa học đang ở trạng thái chờ duyệt (PendingApproval).");
        }
        String reviewNote = body != null ? body.get("reviewNote") : null;
        if (reviewNote == null || reviewNote.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Vui lòng nhập lý do từ chối xét duyệt để giảng viên chỉnh sửa.");
        }
        course.setStatus("NeedsReview");
        course.setReviewNote(reviewNote.trim());
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);

        if (course.getInstructorId() != null) {
            userRepository.findById(course.getInstructorId()).ifPresent(inst ->
                    mailService.sendCourseRejected(inst.getEmail(), inst.getFullName(), course.getTitle(), Long.valueOf(courseId), reviewNote.trim()));
        }

        return ResponseEntity.ok(Map.of(
                "courseId", courseId,
                "status", "NeedsReview",
                "reviewNote", reviewNote.trim(),
                "message", "Đã từ chối khóa học và gửi phản hồi cho giảng viên."
        ));
    }

    @PostMapping({"/api/v1/admin/courses/{courseId}/hide", "/api/admin/courses/{courseId}/hide"})
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> hideCourse(@PathVariable Integer courseId, @RequestBody(required = false) Map<String, String> body) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học."));
        if (!"Published".equalsIgnoreCase(course.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT, "Chỉ có thể ẩn khóa học đang ở trạng thái Công khai (Published).");
        }
        String reason = (body != null && body.get("reason") != null) ? body.get("reason").trim() : "Quản trị viên ẩn theo chính sách nền tảng";
        course.setStatus("Hidden");
        course.setReviewNote("Lý do ẩn: " + reason);
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);

        if (course.getInstructorId() != null) {
            userRepository.findById(course.getInstructorId()).ifPresent(inst ->
                    mailService.sendCourseHidden(inst.getEmail(), inst.getFullName(), course.getTitle(), Long.valueOf(courseId), reason));
        }

        return ResponseEntity.ok(Map.of(
                "courseId", courseId,
                "status", "Hidden",
                "reason", reason,
                "message", "Đã ẩn khóa học khỏi danh sách công khai. Học viên đã ghi danh vẫn tiếp tục học bình thường."
        ));
    }

    @PostMapping({"/api/v1/admin/courses/{courseId}/unhide", "/api/admin/courses/{courseId}/unhide"})
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> unhideCourse(@PathVariable Integer courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học."));
        if (!"Hidden".equalsIgnoreCase(course.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT, "Chỉ có thể hiện lại khóa học đang ở trạng thái Đã ẩn (Hidden).");
        }
        course.setStatus("Published");
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);
        return ResponseEntity.ok(Map.of(
                "courseId", courseId,
                "status", "Published",
                "message", "Đã hiện lại khóa học công khai."
        ));
    }

    // D-21: Admin deactivate / activate category
    @PostMapping({"/api/v1/admin/categories/{categoryId}/deactivate", "/api/admin/categories/{categoryId}/deactivate"})
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> deactivateCategory(@PathVariable Integer categoryId) {
        var cat = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy danh mục."));
        cat.setIsActive(false);
        categoryRepository.save(cat);
        return ResponseEntity.ok(Map.of(
                "categoryId", categoryId,
                "isActive", false,
                "message", "Đã ngừng sử dụng danh mục '" + cat.getName() + "' thành công."
        ));
    }

    @PostMapping({"/api/v1/admin/categories/{categoryId}/activate", "/api/admin/categories/{categoryId}/activate"})
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> activateCategory(@PathVariable Integer categoryId) {
        var cat = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy danh mục."));
        cat.setIsActive(true);
        categoryRepository.save(cat);
        return ResponseEntity.ok(Map.of(
                "categoryId", categoryId,
                "isActive", true,
                "message", "Đã kích hoạt lại danh mục '" + cat.getName() + "' thành công."
        ));
    }

    @GetMapping({"/api/v1/courses/{courseId}/review-note", "/api/courses/{courseId}/review-note"})
    public ResponseEntity<?> getCourseReviewNote(@PathVariable Integer courseId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        String note = (course != null && course.getReviewNote() != null) ? course.getReviewNote() : "";
        return ResponseEntity.ok(Map.of(
                "courseId", courseId,
                "reviewNote", note
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
                "message", "Đã hủy đơn hàng chờ thanh toán thành công."
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
                    boolean reconcile = "NEEDS_REVIEW".equalsIgnoreCase(status)
                            || "RECONCILE".equalsIgnoreCase(status)
                            || "FAILED".equalsIgnoreCase(status);
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
                    tx.put("reconciliationNote", order.getReconciliationNote() != null
                            ? order.getReconciliationNote()
                            : (reconcile ? "Cần kiểm tra đối soát giao dịch" : null));
                    tx.put("reconciledAt", order.getReconciledAt());
                    tx.put("createdAt", order.getCreatedAt());
                    return tx;
                })
                .filter(tx -> needsReconciliation == null || Objects.equals(tx.get("needsReconciliation"), needsReconciliation))
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactions);
    }

    // UC-A18: Admin manual payment reconciliation (ACTIVATE | REFUND_EXTERNAL)
    @PostMapping({"/api/v1/admin/payments/{orderId}/reconcile", "/api/admin/payments/{orderId}/reconcile"})
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> reconcilePayment(@PathVariable Integer orderId, @RequestBody(required = false) Map<String, String> body) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy đơn hàng #" + orderId));

        String status = order.getStatus() != null ? order.getStatus().toUpperCase() : "";
        if (!"NEEDS_REVIEW".equals(status) && !"RECONCILE".equals(status)) {
            throw new BusinessException(ErrorCode.CONFLICT, "Đơn hàng này không ở trạng thái chờ đối soát (hiện tại: " + status + ") hoặc đã được xử lý trước đó.");
        }

        String action = body != null ? body.getOrDefault("action", body.getOrDefault("resolution", "")).trim().toUpperCase() : "";
        String note = body != null ? body.getOrDefault("note", body.getOrDefault("reconciliationNote", "")).trim() : "";
        if (note.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Bắt buộc nhập ghi chú đối soát khi xử lý đơn hàng.");
        }
        if (!"ACTIVATE".equals(action) && !"REFUND_EXTERNAL".equals(action)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Hành động đối soát không hợp lệ (chỉ chấp nhận ACTIVATE hoặc REFUND_EXTERNAL).");
        }

        List<OrderItem> items = order.getOrderItems();
        if ((items == null || items.isEmpty()) && orderItemRepository != null) {
            items = orderItemRepository.findByOrderId(orderId);
        }

        Integer adminId = SecurityUtils.getCurrentUserId();
        boolean activated = "ACTIVATE".equals(action);
        order.setStatus(activated ? "PAID" : "REFUNDED_EXTERNAL");
        order.setReconciliationNote(note);
        order.setReconciledAt(LocalDateTime.now());
        order.setReconciledBy(adminId);
        orderRepository.save(order);

        List<String> titles = new ArrayList<>();
        if (items != null) {
            List<com.edumy.payout.api.EarningApi.OrderLine> earningLines = new ArrayList<>();
            for (OrderItem item : items) {
                Integer cid = item.getCourseId();
                if (cid == null) continue;
                Course course = courseRepository.findById(cid).orElse(null);
                if (course != null) titles.add(course.getTitle());

                if (activated && order.getUserId() != null) {
                    boolean exists = enrollmentRepository.existsByUserIdAndCourseId(order.getUserId(), cid);
                    if (!exists) {
                        Enrollment enrollment = Enrollment.builder()
                                .userId(order.getUserId())
                                .courseId(cid)
                                .enrolledAt(LocalDateTime.now())
                                .progressPercentage(0)
                                .status("ACTIVE")
                                .build();
                        enrollmentRepository.save(enrollment);
                        if (course != null) {
                            course.setStudentCount((course.getStudentCount() != null ? course.getStudentCount() : 0) + 1);
                            courseRepository.save(course);
                        }
                    }
                    if (item.getPrice() != null && item.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                        earningLines.add(new com.edumy.payout.api.EarningApi.OrderLine(
                                item.getOrderItemId(), cid,
                                course != null ? course.getTitle() : "Khóa học #" + cid,
                                item.getPrice().longValue()
                        ));
                    }
                }
            }
            if (activated && earningApi != null && !earningLines.isEmpty()) {
                earningApi.recordForPaidOrder(new com.edumy.payout.api.EarningApi.PaidOrderCommand(
                        orderId, "ORD-" + orderId, earningLines));
            }
        }

        if (order.getUserId() != null && mailService != null) {
            userRepository.findById(order.getUserId()).ifPresent(buyer ->
                    mailService.sendPaymentReconciled(buyer.getEmail(), buyer.getFullName(), "EDUMY" + orderId, activated, titles));
        }

        return ResponseEntity.ok(Map.of(
                "orderId", orderId,
                "action", action,
                "status", order.getStatus(),
                "reconciliationNote", note,
                "message", activated
                        ? "Đã đối soát kích hoạt quyền học cho học viên thành công."
                        : "Đã ghi nhận hoàn tiền ngoài hệ thống cho giao dịch."
        ));
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
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "COURSE_NOT_FOUND", "Không tìm thấy khóa học."));
        if (!"Published".equalsIgnoreCase(course.getStatus())) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "COURSE_NOT_FOUND", "Không tìm thấy khóa học.");
        }
        if (Objects.equals(course.getInstructorId(), userId)) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "OWN_COURSE", "Giảng viên không thể ghi danh khóa học của chính mình.");
        }
        if (course.getPrice() != null && course.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "COURSE_NOT_FREE", "Khóa học này là khóa trả phí, vui lòng thêm vào giỏ hàng để thanh toán.");
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
                "message", "Ghi danh khóa học miễn phí thành công! Bạn có thể vào học ngay."
        ));
    }

    @DeleteMapping({"/api/v1/learning/lessons/{lessonId}/completion", "/api/courses/{courseId}/lessons/{lessonId}/complete"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> unmarkLessonCompletion(@PathVariable(required = false) Integer courseId,
                                                    @PathVariable Integer lessonId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);
        var res = learningProgressService.markLesson(userId, lessonId, false);
        return ResponseEntity.ok(Map.of(
                "lessonId", lessonId,
                "completed", false,
                "progressPercent", res.progressPercent(),
                "message", "Đã bỏ đánh dấu hoàn thành bài học."
        ));
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
                    m.put("visibility", r.getVisibilityStatus() != null ? r.getVisibilityStatus() : "VISIBLE");
                    m.put("moderationReason", r.getModerationReason());
                    m.put("moderatedAt", r.getModeratedAt());
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
        review.setVisibilityStatus("HIDDEN_BY_ADMIN");
        review.setModerationReason(reason);
        review.setModeratedAt(LocalDateTime.now());
        reviewRepository.save(review);

        Integer courseId = review.getCourseId();
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course != null) {
            List<Review> remaining = reviewRepository.findByCourseId(courseId).stream()
                    .filter(r -> !"HIDDEN_BY_ADMIN".equalsIgnoreCase(r.getVisibilityStatus()))
                    .toList();
            double avg = remaining.isEmpty() ? 0.0 : remaining.stream().mapToInt(Review::getRating).average().orElse(0.0);
            course.setAverageRating(Math.round(avg * 10.0) / 10.0);
            courseRepository.save(course);
        }

        return ResponseEntity.ok(Map.of(
                "reviewId", reviewId,
                "visibility", "HIDDEN_BY_ADMIN",
                "moderationReason", reason,
                "message", "Đã ẩn đánh giá vi phạm thành công."
        ));
    }
}

