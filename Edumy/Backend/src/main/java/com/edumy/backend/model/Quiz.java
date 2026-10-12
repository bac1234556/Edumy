package com.edumy.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Quizzes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "QuizId")
    private Integer quizId;

    @Column(name = "Title", nullable = false)
    private String title;

    @Column(name = "Description")
    private String description;

    @Column(name = "PassingScore")
    @Builder.Default
    private Integer passingScore = 80;

    @Column(name = "TimeLimitMinutes")
    private Integer timeLimitMinutes;
    
    @Column(name = "CourseSectionId")
    private Integer courseSectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CourseSectionId", insertable = false, updatable = false)
    private CourseSection courseSection;
    
    @OneToMany(mappedBy = "quiz")
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "quiz")
    @Builder.Default
    private List<QuizAttempt> quizAttempts = new ArrayList<>();
}
