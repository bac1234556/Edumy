package com.edumy.backend.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class QuestionDto {
    private Integer questionId;
    private String content = "";
    private Integer points;
    private String explanation;
    private List<AnswerDto> answers = new ArrayList<>();
}
