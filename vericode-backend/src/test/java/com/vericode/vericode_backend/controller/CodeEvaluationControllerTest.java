package com.vericode.vericode_backend.controller;

import com.vericode.vericode_backend.Service.CodeExecutionService;
import com.vericode.vericode_backend.dto.CodeExecutionRequestDTO;
import com.vericode.vericode_backend.dto.CodeExecutionResponseDTO;
import com.vericode.vericode_backend.exception.GlobalExceptionHandler;
import com.vericode.vericode_backend.model.TestCase;
import com.vericode.vericode_backend.repository.AssessmentRepository;
import com.vericode.vericode_backend.repository.TestCaseRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CodeEvaluationControllerTest {

    private CodeExecutionService codeExecutionService;
    private AssessmentRepository assessmentRepository;
    private TestCaseRepository testCaseRepository;
    private LocalValidatorFactoryBean validator;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        codeExecutionService = mock(CodeExecutionService.class);
        assessmentRepository = mock(AssessmentRepository.class);
        testCaseRepository = mock(TestCaseRepository.class);
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        CodeExecutionController controller = new CodeExecutionController(
                codeExecutionService, testCaseRepository, assessmentRepository);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }

    @Test
    void evaluateRejectsRequestWithoutAuthenticatedSession() throws Exception {
        mockMvc.perform(post("/CodeExecution/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isUnauthorized());

        verify(assessmentRepository, never()).isQuestionAssignedToStudent(any(), any());
        verify(codeExecutionService, never()).execute(any(CodeExecutionRequestDTO.class));
    }

    @Test
    void evaluateRejectsAuthenticatedNonStudent() throws Exception {
        MockHttpSession session = studentSession("RECRUITER");

        mockMvc.perform(post("/CodeExecution/evaluate")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isForbidden());

        verify(assessmentRepository, never()).isQuestionAssignedToStudent(any(), any());
        verify(codeExecutionService, never()).execute(any(CodeExecutionRequestDTO.class));
    }

    @Test
    void evaluateRejectsQuestionNotAssignedToStudent() throws Exception {
        MockHttpSession session = studentSession("STUDENT");
        when(assessmentRepository.isQuestionAssignedToStudent(21, 7)).thenReturn(false);

        mockMvc.perform(post("/CodeExecution/evaluate")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(
                        "This question is not part of an assessment assigned to you."));

        verify(testCaseRepository, never()).findByQuestion_IdOrderByIdAsc(7);
        verify(codeExecutionService, never()).execute(any(CodeExecutionRequestDTO.class));
    }

    @Test
    void evaluateReturnsBadRequestWhenQuestionHasNoTestCases() throws Exception {
        MockHttpSession session = studentSession("STUDENT");
        when(assessmentRepository.isQuestionAssignedToStudent(21, 7)).thenReturn(true);
        when(testCaseRepository.findByQuestion_IdOrderByIdAsc(7)).thenReturn(List.of());

        mockMvc.perform(post("/CodeExecution/evaluate")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This question has no test cases."));

        verify(codeExecutionService, never()).execute(any(CodeExecutionRequestDTO.class));
    }

    @Test
    void evaluateReturnsScoreAndDoesNotExposeHiddenTestCaseOutput() throws Exception {
        MockHttpSession session = studentSession("STUDENT");
        TestCase visibleCase = testCase("2", "4", false, 5);
        TestCase hiddenCase = testCase("3", "9", true, 10);

        when(assessmentRepository.isQuestionAssignedToStudent(21, 7)).thenReturn(true);
        when(testCaseRepository.findByQuestion_IdOrderByIdAsc(7))
                .thenReturn(List.of(visibleCase, hiddenCase));
        when(codeExecutionService.execute(any(CodeExecutionRequestDTO.class)))
                .thenReturn(
                        new CodeExecutionResponseDTO(true, true, false, "4", "", 12),
                        new CodeExecutionResponseDTO(true, false, false, "SECRET_OUTPUT", "Wrong answer", 15));

        mockMvc.perform(post("/CodeExecution/evaluate")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passed").value(1))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.earnedPoints").value(5))
                .andExpect(jsonPath("$.totalPoints").value(15))
                .andExpect(jsonPath("$.results[0].hidden").value(false))
                .andExpect(jsonPath("$.results[0].output").value("4"))
                .andExpect(jsonPath("$.results[1].hidden").value(true))
                .andExpect(jsonPath("$.results[1].output").value(""))
                .andExpect(jsonPath("$.results[1].error").value("Wrong answer"));

        verify(codeExecutionService, times(2)).execute(any(CodeExecutionRequestDTO.class));
    }

    @Test
    void evaluateRejectsBlankSourceCodeBeforeCheckingAssignment() throws Exception {
        MockHttpSession session = studentSession("STUDENT");

        mockMvc.perform(post("/CodeExecution/evaluate")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "questionId": 7,
                                  "sourceCode": " "
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(assessmentRepository, never()).isQuestionAssignedToStudent(any(), any());
        verify(codeExecutionService, never()).execute(any(CodeExecutionRequestDTO.class));
    }

    private MockHttpSession studentSession(String role) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 21);
        session.setAttribute("role", role);
        return session;
    }

    private TestCase testCase(String input, String expectedOutput, boolean hidden, int points) {
        TestCase testCase = new TestCase();
        testCase.setInput(input);
        testCase.setExpectedOutput(expectedOutput);
        testCase.setHidden(hidden);
        testCase.setPoints(points);
        return testCase;
    }

    private String validRequest() {
        return """
                {
                  "questionId": 7,
                  "sourceCode": "public class Main { public static void main(String[] args) { System.out.println(4); } }"
                }
                """;
    }
}
