package com.edumy.modules.course.repository;

import com.edumy.modules.course.model.CourseSection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseSectionRepository extends JpaRepository<CourseSection, Integer> {
    List<CourseSection> findByCourseId(Integer courseId);
}
