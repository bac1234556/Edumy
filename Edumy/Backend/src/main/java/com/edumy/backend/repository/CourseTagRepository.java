package com.edumy.backend.repository;

import com.edumy.backend.model.CourseTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseTagRepository extends JpaRepository<CourseTag, Integer> {
}
