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
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "sections", "enrollments", "reviews", "orderItems", "wishlists", "courseCoupons", "certificates", "mlAnalyses", "courseTags"})
    private Course course;
    
    @Id
    @Column(name = "CouponId")
    private Integer couponId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CouponId", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Coupon coupon;
}
