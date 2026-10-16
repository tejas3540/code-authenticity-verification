package com.vericode.vericode_backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TestCaseRequestDTO {
    @NotNull private Integer questionId;
    @NotBlank private String input;
    @NotBlank private String expectedOutput;
    private boolean hidden;
    @NotNull @Min(0) private Integer points = 0;

    public Integer getQuestionId() { return questionId; }
    public String getInput() { return input; }
    public String getExpectedOutput() { return expectedOutput; }
    public boolean isHidden() { return hidden; }
    public Integer getPoints() { return points; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }
    public void setInput(String input) { this.input = input; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }
    public void setPoints(Integer points) { this.points = points; }
}
