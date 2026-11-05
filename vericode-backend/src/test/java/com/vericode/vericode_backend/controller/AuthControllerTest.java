package com.vericode.vericode_backend.controller;

import com.vericode.vericode_backend.Service.AuthService;
import com.vericode.vericode_backend.dto.AuthResponseDTO;
import com.vericode.vericode_backend.dto.RecruiterRegisterRequestDTO;
import com.vericode.vericode_backend.dto.StudentRegisterRequestDTO;
import com.vericode.vericode_backend.model.Role;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class AuthControllerTest {

    @Test
    void registeringStudentStartsAuthenticatedSessionAndRotatesItsId() {
        AuthController controller = new AuthController(new StubAuthService());
        MockHttpSession session = new MockHttpSession();
        String originalSessionId = session.getId();
        HttpServletRequest request = requestWithSession(session);

        controller.registerStudent(new StudentRegisterRequestDTO(), request);

        assertEquals(17, session.getAttribute("userId"));
        assertEquals("STUDENT", session.getAttribute("role"));
        assertNotEquals(originalSessionId, session.getId(),
                "Student registration must rotate an existing session ID");
    }

    @Test
    void registeringRecruiterStartsAuthenticatedSessionAndRotatesItsId() {
        AuthController controller = new AuthController(new StubAuthService());
        MockHttpSession session = new MockHttpSession();
        String originalSessionId = session.getId();
        HttpServletRequest request = requestWithSession(session);

        controller.registerRecruiter(new RecruiterRegisterRequestDTO(), request);

        assertEquals(23, session.getAttribute("userId"));
        assertEquals("RECRUITER", session.getAttribute("role"));
        assertNotEquals(originalSessionId, session.getId(),
                "Recruiter registration must rotate an existing session ID");
    }

    private HttpServletRequest requestWithSession(MockHttpSession session) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);
        return request;
    }

    private static class StubAuthService extends AuthService {
        @Override
        public AuthResponseDTO registerStudent(StudentRegisterRequestDTO request) {
            return response(17, Role.STUDENT);
        }

        @Override
        public AuthResponseDTO registerRecruiter(RecruiterRegisterRequestDTO request) {
            return response(23, Role.RECRUITER);
        }

        private AuthResponseDTO response(int userId, Role role) {
            AuthResponseDTO response = new AuthResponseDTO();
            response.setUserId(userId);
            response.setRole(role);
            return response;
        }
    }
}
