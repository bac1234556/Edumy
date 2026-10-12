package com.edumy.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CourseMlAnalyses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseMlAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "CourseId")
    private Integer courseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CourseId", insertable = false, updatable = false)
    private Course course;
    
    @Column(name = "PrimaryCategory")
    private String primaryCategory;

    @Column(name = "SubCategory")
    private String subCategory;

    @Column(name = "SuggestedLevel")
    private String suggestedLevel;

    @Column(name = "Confidence")
    private Double confidence;

    @Column(name = "QualityScore")
    private Integer qualityScore;

    @Column(name = "RiskLevel")
    private String riskLevel;

    @Column(name = "RawResponseJson", columnDefinition = "TEXT")
    private String rawResponseJson;

    @Column(name = "ModelVersion")
    private String modelVersion;

    @Column(name = "Status")
    @Builder.Default
    private String status = "Pending";
    
    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "ApprovedAt")
    private LocalDateTime approvedAt;

    @Column(name = "ApprovedByUserId")
    private Integer approvedByUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ApprovedByUserId", insertable = false, updatable = false)
    private User approvedByUser;
    
    @OneToMany(mappedBy = "courseMlAnalysis") // Assuming back-reference is courseMlAnalysis
    @Builder.Default
    private List<CourseMlAnalysisTag> tags = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
