package com.edumy.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
class CourseTagId implements Serializable {
    private Integer courseId;
    private Integer tagId;
}

@Entity
@Table(name = "CourseTags")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(CourseTagId.class)
public class CourseTag {
    @Id
    @Column(name = "CourseId")
    private Integer courseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CourseId", insertable = false, updatable = false)
    private Course course;
    
    @Id
    @Column(name = "TagId")
    private Integer tagId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TagId", insertable = false, updatable = false)
    private Tag tag;
}
