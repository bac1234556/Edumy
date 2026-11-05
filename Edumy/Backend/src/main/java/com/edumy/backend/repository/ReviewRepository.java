package com.edumy.backend.repository;

import com.edumy.backend.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByCourseId(Integer courseId);
    boolean existsByUserIdAndCourseId(Integer userId, Integer courseId);
}
