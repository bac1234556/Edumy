package com.edumy.modules.course.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CourseQueryDto {
    private String search;
    private String q;
    private Integer categoryId;
    private String level;
    private String priceType; // ALL, FREE, PAID (UC-G03)
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sortBy; // price, rating, newest
    private String sortOrder; // asc, desc
    private Integer pageNumber = 1;
    private Integer pageSize = 12;
}
