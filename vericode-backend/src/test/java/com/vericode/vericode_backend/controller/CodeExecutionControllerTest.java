package com.vericode.vericode_backend.controller;

import com.vericode.vericode_backend.Service.CodeExecutionService;
import com.vericode.vericode_backend.dto.CodeExecutionRequestDTO;
import com.vericode.vericode_backend.dto.CodeExecutionResponseDTO;
import com.vericode.vericode_backend.exception.GlobalExceptionHandler;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CodeExecutionControllerTest {

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
    void runRejectsRequestWithoutAuthenticatedSession() throws Exception {
        mockMvc.perform(post("/CodeExecution/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isUnauthorized());

        verify(codeExecutionService, never()).execute(any(CodeExecutionRequestDTO.class));
    }

    @Test
    void runRejectsAuthenticatedNonStudent() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 12);
        session.setAttribute("role", "RECRUITER");

        mockMvc.perform(post("/CodeExecution/run")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isForbidden());

        verify(codeExecutionService, never()).execute(any(CodeExecutionRequestDTO.class));
    }

    @Test
    void runExecutesValidRequestForStudent() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 12);
        session.setAttribute("role", "STUDENT");
        when(codeExecutionService.execute(any(CodeExecutionRequestDTO.class)))
                .thenReturn(new CodeExecutionResponseDTO(true, true, false, "5", "", 18));

        mockMvc.perform(post("/CodeExecution/run")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compiled").value(true))
                .andExpect(jsonPath("$.passed").value(true))
                .andExpect(jsonPath("$.output").value("5"))
                .andExpect(jsonPath("$.executionTimeMs").value(18));

        verify(codeExecutionService).execute(any(CodeExecutionRequestDTO.class));
    }

    @Test
    void runRejectsBlankSourceCodeBeforeCallingService() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 12);
        session.setAttribute("role", "STUDENT");

        mockMvc.perform(post("/CodeExecution/run")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceCode": " ",
                                  "timeLimitSeconds": 5
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Source code is required"));

        verify(codeExecutionService, never()).execute(any(CodeExecutionRequestDTO.class));
    }

    private String validRequest() {
        return """
                {
                  "sourceCode": "public class Main { public static void main(String[] args) { System.out.println(2 + 3); } }",
                  "input": "",
                  "expectedOutput": "5",
                  "timeLimitSeconds": 5
                }
                """;
    }
}
