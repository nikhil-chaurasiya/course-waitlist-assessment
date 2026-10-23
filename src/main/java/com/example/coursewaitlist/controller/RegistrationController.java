package com.example.coursewaitlist.controller;

import com.example.coursewaitlist.dto.ApiModels;
import com.example.coursewaitlist.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RegistrationController {
    private final RegistrationService service;
    public RegistrationController(RegistrationService service) { this.service = service; }

    @PostMapping("/courses")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiModels.CourseResponse createCourse(@Valid @RequestBody ApiModels.CreateCourseRequest request) {
        return service.createCourse(request);
    }

    @GetMapping("/courses/{courseId}")
    public ApiModels.CourseResponse getCourse(@PathVariable Long courseId) { return service.getCourse(courseId); }

    @PostMapping("/courses/{courseId}/enrollments")
    public ApiModels.OperationResponse enroll(@PathVariable Long courseId, @Valid @RequestBody ApiModels.EnrollRequest request) {
        return service.enroll(courseId, request);
    }

    @DeleteMapping("/courses/{courseId}/enrollments/{learnerId}")
    public ApiModels.OperationResponse drop(@PathVariable Long courseId, @PathVariable String learnerId) {
        return service.drop(courseId, learnerId);
    }
}
