package com.vericode.vericode_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CandidateRequestDTO {

    @NotBlank(message = "Enter Name")
    @Pattern(
            regexp = "^[A-Za-z ]+$",
            message = "Name can contain only letters and spaces"
    )
    private String name;

    @NotBlank(message = "Enter email")
    @Email(message = "Invalid email")
    private String email;

    public CandidateRequestDTO() {
    }

    public CandidateRequestDTO(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
