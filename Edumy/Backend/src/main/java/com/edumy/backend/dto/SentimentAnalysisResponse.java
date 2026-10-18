package com.edumy.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SentimentAnalysisResponse {
    private String sentiment; // "POSITIVE", "NEUTRAL", "NEGATIVE"
    private String label;     // "Positive", "Neutral", "Negative"
    private Double score;
    private Double confidence;
    private String modelVersion = "BiLSTM_Hybrid_v2";
    private Boolean isFallback = false;
}
