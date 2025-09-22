package com.example.lmsbackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore; // <-- 1. IMPORT THIS
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "questions")
@Data
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String questionText;

    @Column(columnDefinition = "JSON", nullable = false)
    private String options;

    @Column(nullable = false)
    private int correctAnswerIndex;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    @JsonIgnore // <-- 2. ADD THIS ANNOTATION
    private Quiz quiz;
}