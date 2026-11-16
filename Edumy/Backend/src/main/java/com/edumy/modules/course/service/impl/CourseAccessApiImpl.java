package com.edumy.modules.course.service.impl;

import com.edumy.authoring.api.CourseAccessApi;
import com.edumy.modules.course.model.Course;
import com.edumy.modules.course.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CourseAccessApiImpl implements CourseAccessApi {

    private final CourseRepository courseRepository;

    public CourseAccessApiImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMainInstructor(Integer userId, Integer courseId) {
        if (userId == null || courseId == null) {
            return false;
        }
        return courseRepository.findById(courseId)
                .map(c -> userId.equals(c.getInstructorId()))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCourseInstructor(Integer userId, Integer courseId) {
        return isMainInstructor(userId, courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasInstructorRelation(Integer userId, Integer courseId) {
        return isMainInstructor(userId, courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Integer> findInstructorRelationCourseIds(Integer userId, Collection<Integer> courseIds) {
        if (userId == null || courseIds == null || courseIds.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Integer> result = new HashSet<>();
        for (Integer cid : courseIds) {
            if (hasInstructorRelation(userId, cid)) {
                result.add(cid);
            }
        }
        return Collections.unmodifiableSet(result);
    }
}
