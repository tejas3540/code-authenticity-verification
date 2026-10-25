package com.vericode.vericode_backend.repository;

import com.vericode.vericode_backend.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Integer> {
    List<Submission> findByStudent_Id(Integer studentId);
    List<Submission> findByAssessment_Id(Integer assessmentId);
    List<Submission> findByAssessment_Recruiter_IdOrderBySubmittedAtDesc(Integer recruiterId);

    @Query("select count(distinct submission.question.id) from Submission submission " +
            "where submission.assessment.id = :assessmentId and submission.student.id = :studentId")
    long countDistinctQuestionsForAssessmentAndStudent(@Param("assessmentId") Integer assessmentId,
                                                       @Param("studentId") Integer studentId);
}
