package com.example.coursewaitlist.service;

import com.example.coursewaitlist.dto.ApiModels;
import com.example.coursewaitlist.entity.CourseOffering;
import com.example.coursewaitlist.entity.Enrollment;
import com.example.coursewaitlist.entity.WaitlistEntry;
import com.example.coursewaitlist.repository.CourseOfferingRepository;
import com.example.coursewaitlist.repository.EnrollmentRepository;
import com.example.coursewaitlist.repository.WaitlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RegistrationService {
    private final CourseOfferingRepository courses;
    private final EnrollmentRepository enrollments;
    private final WaitlistRepository waitlist;

    public RegistrationService(CourseOfferingRepository courses, EnrollmentRepository enrollments, WaitlistRepository waitlist) {
        this.courses = courses; this.enrollments = enrollments; this.waitlist = waitlist;
    }

    @Transactional
    public ApiModels.CourseResponse createCourse(ApiModels.CreateCourseRequest request) {
        CourseOffering saved = courses.save(new CourseOffering(request.name().trim(), request.capacity()));
        return response(saved);
    }

    @Transactional
    public ApiModels.OperationResponse enroll(Long courseId, ApiModels.EnrollRequest request) {
        CourseOffering course = lockedCourse(courseId);
        String learnerId = request.learnerId().trim();
        if (enrollments.existsByCourse_IdAndLearnerId(courseId, learnerId)
                || waitlist.existsByCourse_IdAndLearnerId(courseId, learnerId)) {
            return result("DUPLICATE", "Learner is already enrolled or waitlisted.", learnerId, null);
        }
        if (!request.prerequisiteSatisfied()) {
            return result("PREREQUISITE_NOT_MET", "Learner does not satisfy the prerequisite.", learnerId, null);
        }
        if (enrollments.countByCourse_Id(courseId) < course.getCapacity()) {
            enrollments.save(new Enrollment(course, learnerId));
            return result("ENROLLED", "Learner enrolled successfully.", learnerId, null);
        }
        waitlist.save(new WaitlistEntry(course, learnerId, true));
        return result("WAITLISTED", "Course is full; learner added to the waitlist.", learnerId, null);
    }

    @Transactional
    public ApiModels.OperationResponse drop(Long courseId, String learnerIdInput) {
        lockedCourse(courseId);
        String learnerId = learnerIdInput == null ? "" : learnerIdInput.trim();
        var existing = enrollments.findByCourse_IdAndLearnerId(courseId, learnerId);
        if (existing.isEmpty()) {
            return result("NOT_ENROLLED", "Learner is not currently enrolled; nothing was changed.", learnerId, null);
        }
        enrollments.delete(existing.get());
        String promoted = promoteFirstEligible(courseId);
        return result("DROPPED", "Enrollment dropped successfully.", learnerId, promoted);
    }

    @Transactional(readOnly = true)
    public ApiModels.CourseResponse getCourse(Long courseId) {
        CourseOffering course = courses.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
        return response(course);
    }

    private CourseOffering lockedCourse(Long courseId) {
        return courses.findByIdForUpdate(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
    }

    private String promoteFirstEligible(Long courseId) {
        List<WaitlistEntry> entries = waitlist.findAllByCourse_IdOrderByCreatedAtAscIdAsc(courseId);
        for (WaitlistEntry entry : entries) {
            if (entry.isPrerequisiteSatisfied()) {
                waitlist.delete(entry);
                enrollments.save(new Enrollment(entry.getCourse(), entry.getLearnerId()));
                return entry.getLearnerId();
            }
            // Policy: retain an ineligible entry in its original position and inspect later entries.
        }
        return null;
    }

    private ApiModels.CourseResponse response(CourseOffering course) {
        return new ApiModels.CourseResponse(course.getId(), course.getName(), course.getCapacity(),
                enrollments.countByCourse_Id(course.getId()), waitlist.countByCourse_Id(course.getId()));
    }

    private ApiModels.OperationResponse result(String status, String message, String learnerId, String promoted) {
        return new ApiModels.OperationResponse(status, message, learnerId, promoted);
    }

    public static class CourseNotFoundException extends RuntimeException {
        public CourseNotFoundException(Long id) { super("Course offering " + id + " was not found."); }
    }
}
