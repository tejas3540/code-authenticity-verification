package com.vericode.vericode_backend.controller;

import com.vericode.vericode_backend.Service.QuestionImportService;
import com.vericode.vericode_backend.dto.QuestionImportRequestDTO;
import com.vericode.vericode_backend.dto.QuestionImportResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/QuestionImport")
public class QuestionImportController {
    private final QuestionImportService questionImportService;

    public QuestionImportController(QuestionImportService questionImportService) {
        this.questionImportService = questionImportService;
    }

    @PostMapping("/preview")
    public ResponseEntity<QuestionImportResponseDTO> preview(@Valid @RequestBody QuestionImportRequestDTO request) {
        return ResponseEntity.ok(questionImportService.preview(request.getUrl()));
    }
}
