package com.vericode.vericode_backend.repository;

import com.vericode.vericode_backend.model.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AssessmentRepository extends JpaRepository<Assessment, Integer> {
    List<Assessment> findByRecruiter_Id(Integer recruiterId);
    List<Assessment> findDistinctByStudents_Id(Integer studentId);
}