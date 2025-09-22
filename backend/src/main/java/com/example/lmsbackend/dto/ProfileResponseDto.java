package com.example.lmsbackend.dto;

import lombok.Data;

@Data
public class ProfileResponseDto {
    private Long id;
    private String username;
    private String role;
    private String avatarUrl;
    private String email; // Add email from the User object
}