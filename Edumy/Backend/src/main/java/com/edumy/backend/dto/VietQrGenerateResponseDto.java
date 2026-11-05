package com.edumy.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VietQrGenerateResponseDto {
    private Integer orderId;
    private BigDecimal amount;
    private String transferContent;
    private String bankId;
    private String bankName;
    private String accountNo;
    private String accountName;
    private String qrImageUrl;
    private String status;
    private LocalDateTime expiresAt;
    private Integer firstCourseId;
    private String message;
}
