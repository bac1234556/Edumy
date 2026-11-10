package com.edumy.modules.course.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonOutlineDto {
    private String title;
    private Integer durationMinutes = 15;
    private Boolean isPreview = false;
}
