package com.example.lmsbackend.service;

import com.example.lmsbackend.model.Profile;
import com.example.lmsbackend.repository.ProfileRepository;
import com.example.lmsbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Find the user by their email
        com.example.lmsbackend.model.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Find the corresponding profile to get the role
        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new UsernameNotFoundException("Profile not found for user: " + email));

        // Create a list of authorities (roles) for Spring Security
        List<GrantedAuthority> authorities = new ArrayList<>();
        // Add the role directly as an authority without ROLE_ prefix to support hasAuthority
        authorities.add(new SimpleGrantedAuthority(profile.getRole()));
        
        // Return a new UserDetails object containing the email, password, and the user's role
        return new User(user.getEmail(), user.getPassword(), authorities);
    }
}