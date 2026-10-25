package com.vericode.vericode_backend.repository;

import com.vericode.vericode_backend.model.AssessmentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssessmentAttemptRepository extends JpaRepository<AssessmentAttempt, Integer> {
    Optional<AssessmentAttempt> findByAssessment_IdAndStudent_Id(Integer assessmentId, Integer studentId);
}
