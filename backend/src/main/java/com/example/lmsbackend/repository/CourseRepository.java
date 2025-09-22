package com.example.lmsbackend.repository;

import com.example.lmsbackend.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}