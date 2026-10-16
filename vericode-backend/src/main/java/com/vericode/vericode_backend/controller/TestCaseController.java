package com.vericode.vericode_backend.controller;

import com.vericode.vericode_backend.Service.TestCaseService;
import com.vericode.vericode_backend.dto.TestCaseRequestDTO;
import com.vericode.vericode_backend.dto.TestCaseResponseDTO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/TestCases")
public class TestCaseController {
    private final TestCaseService service;

    public TestCaseController(TestCaseService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody TestCaseRequestDTO request, HttpSession session) {
        if (!"RECRUITER".equals(session.getAttribute("role"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<TestCaseResponseDTO>> find(@PathVariable Integer questionId, HttpSession session) {
        boolean student = "STUDENT".equals(session.getAttribute("role"));
        return ResponseEntity.ok(service.findForQuestion(questionId, student));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id, HttpSession session) {
        if (!"RECRUITER".equals(session.getAttribute("role"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
