package com.vericode.vericode_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CodeEvaluationRequestDTO {
    @NotNull private Integer questionId;
    @NotBlank private String sourceCode;

    public Integer getQuestionId() { return questionId; }
    public String getSourceCode() { return sourceCode; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }
}
