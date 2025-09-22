package com.example.lmsbackend.dto;

import com.example.lmsbackend.model.Profile;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDto {
    private String token;
    private Profile profile;
}