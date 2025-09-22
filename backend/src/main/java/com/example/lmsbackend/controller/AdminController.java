package com.example.lmsbackend.controller;

import com.example.lmsbackend.dto.EnrollmentResponseDto;
import com.example.lmsbackend.repository.EnrollmentRepository;
import com.example.lmsbackend.repository.ProfileRepository;
import com.example.lmsbackend.service.AuthService;
import com.example.lmsbackend.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {


    
    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            // Create a custom query to fetch profiles with eager loading of user data
            List<Map<String, Object>> profileData = profileRepository.findAll().stream()
                .map(profile -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", profile.getId());
                    data.put("username", profile.getUsername());
                    data.put("role", profile.getRole());
                    data.put("avatarUrl", profile.getAvatarUrl());
                    
                    // Only include necessary user data
                    if (profile.getUser() != null) {
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("id", profile.getUser().getId());
                        userData.put("email", profile.getUser().getEmail());
                        data.put("user", userData);
                    }
                    
                    return data;
                })
                .collect(Collectors.toList());
                
            return ResponseEntity.ok(profileData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody com.example.lmsbackend.dto.AdminCreateUserDto createUserDto) {
        try {
            User newUser = authService.createUserByAdmin(createUserDto);
            return new ResponseEntity<>("User created successfully with ID: " + newUser.getId(), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error creating user: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/users/{profileId}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long profileId, @RequestBody com.example.lmsbackend.dto.UpdateRoleDto updateRoleDto) {
        try {
        // Find the profile by its ID
            com.example.lmsbackend.model.Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found with id: " + profileId));

        // Set the new role (ensuring it's uppercase) and save
            profile.setRole(updateRoleDto.getRole().toUpperCase());
            profileRepository.save(profile);

            return ResponseEntity.ok().body(Map.of("message", "User role updated successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

@GetMapping("/enrollments")
public ResponseEntity<List<EnrollmentResponseDto>> getAllEnrollments() {
    List<com.example.lmsbackend.model.Enrollment> enrollments = enrollmentRepository.findAll();
    
    List<EnrollmentResponseDto> responseDtos = enrollments.stream().map(enrollment -> 
        new EnrollmentResponseDto(
            enrollment.getId(),
            enrollment.getStudent().getProfile().getUsername(),
            enrollment.getCourse().getTitle(),
            enrollment.getStatus()
        )
    ).collect(java.util.stream.Collectors.toList());

    return ResponseEntity.ok(responseDtos);
}

    @DeleteMapping("/enrollments/{enrollmentId}")
    public ResponseEntity<?> deleteEnrollment(@PathVariable Long enrollmentId) {
        if (!enrollmentRepository.existsById(enrollmentId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Enrollment not found."));
        }
        enrollmentRepository.deleteById(enrollmentId);
        return ResponseEntity.ok().body(Map.of("message", "Enrollment removed successfully."));
    }
}