package com.edumy.authoring.repository;

import com.edumy.authoring.model.CourseTag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseTagRepository extends JpaRepository<CourseTag, Integer> {
}
