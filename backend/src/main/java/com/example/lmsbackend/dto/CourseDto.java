package com.example.lmsbackend.dto;

import lombok.Data;

@Data
public class CourseDto {
    private Long id;
    private String title;
    private String description;
    private Long instructorId;
    private String instructorEmail;
    
    // Default constructor for serialization/deserialization
    public CourseDto() {
    }
    
    // Constructor for easy conversion from Course entity
    public CourseDto(Long id, String title, String description, Long instructorId, String instructorEmail) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.instructorId = instructorId;
        this.instructorEmail = instructorEmail;
    }
}