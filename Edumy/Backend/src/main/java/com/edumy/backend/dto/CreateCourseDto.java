package com.edumy.backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateCourseDto {
    private String title = "";
    private String description;
    private BigDecimal price;
    private String level = "Beginner";
    private Integer categoryId;
    private String thumbnailUrl;
}
