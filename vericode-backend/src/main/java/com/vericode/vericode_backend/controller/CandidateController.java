package com.vericode.vericode_backend.controller;

import com.vericode.vericode_backend.dto.CandidateRequestDTO;
import com.vericode.vericode_backend.dto.CandidateResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.vericode.vericode_backend.model.Candidate;
import com.vericode.vericode_backend.Service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/Candidates")
public class CandidateController {

    @Autowired
    private CandidateService candidateService;
    @GetMapping("/{id}")
    public ResponseEntity<CandidateResponseDTO> getCandidate(
            @PathVariable Integer id){

        return ResponseEntity.ok(
                candidateService.getCandidateById(id)
        );
    }
    @GetMapping
    public ResponseEntity<List<Candidate>> getAllCandidate() {
      List<Candidate> candidates = candidateService.getAllCandidates();
      return ResponseEntity.ok().body(candidates);
    }
    @PostMapping
    public ResponseEntity<CandidateResponseDTO> save(@Valid  @RequestBody CandidateRequestDTO request) {

        CandidateResponseDTO response = candidateService.save(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);

    }
    @PutMapping
    public ResponseEntity<Candidate> updateCandidate(
            @Valid @RequestBody Candidate candidate) {

        Candidate updatedCandidate =
                candidateService.updateCandidate(candidate);

        return ResponseEntity.ok(updatedCandidate);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Candidate> deleteCandidate(
            @PathVariable Integer id) {

        Candidate deletedCandidate =
                candidateService.deleteCandidate(id);

        return ResponseEntity.ok(deletedCandidate);
    }
}


