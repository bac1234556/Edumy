package com.edumy.authoring.api;

import java.util.List;

public interface CourseInstructorsApi {
    record InstructorShare(Integer instructorId, int shareBps) {}
    record CourseInstructorsSnapshot(Integer mainInstructorId, List<InstructorShare> coInstructors) {}

    CourseInstructorsSnapshot getInstructorsForCourse(Integer courseId);
}
