package com.edumy.modules.ml.dto;

import com.edumy.modules.course.model.Course;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
