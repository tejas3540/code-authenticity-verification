package com.vericode.vericode_backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import com.vericode.vericode_backend.Service.AuthService;
import com.vericode.vericode_backend.dto.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Student and recruiter registration, login, current session and logout.")
@RestController
@RequestMapping("/Auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Register a student account and start a session")
    @PostMapping("/register/student")
    public ResponseEntity<AuthResponseDTO> registerStudent(
            @Valid @RequestBody StudentRegisterRequestDTO request,
            HttpSession session) {
        AuthResponseDTO response = authService.registerStudent(request);
        establishSession(session, response);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(summary = "Register a recruiter account and start a session")
    @PostMapping("/register/recruiter")
    public ResponseEntity<AuthResponseDTO> registerRecruiter(
            @Valid @RequestBody RecruiterRegisterRequestDTO request,
            HttpSession session) {
        AuthResponseDTO response = authService.registerRecruiter(request);
        establishSession(session, response);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(summary = "Authenticate an existing student or recruiter")
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

    @Operation(summary = "Get the currently authenticated account")
    @SecurityRequirement(name = "SessionCookie")
    @GetMapping("/me")
    public ResponseEntity<AuthResponseDTO> me(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(authService.getById(userId));
    }

    @Operation(summary = "End the current authenticated session")
    @SecurityRequirement(name = "SessionCookie")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }
}
