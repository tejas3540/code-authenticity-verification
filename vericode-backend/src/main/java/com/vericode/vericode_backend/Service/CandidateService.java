package com.vericode.vericode_backend.Service;

import com.vericode.vericode_backend.dto.CandidateRequestDTO;
import com.vericode.vericode_backend.dto.CandidateResponseDTO;
import com.vericode.vericode_backend.exception.CandidateNotFoundException;
import com.vericode.vericode_backend.model.Candidate;
import com.vericode.vericode_backend.repository.CandidateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CandidateService {
    @Autowired
    private CandidateRepository candidateRepository;

    public CandidateResponseDTO save(CandidateRequestDTO request){
        Candidate candidate = new Candidate();
        candidate.setName(request.getName());
        candidate.setEmail(request.getEmail());

        Candidate savedCandidate = candidateRepository.save(candidate);
        CandidateResponseDTO response = new CandidateResponseDTO();
        response.setId(savedCandidate.getId());
        response.setName(savedCandidate.getName());
        response.setEmail(savedCandidate.getEmail());
        return response;
    }

    public List<Candidate> getAllCandidates(){
        return candidateRepository.findAll();
    }
    public Candidate updateCandidate(Candidate candidate){
        Optional<Candidate> candidateBox =
                candidateRepository.findById(candidate.getId());

        if(candidateBox.isPresent()){
            return candidateRepository.save(candidate);
        }
        else{
            throw new CandidateNotFoundException(
                    "Candidate not found with id "
                            + candidate.getId()
            );
        }
    }
    public Candidate deleteCandidate(Integer id){
        Optional<Candidate> candidateBox =
                candidateRepository.findById(id);
        if(candidateBox.isPresent()){
            candidateRepository.deleteById(id);
            return candidateBox.get();
        }
        else{
            throw new CandidateNotFoundException("Candidate not found with id "+id);
        }
    }
    public CandidateResponseDTO getCandidateById(Integer id){
        Optional<Candidate> candidateBox =
                candidateRepository.findById(id);
        if(candidateBox.isPresent()){
            Candidate candidate = candidateBox.get();
            CandidateResponseDTO response = new CandidateResponseDTO();
            response.setId(candidate.getId());
            response.setName(candidate.getName());
            response.setEmail(candidate.getEmail());
            return response;
        }
        else{
            throw new CandidateNotFoundException(
                    "Candidate not found with id " + id
            );
        }
    }

}
