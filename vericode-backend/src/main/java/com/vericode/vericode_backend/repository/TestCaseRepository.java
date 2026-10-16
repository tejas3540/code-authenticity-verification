package com.vericode.vericode_backend.repository;

import com.vericode.vericode_backend.model.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TestCaseRepository extends JpaRepository<TestCase, Integer> {
    List<TestCase> findByQuestion_IdOrderByIdAsc(Integer questionId);
}
