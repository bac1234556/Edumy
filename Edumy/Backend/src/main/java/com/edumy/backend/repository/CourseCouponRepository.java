package com.edumy.backend.repository;

import com.edumy.backend.model.CourseCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseCouponRepository extends JpaRepository<CourseCoupon, Integer> {
    boolean existsByCouponId(Integer couponId);
    boolean existsByCouponIdAndCourseId(Integer couponId, Integer courseId);
    List<CourseCoupon> findByCouponId(Integer couponId);
    List<CourseCoupon> findByCourseId(Integer courseId);
}
