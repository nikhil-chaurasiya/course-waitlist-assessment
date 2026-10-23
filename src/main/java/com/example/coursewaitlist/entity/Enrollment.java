package com.example.coursewaitlist.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "enrollments", uniqueConstraints = @UniqueConstraint(name = "uk_enrollment_course_learner", columnNames = {"course_id", "learner_id"}))
public class Enrollment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "course_id", nullable = false)
    private CourseOffering course;
    @Column(name = "learner_id", nullable = false)
    private String learnerId;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Enrollment() { }
    public Enrollment(CourseOffering course, String learnerId) { this.course = course; this.learnerId = learnerId; this.createdAt = Instant.now(); }
    public Long getId() { return id; }
    public CourseOffering getCourse() { return course; }
    public String getLearnerId() { return learnerId; }
    public Instant getCreatedAt() { return createdAt; }
}
