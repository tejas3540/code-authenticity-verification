package com.vericode.vericode_backend.controller;

import com.vericode.vericode_backend.Service.AuthService;
import com.vericode.vericode_backend.dto.AuthResponseDTO;
import com.vericode.vericode_backend.dto.LoginRequestDTO;
import com.vericode.vericode_backend.model.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerSessionTest {

    private LocalValidatorFactoryBean validator;
    private MockMvc mockMvc;
    private StubAuthService authService;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        authService = new StubAuthService(studentResponse());
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authService))
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }

    @Test
    void loginStoresUserIdAndRoleInHttpSession() throws Exception {
        mockMvc.perform(post("/Auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "student@example.com",
                                  "password": "StrongPass123",
                                  "role": "STUDENT"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(request().sessionAttribute("userId", 21))
                .andExpect(request().sessionAttribute("role", "STUDENT"));
    }

    @Test
    void loginRotatesExistingSessionIdBeforeAuthentication() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String originalSessionId = session.getId();

        mockMvc.perform(post("/Auth/login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "student@example.com",
                                  "password": "StrongPass123",
                                  "role": "STUDENT"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(request().sessionAttribute("userId", 21))
                .andExpect(request().sessionAttribute("role", "STUDENT"));

        assertNotEquals(originalSessionId, session.getId(),
                "Authentication must rotate an existing pre-login session ID");
    }

    @Test
    void currentUserWithoutSessionReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/Auth/me"))
                .andExpect(status().isUnauthorized());

        assertNull(authService.requestedUserId,
                "The service must not be called when the session is unauthenticated");
    }

    @Test
    void currentUserLoadsTheAccountFromTheSessionUserId() throws Exception {
        mockMvc.perform(get("/Auth/me").sessionAttr("userId", 21))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(21))
                .andExpect(jsonPath("$.email").value("student@example.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"));

        assertEquals(21, authService.requestedUserId);
    }

    @Test
    void logoutInvalidatesTheExistingSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 21);
        session.setAttribute("role", "STUDENT");

        mockMvc.perform(post("/Auth/logout").session(session))
                .andExpect(status().isNoContent());

        assertThrows(IllegalStateException.class, () -> session.getAttribute("userId"),
                "The session must no longer be usable after logout");
    }

    private AuthResponseDTO studentResponse() {
        AuthResponseDTO response = new AuthResponseDTO();
        response.setUserId(21);
        response.setName("Test Student");
        response.setEmail("student@example.com");
        response.setRole(Role.STUDENT);
        response.setCollege("Example College");
        response.setCurrentStudyField("Computer Science");
        return response;
    }

    private static class StubAuthService extends AuthService {
        private final AuthResponseDTO response;
        private Integer requestedUserId;

        private StubAuthService(AuthResponseDTO response) {
            this.response = response;
        }

        @Override
        public AuthResponseDTO login(LoginRequestDTO request) {
            return response;
        }

        @Override
        public AuthResponseDTO getById(Integer userId) {
            requestedUserId = userId;
            return response;
        }
    }
}
