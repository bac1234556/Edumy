package com.edumy.modules.enrollment.service.impl;

import com.edumy.modules.course.model.Course;
import com.edumy.modules.course.repository.CourseRepository;
import com.edumy.modules.course.model.CourseSection;
import com.edumy.modules.course.repository.CourseSectionRepository;
import com.edumy.modules.enrollment.repository.EnrollmentRepository;
import com.edumy.modules.course.model.Lesson;
import com.edumy.modules.enrollment.dto.LessonAccessResponseDto;
import com.edumy.modules.enrollment.service.LessonAccessService;
import com.edumy.modules.course.repository.LessonRepository;
import com.edumy.modules.user.model.User;
import com.edumy.modules.user.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class LessonAccessServiceImpl implements LessonAccessService {

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

    @Override
    public LessonAccessResponseDto checkLessonAccess(Integer lessonId, Integer currentUserId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NoSuchElementException("Bài học không tồn tại với ID: " + lessonId));

        CourseSection section = courseSectionRepository.findById(lesson.getSectionId())
                .orElseThrow(() -> new NoSuchElementException("Chương học không tồn tại"));

        Integer courseId = section.getCourseId();
        Course course = courseRepository.findById(courseId).orElse(null);

        // Case 1: Bài học xem thử miễn phí (Preview)
        if (Boolean.TRUE.equals(lesson.getIsPreview())) {
            return LessonAccessResponseDto.builder()
                    .lessonId(lesson.getLessonId())
                    .courseId(courseId)
                    .sectionId(lesson.getSectionId())
                    .title(lesson.getTitle())
                    .videoUrl(lesson.getVideoUrl())
                    .duration(lesson.getDuration())
                    .isPreview(true)
                    .canPlay(true)
                    .message("Truy cập video học thử miễn phí (Preview) thành công.")
                    .build();
        }

        // Case 2: Bài học yêu cầu bản quyền - Bắt buộc xác thực danh tính
        if (currentUserId == null) {
            throw new AccessDeniedException("Bạn chưa đăng ký khóa học này nên không thể xem bài học!");
        }

        // Kiểm tra quyền Admin hoặc Instructor sở hữu khóa học
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        if (currentUser != null && "Admin".equalsIgnoreCase(currentUser.getRole())) {
            return LessonAccessResponseDto.builder()
                    .lessonId(lesson.getLessonId())
                    .courseId(courseId)
                    .sectionId(lesson.getSectionId())
                    .title(lesson.getTitle())
                    .videoUrl(lesson.getVideoUrl())
                    .duration(lesson.getDuration())
                    .isPreview(false)
                    .canPlay(true)
                    .message("Truy cập bài học với quyền Quản trị viên (Admin).")
                    .build();
        }

        if (course != null && course.getInstructorId() != null && course.getInstructorId().equals(currentUserId)) {
            return LessonAccessResponseDto.builder()
                    .lessonId(lesson.getLessonId())
                    .courseId(courseId)
                    .sectionId(lesson.getSectionId())
                    .title(lesson.getTitle())
                    .videoUrl(lesson.getVideoUrl())
                    .duration(lesson.getDuration())
                    .isPreview(false)
                    .canPlay(true)
                    .message("Truy cập bài học với quyền Giảng viên khóa học.")
                    .build();
        }

        // Case 3: Kiểm tra học viên đã đăng ký khóa học này chưa (ACTIVE hoặc COMPLETED)
        boolean hasActiveEnrollment = enrollmentRepository.isUserEnrolledActive(currentUserId, courseId);

        if (!hasActiveEnrollment) {
            throw new AccessDeniedException("Bạn chưa đăng ký khóa học này nên không thể xem bài học!");
        }

        return LessonAccessResponseDto.builder()
                .lessonId(lesson.getLessonId())
                .courseId(courseId)
                .sectionId(lesson.getSectionId())
                .title(lesson.getTitle())
                .videoUrl(lesson.getVideoUrl())
                .duration(lesson.getDuration())
                .isPreview(false)
                .canPlay(true)
                .message("Xác thực quyền học viên (ACTIVE) thành công.")
                .build();
    }
}