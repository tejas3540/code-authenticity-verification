package com.vericode.vericode_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_accounts")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String college;
    private String currentStudyField;
    private String organizationName;

    public UserAccount() {
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public String getCollege() { return college; }
    public String getCurrentStudyField() { return currentStudyField; }
    public String getOrganizationName() { return organizationName; }

    public void setId(Integer id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(Role role) { this.role = role; }
    public void setCollege(String college) { this.college = college; }
    public void setCurrentStudyField(String currentStudyField) { this.currentStudyField = currentStudyField; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
}
