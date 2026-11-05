package com.edumy.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiCategorySuggestRequest {
    private String title;
    private String description;
}
