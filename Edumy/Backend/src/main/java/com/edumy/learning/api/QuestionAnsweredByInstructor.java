package com.edumy.learning.api;

import java.util.Objects;

public record QuestionAnsweredByInstructor(Long questionId, Long answerId, Long courseId, Long askerId,
                                           Long instructorId, String questionTitle) {

    public QuestionAnsweredByInstructor {
        Objects.requireNonNull(questionId, "questionId");
        Objects.requireNonNull(answerId, "answerId");
        Objects.requireNonNull(courseId, "courseId");
        Objects.requireNonNull(askerId, "askerId");
        Objects.requireNonNull(instructorId, "instructorId");
        Objects.requireNonNull(questionTitle, "questionTitle");
    }
}
