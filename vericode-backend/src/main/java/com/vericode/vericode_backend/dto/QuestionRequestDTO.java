package com.vericode.vericode_backend.dto;

import com.vericode.vericode_backend.model.Language;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class QuestionRequestDTO {

    @NotBlank(message = "Enter question title")
    private String title;

    @NotBlank(message = "Enter problem description")
    private String description;

    @NotBlank(message = "Enter input description")
    private String inputDescription;

    @NotBlank(message = "Enter output description")
    private String outputDescription;

    @NotBlank(message = "Enter constraints")
    private String constraints;

    @NotNull(message = "Enter time limit")
    @Min(value = 1, message = "Time limit must be at least 1 second")
    private Integer timeLimitSeconds;

    @NotNull(message = "Select language")
    private Language language = Language.JAVA;

    public QuestionRequestDTO() {
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
