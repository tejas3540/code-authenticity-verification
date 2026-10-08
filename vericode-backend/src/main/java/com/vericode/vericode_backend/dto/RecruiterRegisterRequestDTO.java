package com.vericode.vericode_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class RecruiterRegisterRequestDTO {

    @NotBlank private String recruiterName;
    @Email @NotBlank private String email;
    @NotBlank private String password;
    @NotBlank private String organizationName;

    public RecruiterRegisterRequestDTO() {}

    public String getRecruiterName() { return recruiterName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getOrganizationName() { return organizationName; }

    public void setRecruiterName(String recruiterName) { this.recruiterName = recruiterName; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
}
