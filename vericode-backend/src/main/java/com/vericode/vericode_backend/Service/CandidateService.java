package com.vericode.vericode_backend.Service;

import com.vericode.vericode_backend.dto.CandidateRequestDTO;
import com.vericode.vericode_backend.dto.CandidateResponseDTO;
import com.vericode.vericode_backend.exception.CandidateNotFoundException;
import com.vericode.vericode_backend.model.Candidate;
import com.vericode.vericode_backend.repository.CandidateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CandidateService {

    @Autowired
    private CandidateRepository candidateRepository;

    public CandidateResponseDTO save(CandidateRequestDTO request) {
        Candidate candidate = new Candidate();
        candidate.setName(request.getName());
        candidate.setEmail(request.getEmail());

        Candidate savedCandidate = candidateRepository.save(candidate);
        return toResponseDTO(savedCandidate);
    }

    public List<CandidateResponseDTO> getAllCandidates() {
        return candidateRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public CandidateResponseDTO updateCandidate(
            Integer id,
            CandidateRequestDTO request) {

        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new CandidateNotFoundException(
                        "Candidate not found with id " + id
                ));

        candidate.setName(request.getName());
        candidate.setEmail(request.getEmail());

        Candidate updatedCandidate = candidateRepository.save(candidate);
        return toResponseDTO(updatedCandidate);
    }

    public CandidateResponseDTO deleteCandidate(Integer id) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new CandidateNotFoundException(
                        "Candidate not found with id " + id
                ));

        candidateRepository.delete(candidate);
        return toResponseDTO(candidate);
    }

    public CandidateResponseDTO getCandidateById(Integer id) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new CandidateNotFoundException(
                        "Candidate not found with id " + id
                ));

        return toResponseDTO(candidate);
    }

    private CandidateResponseDTO toResponseDTO(Candidate candidate) {
        CandidateResponseDTO response = new CandidateResponseDTO();
        response.setId(candidate.getId());
        response.setName(candidate.getName());
        response.setEmail(candidate.getEmail());
        return response;
    }
}
