package com.vericode.vericode_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class StudentRegisterRequestDTO {

    @NotBlank private String name;
    @Email @NotBlank private String email;
    @NotBlank private String password;
    @NotBlank private String college;
    @NotBlank private String currentStudyField;

    public StudentRegisterRequestDTO() {}

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getCollege() { return college; }
    public String getCurrentStudyField() { return currentStudyField; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setCollege(String college) { this.college = college; }
    public void setCurrentStudyField(String currentStudyField) { this.currentStudyField = currentStudyField; }
}
