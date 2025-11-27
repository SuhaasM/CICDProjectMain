package com.example.lmsbackend.config;

import com.example.lmsbackend.model.Profile;
import com.example.lmsbackend.model.User;
import com.example.lmsbackend.repository.ProfileRepository;
import com.example.lmsbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.CommandLineRunner;

@Configuration
public class DataSeeder {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedDefaultUsers() {
        return args -> {
            seedUser("admin@gmail.com", "adminpassword", "admin", "ADMIN");
            seedUser("faculty@gmail.com", "facultypassword", "faculty", "FACULTY");
        };
    }

    private void seedUser(String email, String rawPassword, String username, String role) {
        if (userRepository.findByEmail(email).isEmpty()) {
            User user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user = userRepository.save(user);

            Profile profile = new Profile();
            profile.setUser(user);
            profile.setUsername(username);
            profile.setRole(role.toUpperCase());
            profileRepository.save(profile);
        }
    }
}

