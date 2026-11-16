package com.edumy.authoring.api;

import java.util.Objects;

public record CourseHidden(Long courseId, String slug, String reason) {

    public CourseHidden {
        Objects.requireNonNull(courseId, "courseId");
        Objects.requireNonNull(slug, "slug");
        Objects.requireNonNull(reason, "reason");
    }
}
