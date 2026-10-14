package com.vericode.vericode_backend.controller;

import com.vericode.vericode_backend.Service.CodeExecutionService;
import com.vericode.vericode_backend.dto.CodeExecutionRequestDTO;
import com.vericode.vericode_backend.dto.CodeExecutionResponseDTO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/CodeExecution")
public class CodeExecutionController {
    private final CodeExecutionService codeExecutionService;

    public CodeExecutionController(CodeExecutionService codeExecutionService) {
        this.codeExecutionService = codeExecutionService;
    }

    @PostMapping("/run")
    public ResponseEntity<?> run(@Valid @RequestBody CodeExecutionRequestDTO request, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!"STUDENT".equals(role)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        CodeExecutionResponseDTO result = codeExecutionService.execute(request);
        return ResponseEntity.ok(result);
    }
}
