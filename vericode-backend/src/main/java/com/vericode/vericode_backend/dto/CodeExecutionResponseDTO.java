package com.vericode.vericode_backend.dto;

public class CodeExecutionResponseDTO {
    private boolean compiled;
    private boolean passed;
    private boolean timedOut;
    private String output;
    private String error;
    private long executionTimeMs;

    public CodeExecutionResponseDTO(boolean compiled, boolean passed, boolean timedOut, String output, String error, long executionTimeMs) {
        this.compiled = compiled;
        this.passed = passed;
        this.timedOut = timedOut;
        this.output = output;
        this.error = error;
        this.executionTimeMs = executionTimeMs;
    }

    public boolean isCompiled() { return compiled; }
    public boolean isPassed() { return passed; }
    public boolean isTimedOut() { return timedOut; }
    public String getOutput() { return output; }
    public String getError() { return error; }
    public long getExecutionTimeMs() { return executionTimeMs; }
}
