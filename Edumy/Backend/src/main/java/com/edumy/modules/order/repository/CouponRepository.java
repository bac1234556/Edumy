package com.edumy.modules.order.repository;

import com.edumy.modules.order.model.Coupon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Integer> {
    Coupon findByCode(String code);
    Coupon findByCodeIgnoreCase(String code);
}
