package com.edumy.backend.service;

import com.edumy.backend.dto.LessonAccessResponseDto;
import com.edumy.backend.model.Lesson;

import java.util.List;

public interface LessonService {
    /**
     * Lấy chi tiết bài học và kiểm tra IDOR:
     * - Preview == true: Cho phép xem
     * - Role Admin hoặc Giảng viên khóa học: Cho phép xem
     * - Học viên có Enrollment trạng thái ACTIVE: Cho phép xem
     * - Ngược lại: Ném AccessDeniedException("Bạn chưa đăng ký khóa học này nên không thể xem bài học!")
     */
    LessonAccessResponseDto getLessonAccess(Integer lessonId, Integer currentUserId);

    Lesson getLessonEntity(Integer lessonId, Integer currentUserId);

    List<Lesson> getSectionLessons(Integer sectionId, Integer currentUserId);
}
