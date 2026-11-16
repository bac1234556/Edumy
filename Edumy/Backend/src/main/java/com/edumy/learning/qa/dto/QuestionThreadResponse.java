package com.edumy.learning.qa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionThreadResponse {
    private Long id;
    private Integer courseId;
    private String courseTitle;
    private Integer lessonId;
    private String title;
    private String content;
    private Integer askerId;
    private String askerName;
    private String askerAvatar;
    private Boolean isResolved;
    private LocalDateTime createdAt;
    @Builder.Default
    private List<AnswerResponse> answers = new ArrayList<>();

    public Long getQuestionId() {
        return id;
    }
}
