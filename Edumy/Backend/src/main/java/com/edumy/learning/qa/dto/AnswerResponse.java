package com.edumy.learning.qa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResponse {
    private Long id;
    private Integer authorId;
    private String authorName;
    private String authorAvatar;
    private String authorType; // STUDENT, INSTRUCTOR
    private String content;
    private String visibilityStatus;
    private LocalDateTime createdAt;

    public Long getAnswerId() {
        return id;
    }
}
