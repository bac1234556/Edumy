package com.edumy.authoring.api;

import java.util.Objects;

public record CoInstructorInvited(Long invitationId, Long courseId, Long instructorId, Long invitedBy) {

    public CoInstructorInvited {
        Objects.requireNonNull(invitationId, "invitationId");
        Objects.requireNonNull(courseId, "courseId");
        Objects.requireNonNull(instructorId, "instructorId");
        Objects.requireNonNull(invitedBy, "invitedBy");
    }
}
