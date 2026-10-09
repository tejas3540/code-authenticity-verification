package com.vericode.vericode_backend.Service;

import com.vericode.vericode_backend.dto.AssessmentRequestDTO;
import com.vericode.vericode_backend.dto.AssessmentResponseDTO;
import com.vericode.vericode_backend.dto.QuestionResponseDTO;
import com.vericode.vericode_backend.model.Assessment;
import com.vericode.vericode_backend.model.Question;
import com.vericode.vericode_backend.model.Role;
import com.vericode.vericode_backend.model.UserAccount;
import com.vericode.vericode_backend.repository.AssessmentRepository;
import com.vericode.vericode_backend.repository.QuestionRepository;
import com.vericode.vericode_backend.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssessmentService {
    private final AssessmentRepository assessmentRepository;
    private final UserAccountRepository userRepository;
    private final QuestionRepository questionRepository;

    public AssessmentService(AssessmentRepository assessmentRepository,
                             UserAccountRepository userRepository,
                             QuestionRepository questionRepository) {
        this.assessmentRepository = assessmentRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
    }

    @Transactional
    public AssessmentResponseDTO create(Integer recruiterId, AssessmentRequestDTO request) {
        UserAccount recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new IllegalArgumentException("Recruiter account not found"));
        if (recruiter.getRole() != Role.RECRUITER) {
            throw new IllegalArgumentException("Only recruiters can create assessments");
        }

        List<Question> questions = questionRepository.findAllById(request.getQuestionIds());
        if (questions.size() != request.getQuestionIds().stream().distinct().count()) {
            throw new IllegalArgumentException("One or more selected questions do not exist");
        }

        List<UserAccount> students = request.getStudentEmails().stream()
                .map(String::trim)
                .distinct()
                .map(email -> userRepository.findByEmail(email)
                        .orElseThrow(() -> new IllegalArgumentException("No account found for student email: " + email)))
                .toList();
        if (students.stream().anyMatch(student -> student.getRole() != Role.STUDENT)) {
            throw new IllegalArgumentException("Assignments can only be made to student accounts");
        }

        Assessment assessment = new Assessment();
        assessment.setTitle(request.getTitle().trim());
        assessment.setDescription(request.getDescription());
        assessment.setDurationMinutes(request.getDurationMinutes());
        assessment.setRecruiter(recruiter);
        assessment.setQuestions(questions);
        assessment.setStudents(students);
        return toResponse(assessmentRepository.save(assessment));
    }

    @Transactional(readOnly = true)
    public List<AssessmentResponseDTO> getForUser(Integer userId, String role) {
        List<Assessment> assessments;
        if ("RECRUITER".equals(role)) {
            assessments = assessmentRepository.findByRecruiter_Id(userId);
        } else if ("STUDENT".equals(role)) {
            assessments = assessmentRepository.findDistinctByStudents_Id(userId);
        } else {
            assessments = assessmentRepository.findAll();
        }
        return assessments.stream().map(this::toResponse).toList();
    }

    private AssessmentResponseDTO toResponse(Assessment assessment) {
        AssessmentResponseDTO response = new AssessmentResponseDTO();
        response.setId(assessment.getId());
        response.setTitle(assessment.getTitle());
        response.setDescription(assessment.getDescription());
        response.setDurationMinutes(assessment.getDurationMinutes());
        response.setRecruiterName(assessment.getRecruiter().getName());
        response.setQuestions(assessment.getQuestions().stream().map(question -> {
            QuestionResponseDTO dto = new QuestionResponseDTO();
            dto.setId(question.getId());
            dto.setTitle(question.getTitle());
            dto.setDescription(question.getDescription());
            dto.setInputDescription(question.getInputDescription());
            dto.setOutputDescription(question.getOutputDescription());
            dto.setConstraints(question.getConstraints());
            dto.setTimeLimitSeconds(question.getTimeLimitSeconds());
            dto.setLanguage(question.getLanguage());
            return dto;
        }).toList());
        response.setStudentEmails(assessment.getStudents().stream().map(UserAccount::getEmail).toList());
        return response;
    }
}