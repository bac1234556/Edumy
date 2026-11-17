package com.edumy.authoring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "CourseCoInstructors", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_CourseCoInstructors_Course_Instructor", columnNames = {"CourseId", "InstructorId"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseCoInstructor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CourseId", nullable = false)
    private Integer courseId;

    @Column(name = "InstructorId", nullable = false)
    private Integer instructorId;

    @Column(name = "ShareBps", nullable = false)
    @Builder.Default
    private Integer shareBps = 0;

    @Column(name = "Status", nullable = false, length = 32)
    @Builder.Default
    private String status = "INVITED"; // INVITED | ACCEPTED | DECLINED | REMOVED

    @Column(name = "InvitedAt")
    private LocalDateTime invitedAt;

    @Column(name = "RespondedAt")
    private LocalDateTime respondedAt;
}
