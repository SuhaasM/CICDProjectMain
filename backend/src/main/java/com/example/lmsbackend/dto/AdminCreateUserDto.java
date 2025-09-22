package com.example.lmsbackend.dto;

import lombok.Data;

@Data
public class AdminCreateUserDto {
    private String email;
    private String password;
    private String username;
    private String role; // The admin will set this
}