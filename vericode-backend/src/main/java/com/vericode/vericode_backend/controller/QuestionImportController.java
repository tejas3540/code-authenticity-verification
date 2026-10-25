package com.vericode.vericode_backend.controller;

import com.vericode.vericode_backend.Service.QuestionImportService;
import com.vericode.vericode_backend.dto.QuestionImportRequestDTO;
import com.vericode.vericode_backend.dto.QuestionImportResponseDTO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/QuestionImport")
public class QuestionImportController {
    private final QuestionImportService questionImportService;

    public QuestionImportController(QuestionImportService questionImportService) {
        this.questionImportService = questionImportService;
    }

    @PostMapping("/preview")
    public ResponseEntity<?> preview(@Valid @RequestBody QuestionImportRequestDTO request, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!"RECRUITER".equals(session.getAttribute("role"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            QuestionImportResponseDTO result = questionImportService.preview(request.getUrl());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("message", e.getMessage()));
        }
    }
}
