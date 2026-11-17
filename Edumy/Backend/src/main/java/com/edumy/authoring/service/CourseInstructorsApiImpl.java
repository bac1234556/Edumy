package com.edumy.authoring.service;

import com.edumy.authoring.api.CourseInstructorsApi;
import com.edumy.authoring.model.Course;
import com.edumy.authoring.model.CourseCoInstructor;
import com.edumy.authoring.repository.CourseCoInstructorRepository;
import com.edumy.authoring.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CourseInstructorsApiImpl implements CourseInstructorsApi {

    private final CourseRepository courseRepository;
    private final CourseCoInstructorRepository courseCoInstructorRepository;

    public CourseInstructorsApiImpl(CourseRepository courseRepository,
                                    CourseCoInstructorRepository courseCoInstructorRepository) {
        this.courseRepository = courseRepository;
        this.courseCoInstructorRepository = courseCoInstructorRepository;
    }

    @Override
    public CourseInstructorsSnapshot getInstructorsForCourse(Integer courseId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            return null;
        }

        Integer mainInstructorId = course.getInstructorId();
        List<CourseCoInstructor> accepted = courseCoInstructorRepository.findByCourseIdAndStatus(courseId, "ACCEPTED");
        List<InstructorShare> coInstructors = new ArrayList<>();
        for (CourseCoInstructor ci : accepted) {
            coInstructors.add(new InstructorShare(ci.getInstructorId(), ci.getShareBps() != null ? ci.getShareBps() : 0));
        }

        return new CourseInstructorsSnapshot(mainInstructorId, coInstructors);
    }
}
