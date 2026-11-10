package com.edumy.modules.course.repository;

import com.edumy.modules.course.model.CourseTag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseTagRepository extends JpaRepository<CourseTag, Integer> {
}
