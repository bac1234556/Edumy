package com.edumy.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.edumy.backend.model.Course;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseBundleResponse {
    private Course mainCourse;
    private Course bundledCourse;
    private Integer originalTotal;
    private Integer bundlePrice;
    private Integer discountPercent;
    private Integer savings;
}
