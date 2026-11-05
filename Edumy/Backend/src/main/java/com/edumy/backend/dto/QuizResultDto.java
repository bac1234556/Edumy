package com.edumy.backend.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class QuizResultDto {
    private Integer quizAttemptId;
    private Integer score;
    private Integer totalPoints;
    private Boolean passed;
    private List<QuestionResultDto> results = new ArrayList<>();
}
