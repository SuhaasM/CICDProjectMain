package com.example.lmsbackend.controller;

import com.example.lmsbackend.dto.ProfileResponseDto;
import com.example.lmsbackend.dto.ProfileUpdateDto;
import com.example.lmsbackend.model.Profile;
import com.example.lmsbackend.model.User;
import com.example.lmsbackend.repository.ProfileRepository;
import com.example.lmsbackend.repository.UserRepository;
import com.example.lmsbackend.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private FileStorageService fileStorageService;

    // Helper method to convert Profile entity to DTO
    private ProfileResponseDto convertToDto(Profile profile) {
        ProfileResponseDto dto = new ProfileResponseDto();
        dto.setId(profile.getId());
        dto.setUsername(profile.getUsername());
        dto.setRole(profile.getRole());
        dto.setAvatarUrl(profile.getAvatarUrl());
        dto.setEmail(profile.getUser().getEmail()); // Get email from the nested User
        return dto;
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponseDto> getMyProfile(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Profile profile = profileRepository.findByUserWithUserEagerly(user).orElseThrow(() -> new RuntimeException("Profile not found"));
        return ResponseEntity.ok(convertToDto(profile));
    }

    @PatchMapping("/me")
    public ResponseEntity<ProfileResponseDto> updateMyProfile(@RequestBody ProfileUpdateDto profileUpdateDto, Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Profile profile = profileRepository.findByUserWithUserEagerly(user).orElseThrow(() -> new RuntimeException("Profile not found"));

        profile.setUsername(profileUpdateDto.getUsername());
        Profile updatedProfile = profileRepository.save(profile);
        return ResponseEntity.ok(convertToDto(updatedProfile));
    }

    @PostMapping("/me/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file, Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));
        Profile profile = profileRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Profile not found"));
        String fileName = fileStorageService.storeFile(file);
        String avatarUrl = "/uploads/" + fileName;
        profile.setAvatarUrl(avatarUrl);
        profileRepository.save(profile);
        return ResponseEntity.ok(Map.of("message", "Avatar uploaded successfully", "filePath", avatarUrl));
    }
}