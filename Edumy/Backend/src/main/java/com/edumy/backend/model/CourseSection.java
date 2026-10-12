package com.edumy.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CourseSections")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SectionId")
    private Integer sectionId;

    @Column(name = "CourseId")
    private Integer courseId;

    @Column(name = "Title", nullable = false)
    private String title;

    @Column(name = "OrderIndex")
    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CourseId", insertable = false, updatable = false)
    private Course course;
    
    @OneToMany(mappedBy = "section")
    @Builder.Default
    private List<Lesson> lessons = new ArrayList<>();

    @OneToMany(mappedBy = "courseSection")
    @Builder.Default
    private List<Quiz> quizzes = new ArrayList<>();
}
