package com.edumy.authoring.api;

import java.util.Collection;
import java.util.Set;

/**
 * Public Spring Modulith port for course access and instructor relationship checks (authoring module API).
 * Used by commerce (cart validation, order creation) and learning (Q&A permission).
 */
public interface CourseAccessApi {

    /**
     * Checks if the user is the main instructor of the course.
     */
    boolean isMainInstructor(Integer userId, Integer courseId);

    /**
     * Checks if the user is the main instructor or accepted co-instructor of the course.
     */
    boolean isCourseInstructor(Integer userId, Integer courseId);

    /**
     * BR-10 / BR-32: Checks whether the user has any instructor relation to the course
     * (used to prevent instructors from buying their own course).
     */
    boolean hasInstructorRelation(Integer userId, Integer courseId);

    /**
     * Batch check for instructor relation across multiple course IDs.
     */
    Set<Integer> findInstructorRelationCourseIds(Integer userId, Collection<Integer> courseIds);
}
