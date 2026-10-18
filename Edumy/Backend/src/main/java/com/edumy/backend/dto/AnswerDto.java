package com.edumy.backend.dto;

import lombok.Data;

@Data
public class AnswerDto {
    private Integer answerId;
    private String content = "";
    private Boolean isCorrect;

    public Integer getId() { return answerId; }
    public void setId(Integer id) { this.answerId = id; }
    public String getAnswerText() { return content; }
    public void setAnswerText(String answerText) { this.content = answerText; }
}
