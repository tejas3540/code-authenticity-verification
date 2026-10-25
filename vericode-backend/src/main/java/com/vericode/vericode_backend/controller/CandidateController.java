package com.vericode.vericode_backend.controller;

import com.vericode.vericode_backend.Service.CandidateService;
import com.vericode.vericode_backend.dto.CandidateRequestDTO;
import com.vericode.vericode_backend.dto.CandidateResponseDTO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Candidates")
public class CandidateController {

    @Autowired
    private CandidateService candidateService;

    @GetMapping("/{id}")
    public ResponseEntity<CandidateResponseDTO> getCandidate(
            @PathVariable Integer id, HttpSession session) {

        HttpStatus accessError = recruiterAccessError(session);
        if (accessError != null) return ResponseEntity.status(accessError).build();

        return ResponseEntity.ok(candidateService.getCandidateById(id));
    }

    @GetMapping
    public ResponseEntity<List<CandidateResponseDTO>> getAllCandidates(HttpSession session) {
        HttpStatus accessError = recruiterAccessError(session);
        if (accessError != null) return ResponseEntity.status(accessError).build();
        return ResponseEntity.ok(candidateService.getAllCandidates());
    }

    @PostMapping
    public ResponseEntity<CandidateResponseDTO> save(
            @Valid @RequestBody CandidateRequestDTO request, HttpSession session) {

        HttpStatus accessError = recruiterAccessError(session);
        if (accessError != null) return ResponseEntity.status(accessError).build();

        CandidateResponseDTO response = candidateService.save(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CandidateResponseDTO> updateCandidate(
            @PathVariable Integer id,
            @Valid @RequestBody CandidateRequestDTO request,
            HttpSession session) {

        HttpStatus accessError = recruiterAccessError(session);
        if (accessError != null) return ResponseEntity.status(accessError).build();

        CandidateResponseDTO response =
                candidateService.updateCandidate(id, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CandidateResponseDTO> deleteCandidate(
            @PathVariable Integer id, HttpSession session) {

        HttpStatus accessError = recruiterAccessError(session);
        if (accessError != null) return ResponseEntity.status(accessError).build();

        CandidateResponseDTO response =
                candidateService.deleteCandidate(id);

        return ResponseEntity.ok(response);
    }

    private HttpStatus recruiterAccessError(HttpSession session) {
        if (session.getAttribute("userId") == null) return HttpStatus.UNAUTHORIZED;
        if (!"RECRUITER".equals(session.getAttribute("role"))) return HttpStatus.FORBIDDEN;
        return null;
    }
}
