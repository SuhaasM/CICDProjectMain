package com.example.lmsbackend.repository;

import com.example.lmsbackend.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    
    // Custom query to find pending enrollments for courses taught by a specific instructor
    @Query("SELECT e FROM Enrollment e WHERE e.status = 'pending' AND e.course.instructor.id = :instructorId")
    List<Enrollment> findPendingEnrollmentsForInstructor(Long instructorId);
}