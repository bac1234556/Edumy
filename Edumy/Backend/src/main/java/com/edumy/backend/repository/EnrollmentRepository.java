package com.edumy.backend.repository;

import com.edumy.backend.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {
    boolean existsByUserIdAndCourseId(Integer userId, Integer courseId);
    boolean existsByUserIdAndCourseIdAndStatus(Integer userId, Integer courseId, String status);
    List<Enrollment> findByUserId(Integer userId);
    List<Enrollment> findByCourseId(Integer courseId);
    Enrollment findByUserIdAndCourseId(Integer userId, Integer courseId);
    Optional<Enrollment> findFirstByUserIdAndCourseId(Integer userId, Integer courseId);
}
