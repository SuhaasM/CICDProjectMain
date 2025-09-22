package com.example.lmsbackend.model;
    
import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "profiles")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "profile"})
    private User user;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String role;

    @Column(name = "avatar_url") // This maps the field to the database column
    private String avatarUrl; // <-- ADD THIS FIELD
}