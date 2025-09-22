package com.example.lmsbackend.repository;

import com.example.lmsbackend.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}