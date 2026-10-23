package com.example.coursewaitlist.repository;

import com.example.coursewaitlist.entity.WaitlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WaitlistRepository extends JpaRepository<WaitlistEntry, Long> {
    boolean existsByCourse_IdAndLearnerId(Long courseId, String learnerId);
    Optional<WaitlistEntry> findByCourse_IdAndLearnerId(Long courseId, String learnerId);
    List<WaitlistEntry> findAllByCourse_IdOrderByCreatedAtAscIdAsc(Long courseId);
    long countByCourse_Id(Long courseId);
}
