package com.vericode.vericode_backend.controller;

import com.vericode.vericode_backend.Service.AssessmentService;
import com.vericode.vericode_backend.dto.AssessmentRequestDTO;
import com.vericode.vericode_backend.dto.AssessmentResponseDTO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Assessments")
public class AssessmentController {
    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @PostMapping
    public ResponseEntity<AssessmentResponseDTO> create(
            @Valid @RequestBody AssessmentRequestDTO request, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!"RECRUITER".equals(role)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(assessmentService.create(userId, request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<AssessmentResponseDTO>> getMyAssessments(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(assessmentService.getForUser(userId, role));
    }
}