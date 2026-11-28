package com.vericode.vericode_backend.Service;

import com.vericode.vericode_backend.dto.AssessmentRequestDTO;
import com.vericode.vericode_backend.dto.AssessmentResponseDTO;
import com.vericode.vericode_backend.exception.*;
import com.vericode.vericode_backend.model.Assessment;
import com.vericode.vericode_backend.model.Language;
import com.vericode.vericode_backend.model.Question;
import com.vericode.vericode_backend.model.Role;
import com.vericode.vericode_backend.model.UserAccount;
import com.vericode.vericode_backend.repository.AssessmentRepository;
import com.vericode.vericode_backend.repository.QuestionRepository;
import com.vericode.vericode_backend.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssessmentServiceTest {

    @Mock
    private AssessmentRepository assessmentRepository;

    @Mock
    private UserAccountRepository userRepository;

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private AssessmentService assessmentService;

    @Test
    void createTrimsTitleAndDeduplicatesTrimmedStudentEmails() {
        UserAccount recruiter = user(7, "Recruiter", "recruiter@example.com", Role.RECRUITER);
        UserAccount student = user(42, "Student", "student@example.com", Role.STUDENT);
        List<Question> questions = List.of(
                question(3, "Arrays"),
                question(5, "Strings"));
        AssessmentRequestDTO request = request(
                " Java Screening ",
                List.of(3, 5),
                List.of(" student@example.com ", "student@example.com"));

        when(userRepository.findById(7)).thenReturn(Optional.of(recruiter));
        when(questionRepository.findAllById(List.of(3, 5))).thenReturn(questions);
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));
        when(assessmentRepository.save(any(Assessment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AssessmentResponseDTO response = assessmentService.create(7, request);

        assertEquals("Java Screening", response.getTitle());
        assertEquals("Recruiter", response.getRecruiterName());
        assertEquals(List.of(3, 5),
                response.getQuestions().stream().map(question -> question.getId()).toList());
        assertEquals(List.of("student@example.com"), response.getStudentEmails());

        ArgumentCaptor<Assessment> savedAssessment = ArgumentCaptor.forClass(Assessment.class);
        verify(assessmentRepository).save(savedAssessment.capture());
        assertEquals("Java Screening", savedAssessment.getValue().getTitle());
        assertEquals(recruiter, savedAssessment.getValue().getRecruiter());
        assertEquals(questions, savedAssessment.getValue().getQuestions());
        assertEquals(List.of(student), savedAssessment.getValue().getStudents());
        verify(userRepository, times(1)).findByEmail("student@example.com");
    }

    @Test
    void createRejectsUnknownRecruiterAccount() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> assessmentService.create(99,
                        request("Java Screening", List.of(3), List.of("student@example.com"))));

        assertEquals("Recruiter account not found", exception.getMessage());
        verify(questionRepository, never()).findAllById(any());
        verify(assessmentRepository, never()).save(any(Assessment.class));
    }

    @Test
    void createRejectsAccountThatIsNotARecruiter() {
        when(userRepository.findById(42))
                .thenReturn(Optional.of(user(42, "Student", "student@example.com", Role.STUDENT)));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> assessmentService.create(42,
                        request("Java Screening", List.of(3), List.of("another@example.com"))));

        assertEquals("Only recruiters can create assessments", exception.getMessage());
        verify(questionRepository, never()).findAllById(any());
        verify(assessmentRepository, never()).save(any(Assessment.class));
    }

    @Test
    void createRejectsWhenAnySelectedQuestionDoesNotExist() {
        when(userRepository.findById(7))
                .thenReturn(Optional.of(user(7, "Recruiter", "recruiter@example.com", Role.RECRUITER)));
        when(questionRepository.findAllById(List.of(3, 5))).thenReturn(List.of(question(3, "Arrays")));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> assessmentService.create(7,
                        request("Java Screening", List.of(3, 5), List.of("student@example.com"))));

        assertEquals("One or more selected questions do not exist", exception.getMessage());
        verify(userRepository, never()).findByEmail(anyString());
        verify(assessmentRepository, never()).save(any(Assessment.class));
    }

    @Test
    void createRejectsEmailWithoutAnAccount() {
        when(userRepository.findById(7))
                .thenReturn(Optional.of(user(7, "Recruiter", "recruiter@example.com", Role.RECRUITER)));
        when(questionRepository.findAllById(List.of(3))).thenReturn(List.of(question(3, "Arrays")));
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> assessmentService.create(7,
                        request("Java Screening", List.of(3), List.of("missing@example.com"))));

        assertEquals("No account found for student email: missing@example.com", exception.getMessage());
        verify(assessmentRepository, never()).save(any(Assessment.class));
    }

    @Test
    void createRejectsAssignmentToAnAccountThatIsNotAStudent() {
        when(userRepository.findById(7))
                .thenReturn(Optional.of(user(7, "Recruiter", "recruiter@example.com", Role.RECRUITER)));
        when(questionRepository.findAllById(List.of(3))).thenReturn(List.of(question(3, "Arrays")));
        when(userRepository.findByEmail("other-recruiter@example.com"))
                .thenReturn(Optional.of(user(
                        8, "Another recruiter", "other-recruiter@example.com", Role.RECRUITER)));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> assessmentService.create(7,
                        request("Java Screening", List.of(3), List.of("other-recruiter@example.com"))));

        assertEquals("Assignments can only be made to student accounts", exception.getMessage());
        verify(assessmentRepository, never()).save(any(Assessment.class));
    }

    @Test
    void getForStudentUsesAssignedAssessmentsAndHidesStudentEmails() {
        UserAccount recruiter = user(7, "Recruiter", "recruiter@example.com", Role.RECRUITER);
        UserAccount firstStudent = user(42, "Student", "student@example.com", Role.STUDENT);
        UserAccount secondStudent = user(43, "Another student", "another@example.com", Role.STUDENT);
        Assessment assessment = assessment(
                recruiter,
                List.of(question(3, "Arrays")),
                List.of(firstStudent, secondStudent));
        when(assessmentRepository.findDistinctByStudents_Id(42)).thenReturn(List.of(assessment));

        List<AssessmentResponseDTO> responses = assessmentService.getForUser(42, "STUDENT");

        assertEquals(1, responses.size());
        assertEquals("Java Screening", responses.get(0).getTitle());
        assertEquals(List.of(3),
                responses.get(0).getQuestions().stream().map(question -> question.getId()).toList());
        assertEquals(List.of(), responses.get(0).getStudentEmails());
        verify(assessmentRepository).findDistinctByStudents_Id(42);
        verify(assessmentRepository, never()).findAll();
        verify(assessmentRepository, never()).findByRecruiter_Id(any());
    }

    @Test
    void getForRecruiterReturnsOnlyTheirAssessmentsAndIncludesStudentEmails() {
        UserAccount recruiter = user(7, "Recruiter", "recruiter@example.com", Role.RECRUITER);
        UserAccount student = user(42, "Student", "student@example.com", Role.STUDENT);
        Assessment assessment = assessment(
                recruiter,
                List.of(question(3, "Arrays")),
                List.of(student));
        when(assessmentRepository.findByRecruiter_Id(7)).thenReturn(List.of(assessment));

        List<AssessmentResponseDTO> responses = assessmentService.getForUser(7, "RECRUITER");

        assertEquals(1, responses.size());
        assertEquals(List.of("student@example.com"), responses.get(0).getStudentEmails());
        verify(assessmentRepository).findByRecruiter_Id(7);
        verify(assessmentRepository, never()).findAll();
        verify(assessmentRepository, never()).findDistinctByStudents_Id(any());
    }

    @Test
    void getForAdminReturnsAllAssessments() {
        UserAccount recruiter = user(7, "Recruiter", "recruiter@example.com", Role.RECRUITER);
        UserAccount student = user(42, "Student", "student@example.com", Role.STUDENT);
        Assessment assessment = assessment(
                recruiter,
                List.of(question(3, "Arrays")),
                List.of(student));
        when(assessmentRepository.findAll()).thenReturn(List.of(assessment));

        List<AssessmentResponseDTO> responses = assessmentService.getForUser(1, "ADMIN");

        assertEquals(1, responses.size());
        assertEquals(List.of("student@example.com"), responses.get(0).getStudentEmails());
        verify(assessmentRepository).findAll();
    }

    private AssessmentRequestDTO request(
            String title,
            List<Integer> questionIds,
            List<String> studentEmails) {
        AssessmentRequestDTO request = new AssessmentRequestDTO();
        request.setTitle(title);
        request.setDescription("Core Java assessment");
        request.setDurationMinutes(60);
        request.setQuestionIds(questionIds);
        request.setStudentEmails(studentEmails);
        return request;
    }

    private UserAccount user(Integer id, String name, String email, Role role) {
        UserAccount user = new UserAccount();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPassword("encoded-password");
        user.setRole(role);
        return user;
    }

    private Question question(Integer id, String title) {
        Question question = new Question();
        question.setId(id);
        question.setTitle(title);
        question.setDescription(title + " problem");
        question.setInputDescription("Input values");
        question.setOutputDescription("Expected result");
        question.setConstraints("1 <= n <= 1000");
        question.setTimeLimitSeconds(2);
        question.setLanguage(Language.JAVA);
        return question;
    }

    private Assessment assessment(
            UserAccount recruiter,
            List<Question> questions,
            List<UserAccount> students) {
        Assessment assessment = new Assessment();
        assessment.setTitle("Java Screening");
        assessment.setDescription("Core Java assessment");
        assessment.setDurationMinutes(60);
        assessment.setRecruiter(recruiter);
        assessment.setQuestions(questions);
        assessment.setStudents(students);
        return assessment;
    }
}
