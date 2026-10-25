package com.vericode.vericode_backend.controller;

import com.vericode.vericode_backend.Service.QuestionService;
import com.vericode.vericode_backend.dto.QuestionRequestDTO;
import com.vericode.vericode_backend.dto.QuestionResponseDTO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public ResponseEntity<?> getAllQuestions(HttpSession session) {
        ResponseEntity<?> accessError = recruiterAccessError(session);
        if (accessError != null) {
            return accessError;
        }
        return ResponseEntity.ok(questionService.getAllQuestions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getQuestion(@PathVariable Integer id, HttpSession session) {
        ResponseEntity<?> accessError = recruiterAccessError(session);
        if (accessError != null) {
            return accessError;
        }
        return ResponseEntity.ok(questionService.getQuestionById(id));
    }

    @PostMapping
    public ResponseEntity<?> save(
            @Valid @RequestBody QuestionRequestDTO request,
            HttpSession session) {
        ResponseEntity<?> accessError = recruiterAccessError(session);
        if (accessError != null) {
            return accessError;
        }
        QuestionResponseDTO response = questionService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuestion(
            @PathVariable Integer id,
            @Valid @RequestBody QuestionRequestDTO request,
            HttpSession session) {
        ResponseEntity<?> accessError = recruiterAccessError(session);
        if (accessError != null) {
            return accessError;
        }
        QuestionResponseDTO response = questionService.updateQuestion(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Integer id, HttpSession session) {
        ResponseEntity<?> accessError = recruiterAccessError(session);
        if (accessError != null) {
            return accessError;
        }
        QuestionResponseDTO response = questionService.deleteQuestion(id);
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<?> recruiterAccessError(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!"RECRUITER".equals(session.getAttribute("role"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return null;
    }
}
