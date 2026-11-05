package com.edumy.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

@Entity
@Table(name = "Coupons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "Code", nullable = false)
    private String code;

    @Column(name = "DiscountPercentage")
    private BigDecimal discountPercentage;

    @Column(name = "ExpiryDate")
    private LocalDateTime expiryDate;

    @Column(name = "IsActive")
    @Builder.Default
    private Boolean isActive = true;
    
    @OneToMany(mappedBy = "coupon")
    @Builder.Default
    private List<CourseCoupon> courseCoupons = new ArrayList<>();
}
