package com.vericode.vericode_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SubmissionRequestDTO {
    @NotNull private Integer assessmentId;
    @NotNull private Integer questionId;
    @NotBlank private String sourceCode;
    public Integer getAssessmentId() { return assessmentId; }
    public Integer getQuestionId() { return questionId; }
    public String getSourceCode() { return sourceCode; }
    public void setAssessmentId(Integer assessmentId) { this.assessmentId = assessmentId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }
}