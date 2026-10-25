package com.vericode.vericode_backend.controller;

import com.vericode.vericode_backend.Service.AuthService;
import com.vericode.vericode_backend.dto.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/Auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/student")
    public ResponseEntity<AuthResponseDTO> registerStudent(
            @Valid @RequestBody StudentRegisterRequestDTO request,
            HttpSession session) {
        AuthResponseDTO response = authService.registerStudent(request);
        establishSession(session, response);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/register/recruiter")
    public ResponseEntity<AuthResponseDTO> registerRecruiter(
            @Valid @RequestBody RecruiterRegisterRequestDTO request,
            HttpSession session) {
        AuthResponseDTO response = authService.registerRecruiter(request);
        establishSession(session, response);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request,
            HttpSession session) {
        AuthResponseDTO response = authService.login(request);
        establishSession(session, response);
        return ResponseEntity.ok(response);
    }

    private void establishSession(HttpSession session, AuthResponseDTO user) {
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("role", user.getRole().name());
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponseDTO> me(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(authService.getById(userId));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }
}
