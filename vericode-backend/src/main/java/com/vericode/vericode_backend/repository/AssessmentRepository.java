package com.vericode.vericode_backend.repository;

import com.vericode.vericode_backend.model.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AssessmentRepository extends JpaRepository<Assessment, Integer> {
    List<Assessment> findByRecruiter_Id(Integer recruiterId);
    List<Assessment> findDistinctByStudents_Id(Integer studentId);

    @Query("select case when count(a) > 0 then true else false end " +
            "from Assessment a join a.students student " +
            "where a.id = :assessmentId and student.id = :studentId")
    boolean isStudentAssignedToAssessment(@Param("assessmentId") Integer assessmentId,
                                          @Param("studentId") Integer studentId);

    @Query("select case when count(a) > 0 then true else false end " +
            "from Assessment a join a.students student join a.questions question " +
            "where student.id = :studentId and question.id = :questionId")
    boolean isQuestionAssignedToStudent(@Param("studentId") Integer studentId,
                                        @Param("questionId") Integer questionId);
}
