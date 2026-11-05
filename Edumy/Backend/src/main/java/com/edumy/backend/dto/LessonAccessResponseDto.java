package com.edumy.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonAccessResponseDto {
    private Integer lessonId;
    private Integer courseId;
    private Integer sectionId;
    private String title;
    private String videoUrl;
    private Integer duration;
    private Boolean isPreview;
    private Boolean canPlay;
    private String message;
}
