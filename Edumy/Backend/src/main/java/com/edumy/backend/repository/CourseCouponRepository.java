package com.edumy.backend.repository;

import com.edumy.backend.model.CourseCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseCouponRepository extends JpaRepository<CourseCoupon, Integer> {
}
