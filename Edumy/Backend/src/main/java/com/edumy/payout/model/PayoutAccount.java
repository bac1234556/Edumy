package com.edumy.payout.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payout_accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutAccount {

    @Id
    @Column(name = "InstructorId")
    private Integer instructorId;

    @Column(name = "BankName", nullable = false, length = 128)
    private String bankName;

    @Column(name = "AccountNumber", nullable = false, length = 128)
    private String accountNumber;

    @Column(name = "AccountHolder", nullable = false, length = 128)
    private String accountHolder;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
}
