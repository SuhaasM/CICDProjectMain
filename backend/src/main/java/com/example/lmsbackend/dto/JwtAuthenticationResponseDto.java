package com.example.lmsbackend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtAuthenticationResponseDto {
    private String token;
}