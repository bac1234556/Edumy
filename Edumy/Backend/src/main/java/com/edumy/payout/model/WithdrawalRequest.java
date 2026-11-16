package com.edumy.payout.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payout_withdrawals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "InstructorId", nullable = false)
    private Integer instructorId;

    @Column(name = "Amount", nullable = false)
    private Long amount;

    @Column(name = "BankName", length = 128)
    private String bankName;

    @Column(name = "AccountNumberLast4", length = 16)
    private String accountNumberLast4;

    @Column(name = "AccountHolder", length = 128)
    private String accountHolder;

    @Column(name = "Status", nullable = false, length = 32)
    @Builder.Default
    private String status = "requested"; // "requested" | "paid" | "rejected" | "cancelled"

    @Column(name = "TransferReference", length = 128)
    private String transferReference;

    @Column(name = "RejectReason", length = 500)
    private String rejectReason;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "ProcessedAt")
    private LocalDateTime processedAt;
}
