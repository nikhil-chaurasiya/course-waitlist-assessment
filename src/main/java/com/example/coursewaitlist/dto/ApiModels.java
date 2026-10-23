package com.example.coursewaitlist.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public final class ApiModels {
    private ApiModels() { }
    public record CreateCourseRequest(@NotBlank String name, @Min(1) int capacity) { }
    public record EnrollRequest(@NotBlank String learnerId, boolean prerequisiteSatisfied) { }
    public record OperationResponse(String status, String message, String learnerId, String promotedLearnerId) { }
    public record CourseResponse(Long id, String name, int capacity, long enrolledCount, long waitlistCount) { }
}
