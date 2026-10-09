package com.vericode.vericode_backend.dto;

import java.util.List;

public class AssessmentResponseDTO {
    private Integer id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private String recruiterName;
    private List<QuestionResponseDTO> questions;
    private List<String> studentEmails;

    public Integer getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public String getRecruiterName() { return recruiterName; }
    public List<QuestionResponseDTO> getQuestions() { return questions; }
    public List<String> getStudentEmails() { return studentEmails; }
    public void setId(Integer id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public void setRecruiterName(String recruiterName) { this.recruiterName = recruiterName; }
    public void setQuestions(List<QuestionResponseDTO> questions) { this.questions = questions; }
    public void setStudentEmails(List<String> studentEmails) { this.studentEmails = studentEmails; }
}