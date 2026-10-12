package com.edumy.backend.repository;

import com.edumy.backend.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Integer> {
    Coupon findByCode(String code);
    Coupon findByCodeIgnoreCase(String code);
}
