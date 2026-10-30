package com.vericode.vericode_backend.controller;

import com.vericode.vericode_backend.Service.AuthService;
import com.vericode.vericode_backend.dto.AuthResponseDTO;
import com.vericode.vericode_backend.dto.LoginRequestDTO;
import com.vericode.vericode_backend.dto.RecruiterRegisterRequestDTO;
import com.vericode.vericode_backend.dto.StudentRegisterRequestDTO;
import com.vericode.vericode_backend.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerValidationTest {

    private LocalValidatorFactoryBean validator;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(new UnexpectedAuthService()))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }

    @Test
    void studentRegistrationRejectsBlankName() throws Exception {
        assertBadRequest("/Auth/register/student", """
                {
                  "name": " ",
                  "email": "student@example.com",
                  "password": "StrongPass123",
                  "college": "Example College",
                  "currentStudyField": "Computer Science"
                }
                """);
    }

    @Test
    void studentRegistrationRejectsMalformedEmail() throws Exception {
        assertBadRequest("/Auth/register/student", """
                {
                  "name": "Tejas",
                  "email": "not-an-email",
                  "password": "StrongPass123",
                  "college": "Example College",
                  "currentStudyField": "Computer Science"
                }
                """);
    }

    @Test
    void studentRegistrationRejectsBlankCollege() throws Exception {
        assertBadRequest("/Auth/register/student", """
                {
                  "name": "Tejas",
                  "email": "student@example.com",
                  "password": "StrongPass123",
                  "college": " ",
                  "currentStudyField": "Computer Science"
                }
                """);
    }

    @Test
    void studentRegistrationRejectsBlankPassword() throws Exception {
        assertBadRequest("/Auth/register/student", """
                {
                  "name": "Tejas",
                  "email": "student@example.com",
                  "password": " ",
                  "college": "Example College",
                  "currentStudyField": "Computer Science"
                }
                """);
    }

    @Test
    void recruiterRegistrationRejectsBlankOrganizationName() throws Exception {
        assertBadRequest("/Auth/register/recruiter", """
                {
                  "recruiterName": "Recruiter",
                  "email": "recruiter@example.com",
                  "password": "StrongPass123",
                  "organizationName": " "
                }
                """);
    }

    @Test
    void loginRejectsMissingRole() throws Exception {
        assertBadRequest("/Auth/login", """
                {
                  "email": "student@example.com",
                  "password": "StrongPass123",
                  "role": null
                }
                """);
    }

    private void assertBadRequest(String endpoint, String requestBody) throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    private static class UnexpectedAuthService extends AuthService {

        @Override
        public AuthResponseDTO registerStudent(StudentRegisterRequestDTO request) {
            throw new AssertionError("Invalid student registration must not reach the service");
        }

        @Override
        public AuthResponseDTO registerRecruiter(RecruiterRegisterRequestDTO request) {
            throw new AssertionError("Invalid recruiter registration must not reach the service");
        }

        @Override
        public AuthResponseDTO login(LoginRequestDTO request) {
            throw new AssertionError("Invalid login must not reach the service");
        }
    }
}
