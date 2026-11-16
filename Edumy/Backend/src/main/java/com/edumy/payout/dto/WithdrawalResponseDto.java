package com.edumy.payout.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalResponseDto {
    private Integer id;
    private Long amount;
    private String bankName;
    private String accountNumberLast4;
    private String accountHolder;
    private String status;
    private String transferReference;
    private String rejectReason;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
