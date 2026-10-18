package com.edumy.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiCategorySuggestResponse {
    private Integer suggestedCategoryId;
    private String categoryName;
    private Double confidenceScore;
    private List<String> suggestedTags = new ArrayList<>();
    private List<SectionOutlineDto> curriculumOutline = new ArrayList<>();
    private String modelType = "MLP_Semantic_Hybrid";
    private Boolean isFallback = false;
}
