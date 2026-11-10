package com.edumy.modules.ml.repository;

import com.edumy.modules.ml.model.CourseMlAnalysis;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseMlAnalysisRepository extends JpaRepository<CourseMlAnalysis, Integer> {
    long countByRiskLevel(String riskLevel);
    long countByStatusIn(java.util.List<String> statuses);
    java.util.List<CourseMlAnalysis> findTop20ByOrderByCreatedAtDesc();
}
