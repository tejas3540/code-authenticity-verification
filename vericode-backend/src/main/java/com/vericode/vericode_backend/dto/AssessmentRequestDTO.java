package com.vericode.vericode_backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class AssessmentRequestDTO {
    @NotBlank private String title;
    private String description;
    @NotNull @Min(1) private Integer durationMinutes;
    @NotEmpty private List<Integer> questionIds;
    @NotEmpty private List<String> studentEmails;

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public List<Integer> getQuestionIds() { return questionIds; }
    public List<String> getStudentEmails() { return studentEmails; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public void setQuestionIds(List<Integer> questionIds) { this.questionIds = questionIds; }
    public void setStudentEmails(List<String> studentEmails) { this.studentEmails = studentEmails; }
}