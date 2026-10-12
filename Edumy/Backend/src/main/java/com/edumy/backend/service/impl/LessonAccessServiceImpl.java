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
import com.edumy.backend.service.LessonAccessService;
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
            throw new AccessDeniedException("Yêu cầu đăng nhập để truy cập bài học này.");
        }

        // Kiểm tra quyền Admin hoặc Instructor của khóa học
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

        // Case 3: Kiểm tra bảng Enrollments có bản ghi trạng thái ACTIVE không
        boolean hasActiveEnrollment = enrollmentRepository.existsByUserIdAndCourseIdAndStatus(currentUserId, courseId, "ACTIVE");

        if (!hasActiveEnrollment) {
            throw new AccessDeniedException("Bạn chưa đăng ký khóa học này hoặc lượt đăng ký chưa được kích hoạt.");
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
