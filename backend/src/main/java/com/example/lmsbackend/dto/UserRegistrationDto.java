package com.example.lmsbackend.dto;

import lombok.Data;

@Data
public class UserRegistrationDto {
    private String email;
    private String password;
    private String username;
    // The role will be 'student' by default, so we don't need it here for now
}