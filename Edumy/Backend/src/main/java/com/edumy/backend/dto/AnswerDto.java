package com.edumy.backend.dto;

import lombok.Data;

@Data
public class AnswerDto {
    private Integer answerId;
    private String content = "";
    private Boolean isCorrect;
}
