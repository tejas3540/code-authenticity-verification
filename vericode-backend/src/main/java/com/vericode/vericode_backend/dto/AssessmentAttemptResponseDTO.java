package com.vericode.vericode_backend.dto;

import java.time.Instant;

public class AssessmentAttemptResponseDTO {
    private final Integer id;
    private final Instant consentAt;
    private final Instant startedAt;
    private final Instant deadlineAt;
    private final Instant submittedAt;
    private final long remainingSeconds;
    private final long graceSecondsRemaining;
    private final boolean submissionAllowed;

    public AssessmentAttemptResponseDTO(Integer id, Instant consentAt, Instant startedAt,
                                        Instant deadlineAt, Instant submittedAt,
                                        long remainingSeconds, long graceSecondsRemaining,
                                        boolean submissionAllowed) {
        this.id = id;
        this.consentAt = consentAt;
        this.startedAt = startedAt;
        this.deadlineAt = deadlineAt;
        this.submittedAt = submittedAt;
        this.remainingSeconds = remainingSeconds;
        this.graceSecondsRemaining = graceSecondsRemaining;
        this.submissionAllowed = submissionAllowed;
    }

    public Integer getId() { return id; }
    public Instant getConsentAt() { return consentAt; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getDeadlineAt() { return deadlineAt; }
    public Instant getSubmittedAt() { return submittedAt; }
    public long getRemainingSeconds() { return remainingSeconds; }
    public long getGraceSecondsRemaining() { return graceSecondsRemaining; }
    public boolean isSubmissionAllowed() { return submissionAllowed; }
}
