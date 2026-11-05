package com.edumy.backend.repository;

import com.edumy.backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Category findByName(String name);
    java.util.List<Category> findByParentCategoryIdIsNull();
}
