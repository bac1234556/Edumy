package com.edumy.backend.controller;

import com.edumy.backend.model.*;
import com.edumy.backend.dto.*;
import com.edumy.backend.repository.*;
import com.edumy.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.net.URI;

@RestController
@RequestMapping({"/api/Categories", "/api/categories"})
public class CategoriesController {

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<?> getCategories() {
        List<Category> categories = categoryRepository.findByParentCategoryIdIsNull();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCategory(@PathVariable Integer id) {
        return categoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> createCategory(@RequestBody Category category) {
        Category saved = categoryRepository.save(category);
        return ResponseEntity.created(URI.create("/api/Categories/" + saved.getCategoryId())).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> updateCategory(@PathVariable Integer id, @RequestBody Category category) {
        if (!id.equals(category.getCategoryId())) {
            return ResponseEntity.badRequest().build();
        }

        if (!categoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        categoryRepository.save(category);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        if (!categoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        categoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
