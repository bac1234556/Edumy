package com.edumy.backend.repository;

import com.edumy.backend.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Integer> {
    Certificate findByUserIdAndCourseId(Integer userId, Integer courseId);
    Certificate findByCertificateUrl(String certificateUrl);
}
