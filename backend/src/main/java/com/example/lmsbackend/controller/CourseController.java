package com.example.lmsbackend.controller;

import com.example.lmsbackend.dto.CourseDto;
import com.example.lmsbackend.model.Course;
import com.example.lmsbackend.repository.CourseRepository;
import com.example.lmsbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<CourseDto> createCourse(@RequestBody Course course, Authentication authentication) {
        String userEmail = authentication.getName();
        com.example.lmsbackend.model.User instructor = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new UsernameNotFoundException("Instructor not found"));
        
        course.setInstructor(instructor);
        Course savedCourse = courseRepository.save(course);
        
        CourseDto courseDto = new CourseDto(
            savedCourse.getId(),
            savedCourse.getTitle(),
            savedCourse.getDescription(),
            savedCourse.getInstructor().getId(),
            savedCourse.getInstructor().getEmail()
        );
        
        return new ResponseEntity<>(courseDto, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CourseDto>> getAllCourses() {
        List<Course> courses = courseRepository.findAll();
        List<CourseDto> courseDtos = courses.stream()
            .map(course -> new CourseDto(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getInstructor().getId(),
                course.getInstructor().getEmail()
            ))
            .collect(Collectors.toList());
        return ResponseEntity.ok(courseDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDto> getCourseById(@PathVariable Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id " + id));
        
        CourseDto courseDto = new CourseDto(
            course.getId(),
            course.getTitle(),
            course.getDescription(),
            course.getInstructor().getId(),
            course.getInstructor().getEmail()
        );
        
        return ResponseEntity.ok(courseDto);
    }
}