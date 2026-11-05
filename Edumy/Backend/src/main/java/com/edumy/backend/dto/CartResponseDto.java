package com.edumy.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartResponseDto {
    private Integer id;
    private Integer userId;
    private Integer totalItems;
    private BigDecimal totalPrice;
    private BigDecimal totalAmount;
    private BigDecimal payableTotal;
    private Integer payableItemsCount;
    private Boolean allPaid;
    private List<CartItemDto> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CartItemDto {
        private Integer id;
        private Integer courseId;
        private String addedAt;
        private Boolean isEnrolled;
        private Boolean isPaid;
        private CourseSummaryDto course;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CourseSummaryDto {
        private String title;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private String thumbnailUrl;
        private String instructorName;
        private Double averageRating;
        private Integer reviewsCount;
    }
}
