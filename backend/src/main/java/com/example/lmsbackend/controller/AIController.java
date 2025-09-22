package com.example.lmsbackend.controller;

import com.example.lmsbackend.dto.QuizDto;
import com.example.lmsbackend.dto.QuestionDto;
import com.example.lmsbackend.model.Lesson;
import com.example.lmsbackend.model.Question;
import com.example.lmsbackend.model.Quiz;
import com.example.lmsbackend.repository.LessonRepository;
import com.example.lmsbackend.repository.QuizRepository;
import com.example.lmsbackend.service.AIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AIController {

    @Autowired
    private AIService aiService;
    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private LessonRepository lessonRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @GetMapping("/quiz/{lessonId}")
    public ResponseEntity<?> getQuizForLesson(@PathVariable Long lessonId, Authentication authentication) {
        try {
            Optional<Quiz> quizOpt = quizRepository.findByLessonId(lessonId);
            if (quizOpt.isEmpty()) {
                return ResponseEntity.ok().build(); // No quiz found
            }
            
            Quiz quiz = quizOpt.get();
            
            // Check if user is faculty/admin or if quiz is visible to students
            boolean isFaculty = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("FACULTY") || a.getAuthority().equals("ADMIN"));
                
            if (!isFaculty && !quiz.isVisible()) {
                return ResponseEntity.ok().build(); // Quiz exists but not visible to students
            }
            
            QuizDto quizDto = new QuizDto();
            quizDto.setTitle(quiz.getTitle());
            quizDto.setLessonId(lessonId);
            
            List<QuestionDto> questionDtos = quiz.getQuestions().stream().map(q -> {
                QuestionDto qDto = new QuestionDto();
                qDto.setQuestionText(q.getQuestionText());
                qDto.setCorrectAnswerIndex(q.getCorrectAnswerIndex());
                try {
                    qDto.setOptions(objectMapper.readValue(q.getOptions(), List.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return qDto;
            }).collect(Collectors.toList());
            
            quizDto.setQuestions(questionDtos);
            
            return ResponseEntity.ok(quizDto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error fetching quiz: " + e.getMessage());
        }
    }
    
    @PostMapping("/save-quiz")
    @Transactional
    public ResponseEntity<?> saveQuiz(@RequestBody Map<String, Object> payload) {
        try {
            QuizDto quizDto = objectMapper.convertValue(payload.get("quiz"), QuizDto.class);
            Long lessonId = Long.valueOf(payload.get("lessonId").toString());
            boolean visible = Boolean.valueOf(payload.get("visible").toString());
            
            Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
                
            // Check if a quiz already exists for this lesson
            Optional<Quiz> existingQuiz = quizRepository.findByLessonId(lessonId);
            Quiz quiz;
            
            if (existingQuiz.isPresent()) {
                quiz = existingQuiz.get();
                quiz.getQuestions().clear(); // Remove existing questions
            } else {
                quiz = new Quiz();
                quiz.setLesson(lesson);
            }
            
            quiz.setTitle(quizDto.getTitle());
            quiz.setVisible(visible);
            
            List<Question> questions = quizDto.getQuestions().stream().map(qDto -> {
                Question question = new Question();
                question.setQuestionText(qDto.getQuestionText());
                question.setCorrectAnswerIndex(qDto.getCorrectAnswerIndex());
                try {
                    question.setOptions(objectMapper.writeValueAsString(qDto.getOptions()));
                } catch (Exception e) { 
                    throw new RuntimeException(e); 
                }
                question.setQuiz(quiz);
                return question;
            }).collect(Collectors.toList());
            
            quiz.setQuestions(questions);
            quizRepository.save(quiz);
            
            return ResponseEntity.ok(quizDto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error saving quiz: " + e.getMessage());
        }
    }

    @PostMapping("/generate-quiz")
@Transactional
public ResponseEntity<?> generateAndSaveQuiz(@RequestBody Map<String, Object> payload) {
    String lessonText = (String) payload.get("text");
    Long lessonId = Long.valueOf(payload.get("lessonId").toString());

    try {
        // Check if a quiz already exists for this lesson
        Optional<Quiz> existingQuiz = quizRepository.findByLessonId(lessonId);

        // If it exists, delete it and flush the change to the database immediately
        if (existingQuiz.isPresent()) {
            quizRepository.delete(existingQuiz.get());
            quizRepository.flush(); // <-- THIS IS THE FIX
        }

        // Now, proceed with creating the new quiz
        String quizJson = aiService.generateQuizFromText(lessonText);
        QuizDto quizDto = objectMapper.readValue(quizJson, QuizDto.class);
        quizDto.setLessonId(lessonId);
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(() -> new RuntimeException("Lesson not found"));

        Quiz newQuiz = new Quiz();
        newQuiz.setTitle(quizDto.getTitle());
        newQuiz.setLesson(lesson);

        List<Question> questions = quizDto.getQuestions().stream().map(qDto -> {
            Question question = new Question();
            question.setQuestionText(qDto.getQuestionText());
            question.setCorrectAnswerIndex(qDto.getCorrectAnswerIndex());
             try {
                question.setOptions(objectMapper.writeValueAsString(qDto.getOptions()));
            } catch (Exception e) { throw new RuntimeException(e); }
            question.setQuiz(newQuiz);
            return question;
        }).collect(Collectors.toList());

        newQuiz.setQuestions(questions);
        quizRepository.save(newQuiz);

        return ResponseEntity.ok(quizDto);

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.internalServerError().body("Error processing AI response: " + e.getMessage());
    }
}
}