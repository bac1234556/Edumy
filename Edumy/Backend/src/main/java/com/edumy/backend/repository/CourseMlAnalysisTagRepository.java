package com.edumy.backend.repository;

import com.edumy.backend.model.CourseMlAnalysisTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseMlAnalysisTagRepository extends JpaRepository<CourseMlAnalysisTag, Integer> {
}
