package com.example.coursewaitlist.repository;

import com.example.coursewaitlist.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByCourse_IdAndLearnerId(Long courseId, String learnerId);
    long countByCourse_Id(Long courseId);
    Optional<Enrollment> findByCourse_IdAndLearnerId(Long courseId, String learnerId);
}
