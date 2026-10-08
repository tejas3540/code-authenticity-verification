package com.vericode.vericode_backend.Service;

import com.vericode.vericode_backend.dto.*;
import com.vericode.vericode_backend.model.Role;
import com.vericode.vericode_backend.model.UserAccount;
import com.vericode.vericode_backend.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthResponseDTO registerStudent(StudentRegisterRequestDTO request) {
        ensureEmailAvailable(request.getEmail());

        UserAccount user = new UserAccount();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.STUDENT);
        user.setCollege(request.getCollege());
        user.setCurrentStudyField(request.getCurrentStudyField());

        return toResponse(userAccountRepository.save(user));
    }

    public AuthResponseDTO registerRecruiter(RecruiterRegisterRequestDTO request) {
        ensureEmailAvailable(request.getEmail());

        UserAccount user = new UserAccount();
        user.setName(request.getRecruiterName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.RECRUITER);
        user.setOrganizationName(request.getOrganizationName());

        return toResponse(userAccountRepository.save(user));
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        UserAccount user = userAccountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (user.getRole() != request.getRole()
                || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email, password, or role");
        }

        return toResponse(user);
    }

    public AuthResponseDTO getById(Integer id) {
        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User account not found"));
        return toResponse(user);
    }

    private void ensureEmailAvailable(String email) {
        if (userAccountRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email is already registered");
        }
    }

    private AuthResponseDTO toResponse(UserAccount user) {
        AuthResponseDTO response = new AuthResponseDTO();
        response.setUserId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setCollege(user.getCollege());
        response.setCurrentStudyField(user.getCurrentStudyField());
        response.setOrganizationName(user.getOrganizationName());
        return response;
    }
}
