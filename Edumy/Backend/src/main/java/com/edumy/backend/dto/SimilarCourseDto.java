package com.edumy.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimilarCourseDto {
    private Integer courseId;
    private String title;
    private String shortDescription;
    private Double price;
    private Double originalPrice;
    private String thumbnailUrl;
    private Double averageRating;
    private Integer reviewCount;
    private Integer enrollmentCount;
    private Integer positiveRatingPercent;
    private String instructorName;
    private String instructorHeadline;
    private String categoryName;
    private String recommendationReason;
}
