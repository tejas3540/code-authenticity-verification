package com.vericode.vericode_backend.dto;

public class QuestionImportResponseDTO {
    private String source;
    private String sourceUrl;
    private String title;
    private String description;
    private String inputDescription;
    private String outputDescription;
    private String constraints;
    private Integer timeLimitSeconds;
    private String language;
    private String note;

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getInputDescription() { return inputDescription; }
    public void setInputDescription(String inputDescription) { this.inputDescription = inputDescription; }
    public String getOutputDescription() { return outputDescription; }
    public void setOutputDescription(String outputDescription) { this.outputDescription = outputDescription; }
    public String getConstraints() { return constraints; }
    public void setConstraints(String constraints) { this.constraints = constraints; }
    public Integer getTimeLimitSeconds() { return timeLimitSeconds; }
    public void setTimeLimitSeconds(Integer timeLimitSeconds) { this.timeLimitSeconds = timeLimitSeconds; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
