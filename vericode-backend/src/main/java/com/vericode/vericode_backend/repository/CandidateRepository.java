package com.vericode.vericode_backend.repository;

import com.vericode.vericode_backend.model.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateRepository
    extends JpaRepository<Candidate,Integer>{

}
