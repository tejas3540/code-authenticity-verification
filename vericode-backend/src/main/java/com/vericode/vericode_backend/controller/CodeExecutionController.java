package com.vericode.vericode_backend.controller;

import com.vericode.vericode_backend.Service.CodeExecutionService;
import com.vericode.vericode_backend.dto.CodeEvaluationRequestDTO;
import com.vericode.vericode_backend.dto.CodeExecutionRequestDTO;
import com.vericode.vericode_backend.dto.CodeExecutionResponseDTO;
import com.vericode.vericode_backend.model.TestCase;
import com.vericode.vericode_backend.repository.TestCaseRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/CodeExecution")
public class CodeExecutionController {
    private final CodeExecutionService codeExecutionService;
    private final TestCaseRepository testCaseRepository;

    public CodeExecutionController(CodeExecutionService codeExecutionService, TestCaseRepository testCaseRepository) {
        this.codeExecutionService = codeExecutionService;
        this.testCaseRepository = testCaseRepository;
    }

    @PostMapping("/run")
    public ResponseEntity<?> run(@Valid @RequestBody CodeExecutionRequestDTO request, HttpSession session) {
        if (session.getAttribute("userId") == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!"STUDENT".equals(session.getAttribute("role"))) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        CodeExecutionResponseDTO result = codeExecutionService.execute(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/evaluate")
    public ResponseEntity<?> evaluate(@Valid @RequestBody CodeEvaluationRequestDTO request, HttpSession session) {
        if (session.getAttribute("userId") == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!"STUDENT".equals(session.getAttribute("role"))) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        List<TestCase> cases = testCaseRepository.findByQuestion_IdOrderByIdAsc(request.getQuestionId());
        if (cases.isEmpty()) return ResponseEntity.badRequest().body(Map.of("message", "This question has no test cases."));

        List<Map<String, Object>> results = new ArrayList<>();
        int passed = 0;
        int earnedPoints = 0;
        int totalPoints = cases.stream().mapToInt(testCase -> testCase.getPoints() == null ? 0 : testCase.getPoints()).sum();

        for (TestCase testCase : cases) {
            CodeExecutionRequestDTO execution = new CodeExecutionRequestDTO();
            execution.setSourceCode(request.getSourceCode());
            execution.setInput(testCase.getInput());
            execution.setExpectedOutput(testCase.getExpectedOutput());
            execution.setTimeLimitSeconds(15);

            CodeExecutionResponseDTO result = codeExecutionService.execute(execution);
            boolean passedCase = result.isPassed();
            if (passedCase) {
                passed++;
                earnedPoints += testCase.getPoints() == null ? 0 : testCase.getPoints();
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("testCaseId", testCase.getId());
            item.put("passed", passedCase);
            item.put("hidden", testCase.isHidden());
            item.put("output", testCase.isHidden() ? "" : result.getOutput());
            item.put("error", result.getError());
            item.put("executionTimeMs", result.getExecutionTimeMs());
            results.add(item);
        }

        return ResponseEntity.ok(Map.of(
                "passed", passed,
                "total", cases.size(),
                "earnedPoints", earnedPoints,
                "totalPoints", totalPoints,
                "results", results
        ));
    }
}
