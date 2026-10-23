package com.example.coursewaitlist.service;


import com.example.coursewaitlist.dto.ApiModels;
import com.example.coursewaitlist.repository.CourseOfferingRepository;
import com.example.coursewaitlist.repository.EnrollmentRepository;
import com.example.coursewaitlist.repository.WaitlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RegistrationConcurrencyTest {

    private static final int CONCURRENT_LEARNERS = 6;

    @Autowired
    RegistrationService service;

    @Autowired
    CourseOfferingRepository courses;

    @Autowired
    EnrollmentRepository enrollments;

    @Autowired
    WaitlistRepository waitlist;

    private Long courseId;

    @BeforeEach
    void setUp() {
        waitlist.deleteAll();
        enrollments.deleteAll();
        courses.deleteAll();

        courseId = service.createCourse(
                new ApiModels.CreateCourseRequest("Concurrent Enrollment", 1)
        ).id();
    }

    @Test
    void concurrentEnrollmentsNeverExceedCapacity() throws Exception {
        var ready = new CountDownLatch(CONCURRENT_LEARNERS);
        var start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(CONCURRENT_LEARNERS);

        try {
            List<java.util.concurrent.Future<ApiModels.OperationResponse>> futures =
                    new ArrayList<>();

            for (int i = 0; i < CONCURRENT_LEARNERS; i++) {
                final String learnerId = "learner-" + i;

                futures.add(executor.submit(() -> {
                    ready.countDown();

                    if (!start.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException(
                                "Timed out waiting to start concurrent enrollment."
                        );
                    }

                    return service.enroll(
                            courseId,
                            new ApiModels.EnrollRequest(learnerId, true)
                    );
                }));
            }

            // Wait until every worker is ready, then release them together.
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            List<String> statuses = new ArrayList<>();

            for (var future : futures) {
                statuses.add(future.get(10, TimeUnit.SECONDS).status());
            }

            // Capacity is one: exactly one enrollment and five waitlist entries.
            assertThat(statuses).containsExactlyInAnyOrder(
                    "ENROLLED",
                    "WAITLISTED",
                    "WAITLISTED",
                    "WAITLISTED",
                    "WAITLISTED",
                    "WAITLISTED"
            );

            assertThat(enrollments.countByCourse_Id(courseId)).isEqualTo(1);
            assertThat(waitlist.countByCourse_Id(courseId))
                    .isEqualTo(CONCURRENT_LEARNERS - 1);

            assertThat(enrollments.countByCourse_Id(courseId))
                    .isLessThanOrEqualTo(1);

        } finally {
            // Ensure workers are released even if an assertion fails.
            start.countDown();
            executor.shutdownNow();

            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }
    }
}

