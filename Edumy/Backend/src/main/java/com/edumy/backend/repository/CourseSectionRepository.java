package com.edumy.backend.repository;

import com.edumy.backend.model.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseSectionRepository extends JpaRepository<CourseSection, Integer> {
    List<CourseSection> findByCourseId(Integer courseId);
}
