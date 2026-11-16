package com.edumy.authoring.api;

import java.util.Objects;

public record CourseApproved(Long courseId, String slug) {

    public CourseApproved {
        Objects.requireNonNull(courseId, "courseId");
        Objects.requireNonNull(slug, "slug");
    }
}
