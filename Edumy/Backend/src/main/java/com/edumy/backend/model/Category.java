package com.edumy.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CategoryId")
    private Integer categoryId;

    @Column(name = "Name", nullable = false)
    private String name;

    @Column(name = "Description")
    private String description;
    
    @Column(name = "ParentCategoryId")
    private Integer parentCategoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ParentCategoryId", insertable = false, updatable = false)
    @JsonIgnoreProperties({"parentCategory", "subCategories", "courses"})
    private Category parentCategory;

    @OneToMany(mappedBy = "parentCategory")
    @Builder.Default
    @JsonIgnoreProperties({"parentCategory", "subCategories", "courses"})
    private List<Category> subCategories = new ArrayList<>();
    
    @OneToMany(mappedBy = "category")
    @Builder.Default
    @JsonIgnore
    private List<Course> courses = new ArrayList<>();
}
