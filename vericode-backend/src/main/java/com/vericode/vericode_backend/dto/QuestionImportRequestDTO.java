package com.vericode.vericode_backend.dto;

import jakarta.validation.constraints.NotBlank;

public class QuestionImportRequestDTO {
    @NotBlank(message = "Enter a LeetCode or HackerRank question URL")
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
