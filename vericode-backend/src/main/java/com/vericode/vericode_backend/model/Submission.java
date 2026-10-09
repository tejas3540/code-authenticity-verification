package com.vericode.vericode_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "submissions")
public class Submission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(optional = false) @JoinColumn(name = "assessment_id")
    private Assessment assessment;
    @ManyToOne(optional = false) @JoinColumn(name = "question_id")
    private Question question;
    @ManyToOne(optional = false) @JoinColumn(name = "student_id")
    private UserAccount student;
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String sourceCode;
    @Column(nullable = false)
    private LocalDateTime submittedAt;

    public Submission() {}
    @PrePersist public void beforeSave() { if (submittedAt == null) submittedAt = LocalDateTime.now(); }
    public Integer getId() { return id; }
    public Assessment getAssessment() { return assessment; }
    public Question getQuestion() { return question; }
    public UserAccount getStudent() { return student; }
    public String getSourceCode() { return sourceCode; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setAssessment(Assessment assessment) { this.assessment = assessment; }
    public void setQuestion(Question question) { this.question = question; }
    public void setStudent(UserAccount student) { this.student = student; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }
}