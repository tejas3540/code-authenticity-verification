package com.vericode.vericode_backend.controller;

import com.vericode.vericode_backend.Service.AuthService;
import com.vericode.vericode_backend.dto.AuthResponseDTO;
import com.vericode.vericode_backend.dto.RecruiterRegisterRequestDTO;
import com.vericode.vericode_backend.dto.StudentRegisterRequestDTO;
import com.vericode.vericode_backend.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthControllerTest {

    @Test
    void registeringStudentStartsAuthenticatedSession() {
        AuthController controller = new AuthController(new StubAuthService());
        MockHttpSession session = new MockHttpSession();

        controller.registerStudent(new StudentRegisterRequestDTO(), session);

        assertEquals(17, session.getAttribute("userId"));
        assertEquals("STUDENT", session.getAttribute("role"));
    }

    @Test
    void registeringRecruiterStartsAuthenticatedSession() {
        AuthController controller = new AuthController(new StubAuthService());
        MockHttpSession session = new MockHttpSession();

        controller.registerRecruiter(new RecruiterRegisterRequestDTO(), session);

        assertEquals(23, session.getAttribute("userId"));
        assertEquals("RECRUITER", session.getAttribute("role"));
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
