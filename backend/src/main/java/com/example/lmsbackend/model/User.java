package com.example.lmsbackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore; // <-- 1. IMPORT THIS
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore // <-- 2. ADD THIS ANNOTATION
    private Profile profile;
}