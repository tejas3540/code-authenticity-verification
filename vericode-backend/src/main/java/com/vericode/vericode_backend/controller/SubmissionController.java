package com.vericode.vericode_backend.controller;

import com.vericode.vericode_backend.dto.SubmissionRequestDTO;
import com.vericode.vericode_backend.model.*;
import com.vericode.vericode_backend.repository.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/Submissions")
public class SubmissionController {
    private final SubmissionRepository submissions;
    private final AssessmentRepository assessments;
    private final QuestionRepository questions;
    private final UserAccountRepository users;

    public SubmissionController(SubmissionRepository submissions, AssessmentRepository assessments,
                                QuestionRepository questions, UserAccountRepository users) {
        this.submissions = submissions;
        this.assessments = assessments;
        this.questions = questions;
        this.users = users;
    }

    @PostMapping
    public ResponseEntity<?> submit(@Valid @RequestBody SubmissionRequestDTO request, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!"STUDENT".equals(role)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        Assessment assessment = assessments.findById(request.getAssessmentId()).orElse(null);
        Question question = questions.findById(request.getQuestionId()).orElse(null);
        UserAccount student = users.findById(userId).orElse(null);
        if (assessment == null || question == null || student == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Assessment, question, or student account was not found."));
        }
        boolean assigned = assessment.getStudents().stream().anyMatch(s -> s.getId().equals(userId));
        boolean included = assessment.getQuestions().stream().anyMatch(q -> q.getId().equals(question.getId()));
        if (!assigned || !included) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "This question is not part of an assessment assigned to you."));
        }

        Submission submission = new Submission();
        submission.setAssessment(assessment);
        submission.setQuestion(question);
        submission.setStudent(student);
        submission.setSourceCode(request.getSourceCode());
        Submission saved = submissions.save(submission);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", saved.getId(),
                "message", "Code submitted successfully.",
                "submittedAt", saved.getSubmittedAt().toString()
        ));
    }
}