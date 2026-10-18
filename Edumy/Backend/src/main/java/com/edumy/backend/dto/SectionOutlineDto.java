package com.edumy.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SectionOutlineDto {
    private String title;
    private List<LessonOutlineDto> lessons = new ArrayList<>();
}
