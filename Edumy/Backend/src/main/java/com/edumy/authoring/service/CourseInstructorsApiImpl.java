package com.edumy.authoring.service;

import com.edumy.authoring.api.CourseInstructorsApi;
import com.edumy.authoring.controller.SpecUseCaseController;
import com.edumy.authoring.model.Course;
import com.edumy.authoring.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CourseInstructorsApiImpl implements CourseInstructorsApi {

    private final CourseRepository courseRepository;

    public CourseInstructorsApiImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public CourseInstructorsSnapshot getInstructorsForCourse(Integer courseId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            return null;
        }

        Integer mainInstructorId = course.getInstructorId();
        List<Map<String, Object>> coList = SpecUseCaseController.COURSE_CO_INSTRUCTORS.getOrDefault(courseId, List.of());
        List<InstructorShare> coInstructors = new ArrayList<>();

        for (Map<String, Object> m : coList) {
            if ("ACCEPTED".equalsIgnoreCase((String) m.get("status"))) {
                Integer instId = (Integer) m.get("instructorId");
                Object s = m.get("shareBps");
                int shareBps = 0;
                if (s instanceof Number n) {
                    shareBps = n.intValue();
                }
                coInstructors.add(new InstructorShare(instId, shareBps));
            }
        }

        return new CourseInstructorsSnapshot(mainInstructorId, coInstructors);
    }
}
