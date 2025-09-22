package com.example.lmsbackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Service
public class AIService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateQuizFromText(String lessonText) {
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=" + apiKey;

        String prompt = "Based on the following text, generate a 5-question multiple-choice quiz. " +
                      "Return the response ONLY as a valid JSON object with the structure: " +
                      "{\"title\":\"Quiz Title\", \"questions\":[{\"questionText\":\"...\", \"options\":[\"...\", \"...\"], \"correctAnswerIndex\":0}]}. " +
                      "Do not include any extra text or markdown formatting like ```json. " +
                      "Text: " + lessonText;

        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", Collections.singletonList(textPart));
        Map<String, Object> requestBody = Map.of("contents", Collections.singletonList(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            
            JsonNode root = objectMapper.readTree(response.getBody());
            String rawText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            
            // --- THIS IS THE FIX ---
            // Clean the raw text to remove Markdown backticks and the "json" language specifier
            String cleanedJson = rawText.replace("```json", "").replace("```", "").trim();
            
            return cleanedJson;

        } catch (Exception e) {
            System.err.println("Error calling Gemini API: " + e.getMessage());
            return "{\"title\":\"Error Generating Quiz\", \"questions\":[{\"questionText\":\"Could not connect to AI service.\", \"options\":[\"OK\"], \"correctAnswerIndex\":0}]}";
        }
    }
}