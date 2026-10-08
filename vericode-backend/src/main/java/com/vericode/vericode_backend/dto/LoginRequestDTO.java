package com.vericode.vericode_backend.dto;

import com.vericode.vericode_backend.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LoginRequestDTO {

    @Email @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotNull
    private Role role;

    public LoginRequestDTO() {}

    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }

    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(Role role) { this.role = role; }
}
