package com.example.lmsbackend.service;

import com.example.lmsbackend.dto.AdminCreateUserDto;
import com.example.lmsbackend.dto.LoginRequestDto;
import com.example.lmsbackend.dto.LoginResponseDto;
import com.example.lmsbackend.dto.UserRegistrationDto;
import com.example.lmsbackend.model.Profile;
import com.example.lmsbackend.model.User;
import com.example.lmsbackend.repository.ProfileRepository;
import com.example.lmsbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Transactional // This ensures both operations succeed or both fail
    public User registerUser(UserRegistrationDto registrationDto) {
        // Step 1: Create and save the User entity
        User newUser = new User();
        newUser.setEmail(registrationDto.getEmail());
        // Hash the password before saving
        newUser.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        User savedUser = userRepository.save(newUser);

        // Step 2: Create and save the Profile entity
        Profile newProfile = new Profile();
        newProfile.setUser(savedUser);
        newProfile.setUsername(registrationDto.getUsername());
        newProfile.setRole("STUDENT"); // All new signups are students by default
        profileRepository.save(newProfile);

        return savedUser;
    }

    public LoginResponseDto loginUser(LoginRequestDto loginRequestDto) {
    User user = userRepository.findByEmail(loginRequestDto.getEmail())
            .orElseThrow(() -> new RuntimeException("Invalid email or password."));

    if (passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
        // Find the user's profile to get their role
        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("User profile not found."));

        // Generate the JWT
        var userDetails = org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail()).password(user.getPassword()).authorities(profile.getRole()).build();
        String token = jwtService.generateToken(userDetails);

        // Build and return the response object
        return LoginResponseDto.builder()
            .token(token)
            .profile(profile)
            .build();
    } else {
        throw new RuntimeException("Invalid email or password.");
    }
}

public LoginResponseDto refreshToken(Authentication authentication) {
    // Get the user details from the authentication object
    String email = authentication.getName();
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found."));
    
    // Find the user's profile to get their role
    Profile profile = profileRepository.findByUser(user)
            .orElseThrow(() -> new RuntimeException("User profile not found."));
    
    // Generate a new JWT token
    var userDetails = org.springframework.security.core.userdetails.User
        .withUsername(user.getEmail()).password(user.getPassword()).authorities(profile.getRole()).build();
    String newToken = jwtService.generateToken(userDetails);
    
    // Build and return the response object
    return LoginResponseDto.builder()
        .token(newToken)
        .profile(profile)
        .build();
}
@Transactional
public User createUserByAdmin(AdminCreateUserDto createUserDto) {
    // Step 1: Create and save the User entity
    User newUser = new User();
    newUser.setEmail(createUserDto.getEmail());
    newUser.setPassword(passwordEncoder.encode(createUserDto.getPassword()));
    User savedUser = userRepository.save(newUser);

    // Step 2: Create and save the Profile entity with the specified role
    Profile newProfile = new Profile();
    newProfile.setUser(savedUser);
    newProfile.setUsername(createUserDto.getUsername());
    // Use the role from the DTO, ensuring it's uppercase
    newProfile.setRole(createUserDto.getRole().toUpperCase()); 
    profileRepository.save(newProfile);

    return savedUser;
}
}