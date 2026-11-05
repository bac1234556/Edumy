package com.edumy.backend.service;

import com.edumy.backend.dto.LessonAccessResponseDto;

public interface LessonAccessService {
    /**
     * Checks if the user has access to stream a specific lesson.
     * If isPreview == true -> Granted
     * Else -> Requires JWT authentication and ACTIVE enrollment in the course.
     *
     * @param lessonId ID of the lesson to check
     * @param currentUserId Authenticated user ID (or null if unauthenticated)
     * @return LessonAccessResponseDto with video streaming details
     */
    LessonAccessResponseDto checkLessonAccess(Integer lessonId, Integer currentUserId);
}
