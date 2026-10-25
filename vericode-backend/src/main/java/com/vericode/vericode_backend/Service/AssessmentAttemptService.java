package com.vericode.vericode_backend.Service;

import com.vericode.vericode_backend.dto.AssessmentAttemptResponseDTO;
import com.vericode.vericode_backend.model.Assessment;
import com.vericode.vericode_backend.model.AssessmentAttempt;
import com.vericode.vericode_backend.repository.AssessmentAttemptRepository;
import com.vericode.vericode_backend.repository.AssessmentRepository;
import com.vericode.vericode_backend.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class AssessmentAttemptService {
    public static final long SUBMISSION_GRACE_SECONDS = 10;

    private final AssessmentAttemptRepository attemptRepository;
    private final AssessmentRepository assessmentRepository;
    private final UserAccountRepository userRepository;
    private final Clock clock;

    public AssessmentAttemptService(AssessmentAttemptRepository attemptRepository,
                                    AssessmentRepository assessmentRepository,
                                    UserAccountRepository userRepository,
                                    Clock clock) {
        this.attemptRepository = attemptRepository;
        this.assessmentRepository = assessmentRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Transactional
    public AssessmentAttemptResponseDTO start(Integer assessmentId, Integer studentId, boolean consentConfirmed) {
        if (!consentConfirmed) {
            throw new IllegalArgumentException("Confirm the recording notice before starting the assessment.");
        }

        Optional<AssessmentAttempt> existing = attemptRepository
                .findByAssessment_IdAndStudent_Id(assessmentId, studentId);
        if (existing.isPresent()) return toResponse(existing.get());

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found."));
        if (!assessmentRepository.isStudentAssignedToAssessment(assessmentId, studentId)) {
            throw new IllegalArgumentException("This assessment is not assigned to your account.");
        }

        Instant startedAt = clock.instant();
        AssessmentAttempt attempt = new AssessmentAttempt();
        attempt.setAssessment(assessment);
        attempt.setStudent(userRepository.getReferenceById(studentId));
        attempt.setConsentAt(startedAt);
        attempt.setStartedAt(startedAt);
        attempt.setDeadlineAt(startedAt.plusSeconds(assessment.getDurationMinutes() * 60L));
        return toResponse(attemptRepository.save(attempt));
    }

    @Transactional(readOnly = true)
    public Optional<AssessmentAttemptResponseDTO> get(Integer assessmentId, Integer studentId) {
        return attemptRepository.findByAssessment_IdAndStudent_Id(assessmentId, studentId)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public SubmissionStatus submissionStatus(Integer assessmentId, Integer studentId) {
        Optional<AssessmentAttempt> attempt = attemptRepository
                .findByAssessment_IdAndStudent_Id(assessmentId, studentId);
        if (attempt.isEmpty()) return SubmissionStatus.NOT_STARTED;
        if (attempt.get().getSubmittedAt() != null) return SubmissionStatus.COMPLETE;
        Instant graceDeadline = attempt.get().getDeadlineAt().plusSeconds(SUBMISSION_GRACE_SECONDS);
        return clock.instant().isAfter(graceDeadline) ? SubmissionStatus.EXPIRED : SubmissionStatus.OPEN;
    }

    @Transactional
    public boolean markCompleteIfAllQuestionsSubmitted(Integer assessmentId, Integer studentId,
                                                        long submittedQuestionCount, int questionCount) {
        AssessmentAttempt attempt = attemptRepository
                .findByAssessment_IdAndStudent_Id(assessmentId, studentId)
                .orElseThrow(() -> new IllegalStateException("Assessment attempt was not started."));
        if (attempt.getSubmittedAt() != null) return true;
        if (questionCount > 0 && submittedQuestionCount >= questionCount) {
            attempt.setSubmittedAt(clock.instant());
            attemptRepository.save(attempt);
            return true;
        }
        return false;
    }

    private AssessmentAttemptResponseDTO toResponse(AssessmentAttempt attempt) {
        Instant now = clock.instant();
        Instant deadline = attempt.getDeadlineAt();
        Instant graceDeadline = deadline.plusSeconds(SUBMISSION_GRACE_SECONDS);
        long remainingSeconds = ceilSeconds(Duration.between(now, deadline).toMillis());
        long graceSecondsRemaining = ceilSeconds(Duration.between(now, graceDeadline).toMillis());
        boolean allowed = attempt.getSubmittedAt() == null && !now.isAfter(graceDeadline);
        return new AssessmentAttemptResponseDTO(
                attempt.getId(), attempt.getConsentAt(), attempt.getStartedAt(), deadline,
                attempt.getSubmittedAt(), remainingSeconds, graceSecondsRemaining, allowed);
    }

    private long ceilSeconds(long milliseconds) {
        return milliseconds <= 0 ? 0 : (milliseconds + 999) / 1000;
    }

    public enum SubmissionStatus {
        NOT_STARTED,
        OPEN,
        EXPIRED,
        COMPLETE
    }
}
