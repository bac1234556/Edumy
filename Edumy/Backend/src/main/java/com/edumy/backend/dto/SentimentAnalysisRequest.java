package com.edumy.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SentimentAnalysisRequest {
    private Integer rating = 5;
    private String comment;
}
