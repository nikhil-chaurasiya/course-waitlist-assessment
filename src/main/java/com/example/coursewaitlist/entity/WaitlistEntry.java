package com.example.coursewaitlist.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "waitlist_entries", uniqueConstraints = @UniqueConstraint(name = "uk_waitlist_course_learner", columnNames = {"course_id", "learner_id"}))
public class WaitlistEntry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "course_id", nullable = false)
    private CourseOffering course;
    @Column(name = "learner_id", nullable = false)
    private String learnerId;
    @Column(name = "prerequisite_satisfied", nullable = false)
    private boolean prerequisiteSatisfied;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected WaitlistEntry() { }
    public WaitlistEntry(CourseOffering course, String learnerId, boolean prerequisiteSatisfied) {
        this.course = course; this.learnerId = learnerId; this.prerequisiteSatisfied = prerequisiteSatisfied; this.createdAt = Instant.now();
    }
    public Long getId() { return id; }
    public CourseOffering getCourse() { return course; }
    public String getLearnerId() { return learnerId; }
    public boolean isPrerequisiteSatisfied() { return prerequisiteSatisfied; }
    public Instant getCreatedAt() { return createdAt; }
}
