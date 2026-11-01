package com.vericode.vericode_backend.controller;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.vericode.vericode_backend.Service.AssessmentAttemptService;
import com.vericode.vericode_backend.dto.StartAssessmentRequestDTO;
import com.vericode.vericode_backend.repository.AssessmentRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Assessment Attempts", description = "Start and retrieve server-timed student assessment attempts.")
@SecurityRequirement(name = "SessionCookie")
@RestController
@RequestMapping("/Assessments")
public class AssessmentAttemptController {
    private final AssessmentAttemptService attemptService;
    private final AssessmentRepository assessmentRepository;

    public AssessmentAttemptController(AssessmentAttemptService attemptService,
                                       AssessmentRepository assessmentRepository) {
        this.attemptService = attemptService;
        this.assessmentRepository = assessmentRepository;
    }

    @GetMapping("/{assessmentId}/attempt")
    @Operation(summary = "Get the current student's assessment attempt and remaining time")
    public ResponseEntity<?> getAttempt(@PathVariable Integer assessmentId, HttpSession session) {
        Integer studentId = studentId(session);
        if (studentId == null) {
            return ResponseEntity.status(session.getAttribute("userId") == null
                    ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN).build();
        }
        if (!assessmentRepository.isStudentAssignedToAssessment(assessmentId, studentId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return attemptService.get(assessmentId, studentId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/{assessmentId}/start")
    @Operation(summary = "Start a timed assessment attempt after recording consent")
    public ResponseEntity<?> start(@PathVariable Integer assessmentId,
                                   @Valid @RequestBody StartAssessmentRequestDTO request,
                                   HttpSession session) {
        Integer studentId = studentId(session);
        if (studentId == null) {
            return ResponseEntity.status(session.getAttribute("userId") == null
                    ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN).build();
        }
        if (!assessmentRepository.isStudentAssignedToAssessment(assessmentId, studentId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(attemptService.start(
                assessmentId, studentId, request != null && request.isConsentConfirmed()));
    }

    private Integer studentId(HttpSession session) {
        if (!"STUDENT".equals(session.getAttribute("role"))) return null;
        Object userId = session.getAttribute("userId");
        return userId instanceof Integer id ? id : null;
    }
}
