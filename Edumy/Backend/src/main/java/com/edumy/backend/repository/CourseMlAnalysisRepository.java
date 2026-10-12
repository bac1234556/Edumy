package com.edumy.backend.repository;

import com.edumy.backend.model.CourseMlAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseMlAnalysisRepository extends JpaRepository<CourseMlAnalysis, Integer> {
    long countByRiskLevel(String riskLevel);
    long countByStatusIn(java.util.List<String> statuses);
    java.util.List<CourseMlAnalysis> findTop20ByOrderByCreatedAtDesc();
}
