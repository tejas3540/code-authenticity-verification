package com.vericode.vericode_backend.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "assessment_attempts", uniqueConstraints = @UniqueConstraint(
        name = "uq_assessment_attempt_assessment_student",
        columnNames = {"assessment_id", "student_id"}
))
public class AssessmentAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Assessment assessment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private UserAccount student;

    @Column(name = "consent_at", nullable = false)
    private Instant consentAt;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "deadline_at", nullable = false)
    private Instant deadlineAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    public Integer getId() { return id; }
    public Assessment getAssessment() { return assessment; }
    public UserAccount getStudent() { return student; }
    public Instant getConsentAt() { return consentAt; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getDeadlineAt() { return deadlineAt; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setAssessment(Assessment assessment) { this.assessment = assessment; }
    public void setStudent(UserAccount student) { this.student = student; }
    public void setConsentAt(Instant consentAt) { this.consentAt = consentAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public void setDeadlineAt(Instant deadlineAt) { this.deadlineAt = deadlineAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
}
