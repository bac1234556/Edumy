package com.edumy.learning.review.repository;

import com.edumy.learning.review.model.Review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByCourseId(Integer courseId);
    Page<Review> findByCourseIdOrderByCreatedAtDesc(Integer courseId, Pageable pageable);
    long countByCourseId(Integer courseId);
    boolean existsByUserIdAndCourseId(Integer userId, Integer courseId);

    @Query("SELECT r.courseId, COUNT(r) FROM Review r WHERE r.courseId IN :courseIds GROUP BY r.courseId")
    List<Object[]> countByCourseIds(@Param("courseIds") List<Integer> courseIds);
}
