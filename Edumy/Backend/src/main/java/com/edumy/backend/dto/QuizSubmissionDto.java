package com.edumy.backend.dto;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

@Data
public class QuizSubmissionDto {
    private Integer quizId;
    private Map<Integer, Integer> selectedAnswers = new HashMap<>();
}
