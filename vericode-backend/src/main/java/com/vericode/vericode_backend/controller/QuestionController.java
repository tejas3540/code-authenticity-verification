package com.vericode.vericode_backend.controller;

import com.vericode.vericode_backend.Service.QuestionService;
import com.vericode.vericode_backend.dto.QuestionRequestDTO;
import com.vericode.vericode_backend.dto.QuestionResponseDTO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(questionService.getAllQuestions());
    }

    @GetMapping("/page")
    public ResponseEntity<?> getQuestionsPage(
            HttpSession session,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (pageable.getPageSize() > 100) {
            return ResponseEntity.badRequest().body("Page size cannot exceed 100.");
        }
        Page<QuestionResponseDTO> page = questionService.getQuestionsPage(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getQuestion(@PathVariable Integer id, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(questionService.getQuestionById(id));
    }

    @PostMapping
    public ResponseEntity<?> save(
            @Valid @RequestBody QuestionRequestDTO request,
            HttpSession session) {
        if (!"RECRUITER".equals(session.getAttribute("role"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        QuestionResponseDTO response = questionService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuestion(
            @PathVariable Integer id,
            @Valid @RequestBody QuestionRequestDTO request,
            HttpSession session) {
        if (!"RECRUITER".equals(session.getAttribute("role"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        QuestionResponseDTO response = questionService.updateQuestion(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Integer id, HttpSession session) {
        if (!"RECRUITER".equals(session.getAttribute("role"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        QuestionResponseDTO response = questionService.deleteQuestion(id);
        return ResponseEntity.ok(response);
    }
}
