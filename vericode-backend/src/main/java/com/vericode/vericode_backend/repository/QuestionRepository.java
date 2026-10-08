package com.vericode.vericode_backend.repository;

import com.vericode.vericode_backend.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
}
