package com.edumy.backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CourseQueryDto {
    private String search;
    private Integer categoryId;
    private String level;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sortBy; // price, rating, newest
    private String sortOrder; // asc, desc
    private Integer pageNumber = 1;
    private Integer pageSize = 12;
}
