package com.vericode.vericode_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String inputDescription;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String outputDescription;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String constraints;

    @Column(nullable = false)
    private Integer timeLimitSeconds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language = Language.JAVA;

    public Question() {
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
