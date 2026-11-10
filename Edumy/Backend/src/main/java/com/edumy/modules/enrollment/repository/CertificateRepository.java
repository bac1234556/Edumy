package com.edumy.modules.enrollment.repository;

import com.edumy.modules.enrollment.model.Certificate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Integer> {
    Certificate findByUserIdAndCourseId(Integer userId, Integer courseId);
    Certificate findByCertificateUrl(String certificateUrl);
    boolean existsByUserIdAndCourseId(Integer userId, Integer courseId);
}
