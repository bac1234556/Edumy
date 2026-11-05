package com.edumy.backend.dto;

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
public class OrderStatusResponseDto {
    private Integer orderId;
    private String status;
    private Boolean isPaid;
    private BigDecimal totalAmount;
    private List<Integer> courseIds;
    private Integer firstCourseId;
    private String message;
}
