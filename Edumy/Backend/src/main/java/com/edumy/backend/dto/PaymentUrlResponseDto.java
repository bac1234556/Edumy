package com.edumy.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentUrlResponseDto {
    private Integer orderId;
    private String paymentMethod;
    private String paymentUrl;
    private String transactionRef;
    private BigDecimal amount;
    private String message;
}
