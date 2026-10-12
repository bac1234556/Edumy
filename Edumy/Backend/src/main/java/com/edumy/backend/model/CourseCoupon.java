package com.edumy.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
class CourseCouponId implements Serializable {
    private Integer courseId;
    private Integer couponId;
}

@Entity
@Table(name = "CourseCoupons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(CourseCouponId.class)
public class CourseCoupon {
    @Id
    @Column(name = "CourseId")
    private Integer courseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CourseId", insertable = false, updatable = false)
    private Course course;
    
    @Id
    @Column(name = "CouponId")
    private Integer couponId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CouponId", insertable = false, updatable = false)
    private Coupon coupon;
}
