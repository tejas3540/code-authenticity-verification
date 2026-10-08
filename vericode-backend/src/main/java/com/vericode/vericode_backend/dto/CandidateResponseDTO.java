package com.vericode.vericode_backend.dto;

public class CandidateResponseDTO {

    private Integer id;
    private String name;
    private String email;

    public CandidateResponseDTO() {
    }

    public CandidateResponseDTO(Integer id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
