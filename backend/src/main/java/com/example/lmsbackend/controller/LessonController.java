package com.example.lmsbackend.controller;

import com.example.lmsbackend.model.Lesson;
import com.example.lmsbackend.repository.CourseRepository;
import com.example.lmsbackend.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses/{courseId}/lessons")
public class LessonController {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private CourseRepository courseRepository;

    @PostMapping
    public ResponseEntity<Lesson> addLesson(@PathVariable Long courseId, @RequestBody Lesson lesson) {
        return courseRepository.findById(courseId).map(course -> {
            lesson.setCourse(course);
            Lesson savedLesson = lessonRepository.save(lesson);
            return new ResponseEntity<>(savedLesson, HttpStatus.CREATED);
        }).orElseThrow(() -> new RuntimeException("Course not found with id " + courseId));
    }

    @GetMapping
    public ResponseEntity<List<Lesson>> getLessonsForCourse(@PathVariable Long courseId) {
        List<Lesson> lessons = lessonRepository.findByCourseId(courseId);
        return ResponseEntity.ok(lessons);
    }
}