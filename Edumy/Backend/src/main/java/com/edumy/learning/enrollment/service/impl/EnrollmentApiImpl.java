package com.edumy.learning.enrollment.service.impl;

import com.edumy.learning.api.EnrollmentApi;
import com.edumy.learning.enrollment.model.Enrollment;
import com.edumy.learning.enrollment.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnrollmentApiImpl implements EnrollmentApi {

    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentApiImpl(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean owns(Integer studentId, Integer courseId) {
        if (studentId == null || courseId == null) {
            return false;
        }
        return enrollmentRepository.isUserEnrolledActive(studentId, courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Integer> findOwnedCourseIds(Integer studentId, Collection<Integer> courseIds) {
        if (studentId == null || courseIds == null || courseIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<Enrollment> enrollments = enrollmentRepository.findByUserId(studentId);
        Set<Integer> target = new HashSet<>(courseIds);
        return enrollments.stream()
                .filter(e -> "ACTIVE".equalsIgnoreCase(e.getStatus()) || "Completed".equalsIgnoreCase(e.getStatus()))
                .map(Enrollment::getCourseId)
                .filter(target::contains)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantForPaidOrder(Integer studentId, Collection<Integer> courseIds) {
        if (studentId == null || courseIds == null || courseIds.isEmpty()) {
            return;
        }
        for (Integer courseId : courseIds) {
            if (!owns(studentId, courseId)) {
                Enrollment enrollment = new Enrollment();
                enrollment.setUserId(studentId);
                enrollment.setCourseId(courseId);
                enrollment.setStatus("ACTIVE");
                enrollment.setProgressPercentage(0);
                enrollment.setEnrolledAt(LocalDateTime.now());
                enrollmentRepository.save(enrollment);
            }
        }
    }
}
