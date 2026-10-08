package com.vericode.vericode_backend.dto;

import com.vericode.vericode_backend.model.Language;

public class QuestionResponseDTO {

    private Integer id;
    private String title;
    private String description;
    private String inputDescription;
    private String outputDescription;
    private String constraints;
    private Integer timeLimitSeconds;
    private Language language;

    public QuestionResponseDTO() {
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getInputDescription() {
        return inputDescription;
    }

    public String getOutputDescription() {
        return outputDescription;
    }

    public String getConstraints() {
        return constraints;
    }

    public Integer getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public Language getLanguage() {
        return language;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setInputDescription(String inputDescription) {
        this.inputDescription = inputDescription;
    }

    public void setOutputDescription(String outputDescription) {
        this.outputDescription = outputDescription;
    }

    public void setConstraints(String constraints) {
        this.constraints = constraints;
    }

    public void setTimeLimitSeconds(Integer timeLimitSeconds) {
        this.timeLimitSeconds = timeLimitSeconds;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }
}
