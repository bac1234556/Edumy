package com.edumy.backend.dto;

import lombok.Data;

@Data
public class QuestionResultDto {
    private Integer questionId;
    private Integer selectedAnswerId;
    private Integer correctAnswerId;
    private Boolean isCorrect;
    private String explanation;
}
