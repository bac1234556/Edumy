package com.edumy.modules.learning.qa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSummaryResponse {
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
    private long answerCount;
    private boolean hasInstructorAnswer;
    private String visibilityStatus;
    private LocalDateTime createdAt;

    public Long getQuestionId() {
        return id;
    }

    public String getContentSnippet() {
        return content;
    }
}
