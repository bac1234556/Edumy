package com.edumy.payout.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payout_earnings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EarningEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "OrderId", nullable = false)
    private Integer orderId;

    @Column(name = "OrderCode", length = 64)
    private String orderCode;

    @Column(name = "OrderItemId")
    private Integer orderItemId;

    @Column(name = "CourseId", nullable = false)
    private Integer courseId;

    @Column(name = "CourseTitle", length = 255)
    private String courseTitle;

    @Column(name = "InstructorId", nullable = false)
    private Integer instructorId;

    @Column(name = "Role", length = 32)
    private String role; // "main" | "co"

    @Column(name = "UnitPrice")
    private Long unitPrice;

    @Column(name = "PlatformFeeBps")
    private Integer platformFeeBps; // default 3000

    @Column(name = "ShareBps")
    private Integer shareBps;

    @Column(name = "Amount", nullable = false)
    private Long amount;

    @Column(name = "EarnedAt", nullable = false)
    private LocalDateTime earnedAt;

    @Column(name = "MaturesAt")
    private LocalDateTime maturesAt;
}
