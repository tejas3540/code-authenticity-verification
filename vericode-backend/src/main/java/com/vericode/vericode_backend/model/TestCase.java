package com.vericode.vericode_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "test_cases", indexes = {
        @Index(name = "idx_test_cases_question", columnList = "question_id")
})
public class TestCase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String input;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String expectedOutput;

    @Column(nullable = false)
    private boolean hidden;

    @Column(nullable = false)
    private Integer points = 0;

    public Integer getId() { return id; }
    public Question getQuestion() { return question; }
    public String getInput() { return input; }
    public String getExpectedOutput() { return expectedOutput; }
    public boolean isHidden() { return hidden; }
    public Integer getPoints() { return points; }
    public void setQuestion(Question question) { this.question = question; }
    public void setInput(String input) { this.input = input; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }
    public void setPoints(Integer points) { this.points = points; }
}
