package com.edumy.authoring.repository;

import com.edumy.authoring.model.CourseSection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseSectionRepository extends JpaRepository<CourseSection, Integer> {
    List<CourseSection> findByCourseId(Integer courseId);
    List<CourseSection> findByCourseIdOrderByOrderIndexAsc(Integer courseId);
}
