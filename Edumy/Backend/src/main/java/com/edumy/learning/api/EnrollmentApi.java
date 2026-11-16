package com.edumy.learning.api;

import java.util.Collection;
import java.util.Set;

/**
 * Public Spring Modulith port for enrollments (learning module API).
 * Used by commerce (cart, checkout) and authoring modules.
 */
public interface EnrollmentApi {

    /**
     * Checks if the student is currently enrolled in the course.
     */
    boolean owns(Integer studentId, Integer courseId);

    /**
     * Finds which of the given courseIds the student already owns (batch query).
     */
    Set<Integer> findOwnedCourseIds(Integer studentId, Collection<Integer> courseIds);

    /**
     * Grants enrollment for a paid order (ADR-004 ACID transaction).
     */
    void grantForPaidOrder(Integer studentId, Collection<Integer> courseIds);
}
