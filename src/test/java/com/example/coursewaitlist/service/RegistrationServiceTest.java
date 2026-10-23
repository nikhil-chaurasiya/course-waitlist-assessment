package com.example.coursewaitlist.service;

import com.example.coursewaitlist.dto.ApiModels;
import com.example.coursewaitlist.entity.CourseOffering;
import com.example.coursewaitlist.entity.WaitlistEntry;
import com.example.coursewaitlist.repository.CourseOfferingRepository;
import com.example.coursewaitlist.repository.EnrollmentRepository;
import com.example.coursewaitlist.repository.WaitlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class RegistrationServiceTest {
    @Autowired RegistrationService service;
    @Autowired CourseOfferingRepository courses;
    @Autowired EnrollmentRepository enrollments;
    @Autowired WaitlistRepository waitlist;
    private Long courseId;

    @BeforeEach
    void setUp() {
        waitlist.deleteAll();
        enrollments.deleteAll();
        courses.deleteAll();
        courseId = service.createCourse(new ApiModels.CreateCourseRequest("Distributed Systems", 2)).id();
    }

    @Test
    void enrollsWhenCapacityIsAvailable() {
        var result = service.enroll(courseId, new ApiModels.EnrollRequest("alice", true));
        assertThat(result.status()).isEqualTo("ENROLLED");
        assertThat(enrollments.countByCourse_Id(courseId)).isEqualTo(1);
        assertThat(waitlist.countByCourse_Id(courseId)).isZero();
    }

    @Test
    void waitlistsEligibleLearnerWhenCourseIsFull() {
        service.enroll(courseId, new ApiModels.EnrollRequest("alice", true));
        service.enroll(courseId, new ApiModels.EnrollRequest("bob", true));
        var result = service.enroll(courseId, new ApiModels.EnrollRequest("charlie", true));
        assertThat(result.status()).isEqualTo("WAITLISTED");
        assertThat(enrollments.countByCourse_Id(courseId)).isEqualTo(2);
        assertThat(waitlist.countByCourse_Id(courseId)).isEqualTo(1);
    }

    @Test
    void duplicateRequestDoesNotCreateDuplicateEnrollmentOrWaitlistEntry() {
        service.enroll(courseId, new ApiModels.EnrollRequest("alice", true));
        var duplicate = service.enroll(courseId, new ApiModels.EnrollRequest("alice", true));
        assertThat(duplicate.status()).isEqualTo("DUPLICATE");
        service.enroll(courseId, new ApiModels.EnrollRequest("bob", true));
        service.enroll(courseId, new ApiModels.EnrollRequest("charlie", true));
        var duplicateWaitlist = service.enroll(courseId, new ApiModels.EnrollRequest("charlie", true));
        assertThat(duplicateWaitlist.status()).isEqualTo("DUPLICATE");
        assertThat(enrollments.countByCourse_Id(courseId)).isEqualTo(2);
        assertThat(waitlist.countByCourse_Id(courseId)).isEqualTo(1);
    }

    @Test
    void droppingLearnerPromotesEarliestWaitlistedLearner() {
        service.enroll(courseId, new ApiModels.EnrollRequest("alice", true));
        service.enroll(courseId, new ApiModels.EnrollRequest("bob", true));
        service.enroll(courseId, new ApiModels.EnrollRequest("charlie", true));
        service.enroll(courseId, new ApiModels.EnrollRequest("diana", true));
        var result = service.drop(courseId, "alice");
        assertThat(result.status()).isEqualTo("DROPPED");
        assertThat(result.promotedLearnerId()).isEqualTo("charlie");
        assertThat(enrollments.existsByCourse_IdAndLearnerId(courseId, "charlie")).isTrue();
        assertThat(waitlist.existsByCourse_IdAndLearnerId(courseId, "charlie")).isFalse();
        assertThat(waitlist.findAllByCourse_IdOrderByCreatedAtAscIdAsc(courseId))
                .extracting(WaitlistEntry::getLearnerId).containsExactly("diana");
        assertThat(enrollments.countByCourse_Id(courseId)).isEqualTo(2);
    }

    @Test
    void skipsButRetainsAnIneligibleEntryWhenPromoting() {
        service.enroll(courseId, new ApiModels.EnrollRequest("alice", true));
        service.enroll(courseId, new ApiModels.EnrollRequest("bob", true));
        CourseOffering course = courses.findById(courseId).orElseThrow();
        waitlist.saveAndFlush(new WaitlistEntry(course, "charlie", false));
        waitlist.saveAndFlush(new WaitlistEntry(course, "diana", true));

        var result = service.drop(courseId, "alice");

        assertThat(result.promotedLearnerId()).isEqualTo("diana");
        assertThat(waitlist.existsByCourse_IdAndLearnerId(courseId, "charlie")).isTrue();
        assertThat(waitlist.existsByCourse_IdAndLearnerId(courseId, "diana")).isFalse();
        assertThat(enrollments.existsByCourse_IdAndLearnerId(courseId, "diana")).isTrue();
    }

    @Test
    void rejectsLearnerWhoDoesNotMeetPrerequisite() {
        var result = service.enroll(courseId, new ApiModels.EnrollRequest("alice", false));
        assertThat(result.status()).isEqualTo("PREREQUISITE_NOT_MET");
        assertThat(enrollments.countByCourse_Id(courseId)).isZero();
        assertThat(waitlist.countByCourse_Id(courseId)).isZero();
    }

    @Test
    void invalidOrRepeatedDropDoesNotChangeState() {
        service.enroll(courseId, new ApiModels.EnrollRequest("alice", true));
        var first = service.drop(courseId, "missing");
        var second = service.drop(courseId, "alice");
        var third = service.drop(courseId, "alice");
        assertThat(first.status()).isEqualTo("NOT_ENROLLED");
        assertThat(second.status()).isEqualTo("DROPPED");
        assertThat(third.status()).isEqualTo("NOT_ENROLLED");
        assertThat(enrollments.countByCourse_Id(courseId)).isZero();
    }

    @Test
    void doesNotExceedCapacity() {
        service.enroll(courseId, new ApiModels.EnrollRequest("alice", true));
        service.enroll(courseId, new ApiModels.EnrollRequest("bob", true));
        service.enroll(courseId, new ApiModels.EnrollRequest("charlie", true));
        assertThat(enrollments.countByCourse_Id(courseId)).isLessThanOrEqualTo(2);
    }
}
