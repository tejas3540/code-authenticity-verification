package com.vericode.vericode_backend.dto;

public class TestCaseResponseDTO {
    private Integer id;
    private Integer questionId;
    private String input;
    private String expectedOutput;
    private boolean hidden;
    private Integer points;

    public TestCaseResponseDTO() {}
    public TestCaseResponseDTO(Integer id, Integer questionId, String input, String expectedOutput, boolean hidden, Integer points) {
        this.id = id;
        this.questionId = questionId;
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.hidden = hidden;
        this.points = points;
    }
    public Integer getId() { return id; }
    public Integer getQuestionId() { return questionId; }
    public String getInput() { return input; }
    public String getExpectedOutput() { return expectedOutput; }
    public boolean isHidden() { return hidden; }
    public Integer getPoints() { return points; }
}
