package com.edumy.authoring.repository;

import com.edumy.authoring.model.Course;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {
    List<Course> findByInstructorId(Integer instructorId);
    List<Course> findByCategoryId(Integer categoryId);
    long countByCategoryId(Integer categoryId);
    Course findBySlug(String slug);
    Optional<Course> findFirstBySlugIgnoreCase(String slug);

    List<Course> findTop4ByStatusIgnoreCaseOrderByCourseIdDesc(String status);

    @Query("SELECT c FROM Course c WHERE LOWER(c.status) = 'published' AND LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY c.title ASC")
    List<Course> searchInstant(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT c FROM Course c
            WHERE LOWER(c.status) = 'published'
              AND (:q IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(COALESCE(c.description, '')) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:categoryId IS NULL OR c.categoryId = :categoryId)
              AND (:priceType IS NULL
                   OR (:priceType = 'free' AND (c.price IS NULL OR c.price <= 0))
                   OR (:priceType = 'paid' AND c.price > 0))
            """)
    Page<Course> findPublishedCatalog(
            @Param("q") String q,
            @Param("categoryId") Integer categoryId,
            @Param("priceType") String priceType,
            Pageable pageable);
}
