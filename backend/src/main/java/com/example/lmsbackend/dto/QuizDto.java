package com.example.lmsbackend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuizDto {
    private String title;
    private Long lessonId;
    private List<QuestionDto> questions;
}