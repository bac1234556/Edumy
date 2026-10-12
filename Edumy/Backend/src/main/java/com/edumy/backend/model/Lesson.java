package com.edumy.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "Lessons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LessonId")
    private Integer lessonId;

    @Column(name = "SectionId")
    private Integer sectionId;

    @Column(name = "Title", nullable = false)
    private String title;

    @Column(name = "VideoUrl")
    private String videoUrl;

    @Column(name = "Duration")
    private Integer duration;

    @Column(name = "OrderIndex")
    private Integer orderIndex;

    @Column(name = "IsPreview")
    @Builder.Default
    private Boolean isPreview = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SectionId", insertable = false, updatable = false)
    private CourseSection section;
}
