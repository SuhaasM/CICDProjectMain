package com.example.lmsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EnrollmentResponseDto {
    private Long enrollmentId;
    private String studentUsername;
    private String courseTitle;
    private String status;
}