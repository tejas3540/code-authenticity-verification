package com.vericode.vericode_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public class CodeExecutionRequestDTO {
    @NotBlank(message = "Source code is required")
    private String sourceCode;
    private String input = "";
    private String expectedOutput = "";
    @NotNull @Min(1)
    private Integer timeLimitSeconds = 5;

    public String getSourceCode() { return sourceCode; }
    public String getInput() { return input == null ? "" : input; }
    public String getExpectedOutput() { return expectedOutput == null ? "" : expectedOutput; }
    public Integer getTimeLimitSeconds() { return timeLimitSeconds; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }
    public void setInput(String input) { this.input = input; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }
    public void setTimeLimitSeconds(Integer timeLimitSeconds) { this.timeLimitSeconds = timeLimitSeconds; }
}
