package com.vericode.vericode_backend.controller;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import com.vericode.vericode_backend.Service.TestCaseService;
import com.vericode.vericode_backend.dto.TestCaseRequestDTO;
import com.vericode.vericode_backend.dto.TestCaseResponseDTO;
import com.vericode.vericode_backend.repository.AssessmentRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Test Cases", description = "Create, read and delete question test cases with role-based visibility.")
@SecurityRequirement(name = "SessionCookie")
@RestController
@RequestMapping("/TestCases")
public class TestCaseController {
    private final TestCaseService service;
    private final AssessmentRepository assessmentRepository;

    public TestCaseController(TestCaseService service, AssessmentRepository assessmentRepository) {
        this.service = service;
        this.assessmentRepository = assessmentRepository;
    }

    @PostMapping
    @Operation(summary = "Create a test case for a coding question")
    public ResponseEntity<?> create(@Valid @RequestBody TestCaseRequestDTO request, HttpSession session) {
        HttpStatus accessError = recruiterAccessError(session);
        if (accessError != null) {
            return ResponseEntity.status(accessError).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/question/{questionId}")
    @Operation(summary = "List test cases visible to the current session role")
    public ResponseEntity<?> find(@PathVariable Integer questionId, HttpSession session) {
        String role = (String) session.getAttribute("role");
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if ("STUDENT".equals(role)) {
            boolean assigned = assessmentRepository.isQuestionAssignedToStudent(userId, questionId);
            if (!assigned) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } else if (!"RECRUITER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(service.findForQuestion(questionId, "STUDENT".equals(role)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a test case")
    public ResponseEntity<?> delete(@PathVariable Integer id, HttpSession session) {
        HttpStatus accessError = recruiterAccessError(session);
        if (accessError != null) {
            return ResponseEntity.status(accessError).build();
        }
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private HttpStatus recruiterAccessError(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (!"RECRUITER".equals(session.getAttribute("role"))) {
            return HttpStatus.FORBIDDEN;
        }
        return null;
    }
}
