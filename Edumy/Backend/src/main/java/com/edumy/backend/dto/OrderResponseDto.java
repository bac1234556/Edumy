package com.edumy.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDto {
    private Integer orderId;
    private String message;
    private String paymentUrl;
    private String status;
    private BigDecimal amount;
    private Boolean isFree;
    private Integer courseId;
    private String courseTitle;

    // VietQR details
    private String qrImageUrl;
    private String transferContent;
    private String accountNo;
    private String accountName;
    private String bankName;
    private String bankId;
}
