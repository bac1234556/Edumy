package com.edumy.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "CourseMlAnalysisTags")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseMlAnalysisTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "CourseMlAnalysisId")
    private Integer courseMlAnalysisId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CourseMlAnalysisId", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "tags", "course", "approvedByUser"})
    private CourseMlAnalysis courseMlAnalysis;
    
    @Column(name = "TagName", nullable = false)
    private String tagName;

    @Column(name = "Confidence")
    private Double confidence;
}
