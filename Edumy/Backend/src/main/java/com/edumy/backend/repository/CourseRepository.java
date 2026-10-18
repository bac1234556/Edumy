package com.edumy.backend.repository;

import com.edumy.backend.model.Course;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {
    List<Course> findByInstructorId(Integer instructorId);
    List<Course> findByCategoryId(Integer categoryId);
    Course findBySlug(String slug);

    @Query("SELECT c FROM Course c WHERE LOWER(c.status) = 'published' AND LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY c.title ASC")
    List<Course> searchInstant(@Param("keyword") String keyword, Pageable pageable);
}
