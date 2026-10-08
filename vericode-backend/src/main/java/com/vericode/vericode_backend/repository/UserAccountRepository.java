package com.vericode.vericode_backend.repository;

import com.vericode.vericode_backend.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Integer> {
    Optional<UserAccount> findByEmail(String email);
}
