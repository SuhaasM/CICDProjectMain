package com.example.lmsbackend.controller;

import com.example.lmsbackend.dto.LoginRequestDto;
import com.example.lmsbackend.dto.LoginResponseDto;
import com.example.lmsbackend.dto.UserRegistrationDto;
import com.example.lmsbackend.model.User;
import com.example.lmsbackend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationDto registrationDto) {
        try {
            User newUser = authService.registerUser(registrationDto);
            // Return a success response, but avoid sending back the password
            return ResponseEntity.ok().body(Map.of(
                "message", "User registered successfully with ID: " + newUser.getId()
            ));
        } catch (Exception e) {
            // Handle potential errors, like a duplicate email
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequestDto loginRequestDto) {
        try {
            LoginResponseDto response = authService.loginUser(loginRequestDto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                LoginResponseDto response = authService.refreshToken(authentication);
                return ResponseEntity.ok().body(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "Not authenticated"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
}