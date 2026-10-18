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
public class InstantCourseSearchDTO {
    private Integer id;
    private String title;
    private String thumbnailUrl;
    private String instructorName;
    private BigDecimal price;
}
