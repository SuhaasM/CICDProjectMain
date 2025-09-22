package com.example.lmsbackend.dto;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String email;
    private String password;
}