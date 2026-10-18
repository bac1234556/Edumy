package com.edumy.backend.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class QuizDto {
    private Integer quizId;
    private String title = "";
    private String description;
    private Integer passingScore;
    private Integer timeLimitMinutes;
    private Integer courseSectionId;
    private List<QuestionDto> questions = new ArrayList<>();

    public Integer getId() { return quizId; }
    public void setId(Integer id) { this.quizId = id; }
}
