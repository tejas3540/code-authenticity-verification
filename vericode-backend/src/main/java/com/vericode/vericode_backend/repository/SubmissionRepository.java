package com.vericode.vericode_backend.repository;

import com.vericode.vericode_backend.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Integer> {
    List<Submission> findByStudent_Id(Integer studentId);
    List<Submission> findByAssessment_Id(Integer assessmentId);
    List<Submission> findByAssessment_Recruiter_IdOrderBySubmittedAtDesc(Integer recruiterId);
}