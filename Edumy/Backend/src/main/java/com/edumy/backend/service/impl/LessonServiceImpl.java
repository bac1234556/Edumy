package com.edumy.backend.service.impl;

import com.edumy.backend.dto.LessonAccessResponseDto;
import com.edumy.backend.model.Course;
import com.edumy.backend.model.CourseSection;
import com.edumy.backend.model.Lesson;
import com.edumy.backend.model.User;
import com.edumy.backend.repository.CourseRepository;
import com.edumy.backend.repository.CourseSectionRepository;
import com.edumy.backend.repository.EnrollmentRepository;
import com.edumy.backend.repository.LessonRepository;
import com.edumy.backend.repository.UserRepository;
import com.edumy.backend.service.LessonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LessonServiceImpl implements LessonService {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private CourseSectionRepository courseSectionRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    private boolean isCurrentUserAdmin(Integer userId) {
        if (userId == null) return false;
        boolean hasAdminAuthority = SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_Admin") ||
                                a.getAuthority().equalsIgnoreCase("ROLE_ADMIN") ||
                                a.getAuthority().equalsIgnoreCase("Admin"));
        if (hasAdminAuthority) return true;

        User user = userRepository.findById(userId).orElse(null);
        return user != null && "Admin".equalsIgnoreCase(user.getRole());
    }

    private void checkLessonAccessPermission(Lesson lesson, Integer currentUserId) {
        // 1. Nếu là bài xem thử miễn phí (Preview): cho phép xem đầy đủ
        if (Boolean.TRUE.equals(lesson.getIsPreview())) {
            return;
        }

        // 2. Không phải preview nhưng chưa đăng nhập
        if (currentUserId == null) {
            throw new AccessDeniedException("Bạn chưa đăng ký khóa học này nên không thể xem bài học!");
        }

        // 3. Nếu là Admin: cho phép xem đầy đủ
        if (isCurrentUserAdmin(currentUserId)) {
            return;
        }

        // Lấy khóa học cha thông qua Section
        Integer sectionId = lesson.getSectionId();
        CourseSection section = lesson.getSection();
        if (section == null && sectionId != null) {
            section = courseSectionRepository.findById(sectionId).orElse(null);
        }

        if (section == null) {
            throw new NoSuchElementException("Không tìm thấy chương học của bài giảng này.");
        }

        Integer courseId = section.getCourseId();
        Course course = section.getCourse();
        if (course == null && courseId != null) {
            course = courseRepository.findById(courseId).orElse(null);
        }

        // 4. Nếu là Giảng viên sở hữu khóa học: cho phép xem đầy đủ
        if (course != null && course.getInstructorId() != null && course.getInstructorId().equals(currentUserId)) {
            return;
        }

        // 5. Kiểm tra học viên có đăng ký khóa học hợp lệ không (ACTIVE hoặc COMPLETED)
        boolean hasActiveEnrollment = false;
        if (courseId != null) {
            hasActiveEnrollment = enrollmentRepository.isUserEnrolledActive(currentUserId, courseId);
        }

        if (!hasActiveEnrollment) {
            throw new AccessDeniedException("Bạn chưa đăng ký khóa học này nên không thể xem bài học!");
        }
    }

    @Override
    public LessonAccessResponseDto getLessonAccess(Integer lessonId, Integer currentUserId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NoSuchElementException("Bài học không tồn tại với ID: " + lessonId));

        checkLessonAccessPermission(lesson, currentUserId);

        Integer sectionId = lesson.getSectionId();
        CourseSection section = lesson.getSection();
        if (section == null && sectionId != null) {
            section = courseSectionRepository.findById(sectionId).orElse(null);
        }
        Integer courseId = section != null ? section.getCourseId() : null;

        return LessonAccessResponseDto.builder()
                .lessonId(lesson.getLessonId())
                .sectionId(lesson.getSectionId())
                .courseId(courseId)
                .title(lesson.getTitle())
                .videoUrl(lesson.getVideoUrl())
                .duration(lesson.getDuration())
                .isPreview(Boolean.TRUE.equals(lesson.getIsPreview()))
                .canPlay(true)
                .message("Xác thực quyền truy cập bài học thành công.")
                .build();
    }

    @Override
    public Lesson getLessonEntity(Integer lessonId, Integer currentUserId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NoSuchElementException("Bài học không tồn tại với ID: " + lessonId));

        checkLessonAccessPermission(lesson, currentUserId);

        return lesson;
    }

    @Override
    public List<Lesson> getSectionLessons(Integer sectionId, Integer currentUserId) {
        CourseSection section = courseSectionRepository.findById(sectionId)
                .orElseThrow(() -> new NoSuchElementException("Chương học không tồn tại"));

        Integer courseId = section.getCourseId();
        Course course = courseRepository.findById(courseId).orElse(null);
        Integer instructorId = course != null ? course.getInstructorId() : null;

        boolean canAccessFull = false;
        if (currentUserId != null) {
            if (isCurrentUserAdmin(currentUserId)) {
                canAccessFull = true;
            } else if (instructorId != null && instructorId.equals(currentUserId)) {
                canAccessFull = true;
            } else if (courseId != null) {
                canAccessFull = enrollmentRepository.isUserEnrolledActive(currentUserId, courseId);
            }
        }

        final boolean hasFullAccess = canAccessFull;

        return lessonRepository.findAll().stream()
                .filter(l -> sectionId.equals(l.getSectionId()))
                .sorted(Comparator.comparing(Lesson::getOrderIndex, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(l -> {
                    Lesson copy = new Lesson();
                    copy.setLessonId(l.getLessonId());
                    copy.setSectionId(l.getSectionId());
                    copy.setTitle(l.getTitle());
                    copy.setDuration(l.getDuration());
                    copy.setOrderIndex(l.getOrderIndex());
                    copy.setIsPreview(l.getIsPreview());
                    if (hasFullAccess || Boolean.TRUE.equals(l.getIsPreview())) {
                        copy.setVideoUrl(l.getVideoUrl());
                    } else {
                        copy.setVideoUrl(null);
                    }
                    return copy;
                })
                .collect(Collectors.toList());
    }
}
