package com.example.lmsbackend.controller;

import com.example.lmsbackend.model.Enrollment;
import com.example.lmsbackend.repository.CourseRepository;
import com.example.lmsbackend.repository.EnrollmentRepository;
import com.example.lmsbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CourseRepository courseRepository;

    @PostMapping("/request")
    public ResponseEntity<?> requestEnrollment(@RequestBody Map<String, Long> payload, Authentication authentication) {
        Long courseId = payload.get("courseId");
        String userEmail = authentication.getName();
        com.example.lmsbackend.model.User student = userRepository.findByEmail(userEmail).orElseThrow();
        com.example.lmsbackend.model.Course course = courseRepository.findById(courseId).orElseThrow();

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setStatus("pending");
        enrollmentRepository.save(enrollment);
        return ResponseEntity.ok().body(Map.of("message", "Enrollment request submitted successfully."));
    }

    @GetMapping("/my-courses")
    public ResponseEntity<List<com.example.lmsbackend.dto.CourseDto>> getMyCourses(Authentication authentication) {
        String userEmail = authentication.getName();
        com.example.lmsbackend.model.User student = userRepository.findByEmail(userEmail).orElseThrow();
        List<Enrollment> enrollments = enrollmentRepository.findAll().stream()
                .filter(e -> e.getStudent().getId().equals(student.getId()) && "approved".equals(e.getStatus()))
                .collect(Collectors.toList());
        List<com.example.lmsbackend.dto.CourseDto> courseDtos = enrollments.stream()
                .map(enrollment -> {
                    com.example.lmsbackend.model.Course course = enrollment.getCourse();
                    return new com.example.lmsbackend.dto.CourseDto(
                        course.getId(),
                        course.getTitle(),
                        course.getDescription(),
                        course.getInstructor().getId(),
                        course.getInstructor().getEmail()
                    );
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(courseDtos);
    }
    
    @GetMapping("/pending")
    public ResponseEntity<List<Enrollment>> getPendingEnrollments(Authentication authentication) {
        String userEmail = authentication.getName();
        com.example.lmsbackend.model.User instructor = userRepository.findByEmail(userEmail).orElseThrow();
        return ResponseEntity.ok(enrollmentRepository.findPendingEnrollmentsForInstructor(instructor.getId()));
    }
    
    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approveEnrollment(@PathVariable Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id).orElseThrow();
        enrollment.setStatus("approved");
        enrollmentRepository.save(enrollment);
        return ResponseEntity.ok().body(Map.of("message", "Enrollment approved."));
    }
}