package com.vericode.vericode_backend.controller;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.vericode.vericode_backend.dto.SubmissionRequestDTO;
import com.vericode.vericode_backend.model.*;
import com.vericode.vericode_backend.repository.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/Submissions")
public class SubmissionController {
    private final SubmissionRepository submissions;
    private final AssessmentRepository assessments;
    private final QuestionRepository questions;
    private final UserAccountRepository users;
    private final ObjectMapper objectMapper;

    public SubmissionController(SubmissionRepository submissions, AssessmentRepository assessments,
                                QuestionRepository questions, UserAccountRepository users,
                                ObjectMapper objectMapper) {
        this.submissions = submissions;
        this.assessments = assessments;
        this.questions = questions;
        this.users = users;
        this.objectMapper = objectMapper;
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
        try {
            List<SubmissionRequestDTO.BehaviorEventDTO> events =
                    request.getBehaviorEvents() == null ? List.of() : request.getBehaviorEvents();
            submission.setBehaviorEventsJson(objectMapper.writeValueAsString(events));
        } catch (Exception exception) {
            return ResponseEntity.badRequest().body(Map.of("message", "Behavior event data could not be processed."));
        }

        Submission saved = submissions.save(submission);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", saved.getId(),
                "message", "Code submitted successfully.",
                "submittedAt", saved.getSubmittedAt().toString()
        ));
    }

    @GetMapping("/recruiter")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getRecruiterSubmissions(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!"RECRUITER".equals(role)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        List<Submission> recruiterSubmissions =
                submissions.findByAssessment_Recruiter_IdOrderBySubmittedAtDesc(userId);
        List<Map<String, Object>> response = recruiterSubmissions.stream()
                .map(submission -> toRecruiterResponse(submission, recruiterSubmissions))
                .toList();
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> toRecruiterResponse(Submission submission, List<Submission> recruiterSubmissions) {
        List<Map<String, Object>> events = readEvents(submission.getBehaviorEventsJson());
        Map<String, Long> eventCounts = events.stream()
                .collect(Collectors.groupingBy(
                        event -> String.valueOf(event.getOrDefault("eventType", "unknown")),
                        TreeMap::new,
                        Collectors.counting()));

        String normalizedCode = normalizeCode(submission.getSourceCode());
        long identicalCodeMatches = recruiterSubmissions.stream()
                .filter(other -> !other.getId().equals(submission.getId()))
                .filter(other -> normalizeCode(other.getSourceCode()).equals(normalizedCode))
                .count();

        List<String> reviewIndicators = new ArrayList<>();
        long pasteEvents = eventCounts.getOrDefault("paste", 0L);
        long focusLossEvents = eventCounts.getOrDefault("window_blur", 0L)
                + eventCounts.getOrDefault("visibility_hidden", 0L);
        if (pasteEvents > 0) reviewIndicators.add("Paste events were recorded; review them in context.");
        if (focusLossEvents > 0) reviewIndicators.add("The assessment page lost focus or visibility during the session.");
        if (identicalCodeMatches > 0) reviewIndicators.add("An identical normalized source-code match exists in another submission in this recruiter's assessments.");
        if (events.isEmpty()) reviewIndicators.add("No behavior events were captured; behavior-based evidence is limited.");
        if (reviewIndicators.isEmpty()) reviewIndicators.add("No basic review indicators were detected by the current checks.");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", submission.getId());
        result.put("studentId", submission.getStudent().getId());
        result.put("studentName", submission.getStudent().getName());
        result.put("studentEmail", submission.getStudent().getEmail());
        result.put("assessmentId", submission.getAssessment().getId());
        result.put("assessmentTitle", submission.getAssessment().getTitle());
        result.put("questionId", submission.getQuestion().getId());
        result.put("questionTitle", submission.getQuestion().getTitle());
        result.put("sourceCode", submission.getSourceCode());
        result.put("submittedAt", submission.getSubmittedAt().toString());
        result.put("behaviorEvents", events);
        result.put("eventCounts", eventCounts);
        result.put("sourceLines", submission.getSourceCode().split("\\R", -1).length);
        result.put("sourceCharacters", submission.getSourceCode().length());
        result.put("identicalCodeMatches", identicalCodeMatches);
        result.put("analysisStatus", events.isEmpty() && identicalCodeMatches == 0 ? "LIMITED_EVIDENCE" : "READY_FOR_REVIEW");
        result.put("reviewIndicators", reviewIndicators);
        result.put("analysisNotice", "These are review indicators, not proof of misconduct or AI use. The current checks do not produce a validated authenticity score.");
        return result;
    }

    private List<Map<String, Object>> readEvents(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private String normalizeCode(String sourceCode) {
        return sourceCode == null ? "" : sourceCode.replaceAll("\\s+", "");
    }
}