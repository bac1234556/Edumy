package com.edumy.backend.repository;

import com.edumy.backend.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {
    boolean existsByUserIdAndCourseId(Integer userId, Integer courseId);
    boolean existsByUserIdAndCourseIdAndStatus(Integer userId, Integer courseId, String status);
    boolean existsByUserIdAndCourseIdAndStatusIgnoreCase(Integer userId, Integer courseId, String status);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Enrollment e WHERE e.userId = :userId AND e.courseId = :courseId AND (e.status IS NULL OR UPPER(e.status) = 'ACTIVE' OR UPPER(e.status) = 'COMPLETED')")
    boolean isUserEnrolledActive(@Param("userId") Integer userId, @Param("courseId") Integer courseId);

    default boolean existsByUserIdAndCourseId(Long userId, Long courseId) {
        if (userId == null || courseId == null) return false;
        return existsByUserIdAndCourseId(userId.intValue(), courseId.intValue());
    }

    default boolean existsByUserIdAndCourseIdAndStatus(Long userId, Long courseId, String status) {
        if (userId == null || courseId == null) return false;
        return existsByUserIdAndCourseIdAndStatus(userId.intValue(), courseId.intValue(), status);
    }

    default boolean isUserEnrolledActive(Long userId, Long courseId) {
        if (userId == null || courseId == null) return false;
        return isUserEnrolledActive(userId.intValue(), courseId.intValue());
    }

    List<Enrollment> findByUserId(Integer userId);
    List<Enrollment> findByCourseId(Integer courseId);
    Enrollment findByUserIdAndCourseId(Integer userId, Integer courseId);
    Optional<Enrollment> findFirstByUserIdAndCourseId(Integer userId, Integer courseId);
}
