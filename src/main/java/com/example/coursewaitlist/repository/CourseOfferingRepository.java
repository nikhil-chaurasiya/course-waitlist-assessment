package com.example.coursewaitlist.repository;

import com.example.coursewaitlist.entity.CourseOffering;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface CourseOfferingRepository extends JpaRepository<CourseOffering, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CourseOffering c where c.id = :id")
    Optional<CourseOffering> findByIdForUpdate(@Param("id") Long id);
}
