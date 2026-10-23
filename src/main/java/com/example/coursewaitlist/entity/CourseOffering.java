package com.example.coursewaitlist.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "course_offerings")
public class CourseOffering {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private int capacity;

    protected CourseOffering() { }
    public CourseOffering(String name, int capacity) { this.name = name; this.capacity = capacity; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }
}
