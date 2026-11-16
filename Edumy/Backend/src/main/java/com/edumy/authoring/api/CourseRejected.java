package com.edumy.authoring.api;

import java.util.Objects;

public record CourseRejected(Long courseId, String slug, String reason) {

    public CourseRejected {
        Objects.requireNonNull(courseId, "courseId");
        Objects.requireNonNull(slug, "slug");
        Objects.requireNonNull(reason, "reason");
    }
}
