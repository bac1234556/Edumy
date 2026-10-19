package com.edumy.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyNowRequest {
    @NotNull(message = "Mã khóa học (courseId) không được để trống")
    private Integer courseId;
    
    private String couponCode;
}
