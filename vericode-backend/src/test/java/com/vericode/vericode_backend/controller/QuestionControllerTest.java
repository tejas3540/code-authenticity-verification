package com.vericode.vericode_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vericode.vericode_backend.Service.QuestionService;
import com.vericode.vericode_backend.dto.QuestionRequestDTO;
import com.vericode.vericode_backend.dto.QuestionResponseDTO;
import com.vericode.vericode_backend.exception.QuestionNotFoundException;
import com.vericode.vericode_backend.model.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuestionController.class)
@AutoConfigureMockMvc(addFilters = false)
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QuestionService questionService;

    private MockHttpSession recruiterSession;

    @BeforeEach
    void setUp() {
        recruiterSession = new MockHttpSession();
        recruiterSession.setAttribute("userId", 1);
        recruiterSession.setAttribute("role", "RECRUITER");
    }

    @Test
    void createQuestionReturnsCreatedAndResponseBody() throws Exception {
        when(questionService.save(any(QuestionRequestDTO.class)))
                .thenReturn(response(12, "Arrays", "Array problem"));

        mockMvc.perform(post("/Questions")
                        .session(recruiterSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("Arrays", "Array problem"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.title").value("Arrays"))
                .andExpect(jsonPath("$.description").value("Array problem"))
                .andExpect(jsonPath("$.language").value("JAVA"));
    }

    @Test
    void getAllQuestionsReturnsOkAndQuestionList() throws Exception {
        when(questionService.getAllQuestions()).thenReturn(List.of(
                response(1, "Arrays", "Array problem"),
                response(2, "Strings", "String problem")));

        mockMvc.perform(get("/Questions").session(recruiterSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].title").value("Strings"));
    }

    @Test
    void getQuestionByIdReturnsOkAndQuestionBody() throws Exception {
        when(questionService.getQuestionById(7))
                .thenReturn(response(7, "Trees", "Tree traversal"));

        mockMvc.perform(get("/Questions/7").session(recruiterSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.title").value("Trees"))
                .andExpect(jsonPath("$.description").value("Tree traversal"));
    }

    @Test
    void updateQuestionReturnsOkAndUpdatedQuestion() throws Exception {
        when(questionService.updateQuestion(eq(9), any(QuestionRequestDTO.class)))
                .thenReturn(response(9, "Updated title", "Updated description"));

        mockMvc.perform(put("/Questions/9")
                        .session(recruiterSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                request("Updated title", "Updated description"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void deleteQuestionReturnsOkAndDeletedQuestionBody() throws Exception {
        when(questionService.deleteQuestion(15))
                .thenReturn(response(15, "Stacks", "Stack problem"));

        mockMvc.perform(delete("/Questions/15").session(recruiterSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(15))
                .andExpect(jsonPath("$.title").value("Stacks"));

        verify(questionService).deleteQuestion(15);
    }

    @Test
    void createQuestionWithInvalidPayloadReturnsBadRequest() throws Exception {
        QuestionRequestDTO invalidRequest = request("", "Array problem");

        mockMvc.perform(post("/Questions")
                        .session(recruiterSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Enter question title"));
    }

    @Test
    void updateQuestionWithInvalidPayloadReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/Questions/9")
                        .session(recruiterSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("", "Updated description"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Enter question title"));
    }

    @Test
    void getUnknownQuestionReturnsNotFound() throws Exception {
        when(questionService.getQuestionById(404))
                .thenThrow(new QuestionNotFoundException("Question not found with id 404"));

        mockMvc.perform(get("/Questions/404").session(recruiterSession))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Question not found with id 404"));
    }

    @Test
    void updateUnknownQuestionReturnsNotFound() throws Exception {
        when(questionService.updateQuestion(eq(404), any(QuestionRequestDTO.class)))
                .thenThrow(new QuestionNotFoundException("Question not found with id 404"));

        mockMvc.perform(put("/Questions/404")
                        .session(recruiterSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("Arrays", "Array problem"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Question not found with id 404"));
    }

    @Test
    void deleteUnknownQuestionReturnsNotFound() throws Exception {
        when(questionService.deleteQuestion(404))
                .thenThrow(new QuestionNotFoundException("Question not found with id 404"));

        mockMvc.perform(delete("/Questions/404").session(recruiterSession))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Question not found with id 404"));
    }

    @Test
    void unexpectedServiceExceptionReturnsInternalServerError() throws Exception {
        when(questionService.getAllQuestions()).thenThrow(new RuntimeException("database unavailable"));

        mockMvc.perform(get("/Questions").session(recruiterSession))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected server error occurred"));
    }

    @Test
    void requestWithoutSessionReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/Questions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestFromNonRecruiterReturnsForbidden() throws Exception {
        MockHttpSession candidateSession = new MockHttpSession();
        candidateSession.setAttribute("userId", 2);
        candidateSession.setAttribute("role", "CANDIDATE");

        mockMvc.perform(get("/Questions").session(candidateSession))
                .andExpect(status().isForbidden());
    }

    private QuestionRequestDTO request(String title, String description) {
        QuestionRequestDTO request = new QuestionRequestDTO();
        request.setTitle(title);
        request.setDescription(description);
        request.setInputDescription("Input values");
        request.setOutputDescription("Expected result");
        request.setConstraints("1 <= n <= 1000");
        request.setTimeLimitSeconds(2);
        request.setLanguage(Language.JAVA);
        return request;
    }

    private QuestionResponseDTO response(Integer id, String title, String description) {
        QuestionResponseDTO response = new QuestionResponseDTO();
        response.setId(id);
        response.setTitle(title);
        response.setDescription(description);
        response.setInputDescription("Input values");
        response.setOutputDescription("Expected result");
        response.setConstraints("1 <= n <= 1000");
        response.setTimeLimitSeconds(2);
        response.setLanguage(Language.JAVA);
        return response;
    }
}
