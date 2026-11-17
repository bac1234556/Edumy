package com.edumy.authoring.repository;

import com.edumy.authoring.model.CourseCoInstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseCoInstructorRepository extends JpaRepository<CourseCoInstructor, Integer> {
    List<CourseCoInstructor> findByCourseIdAndStatusNot(Integer courseId, String excludedStatus);
    List<CourseCoInstructor> findByCourseIdAndStatus(Integer courseId, String status);
    Optional<CourseCoInstructor> findFirstByCourseIdAndInstructorId(Integer courseId, Integer instructorId);
    List<CourseCoInstructor> findByInstructorIdAndStatusNot(Integer instructorId, String excludedStatus);
    List<CourseCoInstructor> findByInstructorIdAndStatus(Integer instructorId, String status);
}
