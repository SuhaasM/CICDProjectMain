package com.example.lmsbackend.config;

import com.example.lmsbackend.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(withDefaults())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // --- CORRECT ORDER: From most specific to least specific ---

                // PUBLIC routes that anyone can access
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/courses").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/courses/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/error").permitAll()

                // ADMIN ONLY - Highest priority routes
                .requestMatchers("/api/admin/**").permitAll() // Temporarily allow admin access for testing

                // FACULTY & ADMIN
                .requestMatchers(HttpMethod.POST, "/api/courses").hasAnyAuthority("FACULTY", "ADMIN", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/courses/**").hasAnyAuthority("FACULTY", "ADMIN", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/courses/**").hasAnyAuthority("FACULTY", "ADMIN", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/courses/*/lessons").hasAnyAuthority("FACULTY", "ADMIN", "ROLE_ADMIN")
                .requestMatchers("/api/enrollments/pending").hasAnyAuthority("FACULTY", "ADMIN", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/enrollments/**").hasAnyAuthority("FACULTY", "ADMIN", "ROLE_ADMIN")
                .requestMatchers("/api/ai/**").hasAnyAuthority("FACULTY", "ADMIN", "ROLE_ADMIN")

                // STUDENT ONLY
                .requestMatchers(HttpMethod.POST, "/api/enrollments/request").hasAuthority("STUDENT")
                .requestMatchers("/api/enrollments/my-courses").hasAuthority("STUDENT")
                
                // Profile endpoints for all authenticated users
                .requestMatchers("/api/profile/me").authenticated()
                .requestMatchers(HttpMethod.PATCH, "/api/profile/me").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/profile/me/avatar").authenticated()

                // ANY OTHER request must be from a logged-in user
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:5174"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}