package com.edumy.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SepayWebhookDto {
    // SePay standard fields
    private Long id;
    private String gateway;
    private String transactionDate;
    private String accountNumber;
    private String subAccount;
    private BigDecimal amountIn;
    private BigDecimal amountOut;
    private BigDecimal accumulated;
    private String code;
    private String transactionContent;
    private String referenceNumber;
    private String body;

    // SePay official payload fields
    private String transferType;
    private BigDecimal transferAmount;
    private String referenceCode;

    // Casso standard structure support
    private Integer error;
    private List<CassoTransactionDto> data;

    // Generic / Direct fallback fields
    private Integer orderId;
    private BigDecimal amount;
    private String content;
    private String description;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CassoTransactionDto {
        private Long id;
        private String tid;
        private String description;
        private BigDecimal amount;
        private BigDecimal cusum_balance;
        private String when;
        private String bank_sub_acc_id;
    }
}
