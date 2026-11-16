package com.edumy.modules.learning.qa.repository;

import com.edumy.modules.learning.qa.entity.CourseQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface CourseQuestionRepository extends JpaRepository<CourseQuestion, Long> {

    @Query("SELECT q FROM CourseQuestion q WHERE q.courseId = :courseId AND q.visibilityStatus = 'VISIBLE' " +
            "AND (:lessonId IS NULL OR q.lessonId = :lessonId)")
    Page<CourseQuestion> findForStudentCourse(@Param("courseId") Integer courseId,
                                             @Param("lessonId") Integer lessonId,
                                             Pageable pageable);

    /**
     * UC-I22: Waiting queue for instructor, oldest first (cũ nhất trước).
     * Defaults to unanswered questions first.
     */
    @Query("SELECT q FROM CourseQuestion q WHERE q.courseId IN :courseIds AND q.visibilityStatus = 'VISIBLE' " +
            "AND (:unansweredOnly = false OR NOT EXISTS (" +
            "  SELECT a.id FROM QuestionAnswer a WHERE a.questionId = q.id AND a.authorType = 'INSTRUCTOR' AND a.visibilityStatus = 'VISIBLE'" +
            "))")
    Page<CourseQuestion> findForInstructorOldestQueue(@Param("courseIds") Collection<Integer> courseIds,
                                                     @Param("unansweredOnly") boolean unansweredOnly,
                                                     Pageable pageable);

    long countByCourseIdAndVisibilityStatus(Integer courseId, String visibilityStatus);
}
