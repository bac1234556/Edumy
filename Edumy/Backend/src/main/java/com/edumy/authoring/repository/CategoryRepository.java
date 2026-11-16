package com.edumy.authoring.repository;

import com.edumy.authoring.model.Category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Category findByName(String name);
    java.util.List<Category> findByParentCategoryIdIsNull();
}
