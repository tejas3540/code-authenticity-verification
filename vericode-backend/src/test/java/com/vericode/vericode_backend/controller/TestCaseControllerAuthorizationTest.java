package com.vericode.vericode_backend.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TestCaseControllerAuthorizationTest {

    private LocalValidatorFactoryBean validator;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        // Rejected requests must return before the controller uses either dependency.
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestCaseController(null, null))
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }

    @Test
    void createWithoutSessionReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/TestCases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createWithRecruiterRoleButNoUserIdReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/TestCases")
                        .sessionAttr("role", "RECRUITER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createWithStudentSessionReturnsForbidden() throws Exception {
        mockMvc.perform(post("/TestCases")
                        .sessionAttr("userId", 42)
                        .sessionAttr("role", "STUDENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteWithoutSessionReturnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/TestCases/7"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteWithStudentSessionReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/TestCases/7")
                        .sessionAttr("userId", 42)
                        .sessionAttr("role", "STUDENT"))
                .andExpect(status().isForbidden());
    }

    private String validRequest() {
        return """
                {
                  "questionId": 1,
                  "input": "1",
                  "expectedOutput": "1",
                  "hidden": false,
                  "points": 1
                }
                """;
    }
}
