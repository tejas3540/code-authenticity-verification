package com.vericode.vericode_backend.dto;

import com.vericode.vericode_backend.model.Role;

public class AuthResponseDTO {

    private Integer userId;
    private String name;
    private String email;
    private Role role;
    private String college;
    private String currentStudyField;
    private String organizationName;

    public AuthResponseDTO() {}

    public Integer getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public String getCollege() { return college; }
    public String getCurrentStudyField() { return currentStudyField; }
    public String getOrganizationName() { return organizationName; }

    public void setUserId(Integer userId) { this.userId = userId; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(Role role) { this.role = role; }
    public void setCollege(String college) { this.college = college; }
    public void setCurrentStudyField(String currentStudyField) { this.currentStudyField = currentStudyField; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
}
