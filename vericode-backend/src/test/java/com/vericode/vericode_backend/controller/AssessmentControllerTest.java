package com.vericode.vericode_backend.controller;

import tools.jackson.databind.json.JsonMapper;
import com.vericode.vericode_backend.Service.AssessmentService;
import com.vericode.vericode_backend.dto.AssessmentRequestDTO;
import com.vericode.vericode_backend.dto.AssessmentResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssessmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class AssessmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private AssessmentService assessmentService;

    @Test
    void recruiterCanCreateAssessment() throws Exception {
        when(assessmentService.create(eq(7), any(AssessmentRequestDTO.class)))
                .thenReturn(response(12, "Java Screening"));

        mockMvc.perform(post("/Assessments")
                        .sessionAttr("userId", 7)
                        .sessionAttr("role", "RECRUITER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.title").value("Java Screening"))
                .andExpect(jsonPath("$.durationMinutes").value(60));

        verify(assessmentService).create(eq(7), any(AssessmentRequestDTO.class));
    }

    @Test
    void createWithoutSessionReturnsUnauthorizedBeforeCallingService() throws Exception {
        mockMvc.perform(post("/Assessments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(assessmentService);
    }

    @Test
    void studentCannotCreateAssessment() throws Exception {
        mockMvc.perform(post("/Assessments")
                        .sessionAttr("userId", 42)
                        .sessionAttr("role", "STUDENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden());

        verifyNoInteractions(assessmentService);
    }

    @Test
    void invalidAssessmentPayloadReturnsBadRequestBeforeCallingService() throws Exception {
        AssessmentRequestDTO invalidRequest = new AssessmentRequestDTO();
        invalidRequest.setTitle(" ");
        invalidRequest.setDurationMinutes(0);
        invalidRequest.setQuestionIds(List.of());
        invalidRequest.setStudentEmails(List.of());

        mockMvc.perform(post("/Assessments")
                        .sessionAttr("userId", 7)
                        .sessionAttr("role", "RECRUITER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(assessmentService);
    }

    @Test
    void studentCanRetrieveAssessmentsForTheirAccount() throws Exception {
        when(assessmentService.getForUser(42, "STUDENT"))
                .thenReturn(List.of(response(12, "Java Screening")));

        mockMvc.perform(get("/Assessments")
                        .sessionAttr("userId", 42)
                        .sessionAttr("role", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(12))
                .andExpect(jsonPath("$[0].title").value("Java Screening"));

        verify(assessmentService).getForUser(42, "STUDENT");
    }

    @Test
    void recruiterCanRetrieveTheirAssessmentsFromMyEndpoint() throws Exception {
        when(assessmentService.getForUser(7, "RECRUITER"))
                .thenReturn(List.of(response(12, "Java Screening")));

        mockMvc.perform(get("/Assessments/my")
                        .sessionAttr("userId", 7)
                        .sessionAttr("role", "RECRUITER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Java Screening"));

        verify(assessmentService).getForUser(7, "RECRUITER");
    }

    @Test
    void listAssessmentsWithoutSessionReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/Assessments"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/Assessments/my"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(assessmentService);
    }

    private AssessmentRequestDTO validRequest() {
        AssessmentRequestDTO request = new AssessmentRequestDTO();
        request.setTitle("Java Screening");
        request.setDescription("Core Java assessment");
        request.setDurationMinutes(60);
        request.setQuestionIds(List.of(3, 5));
        request.setStudentEmails(List.of("student@example.com"));
        return request;
    }

    private AssessmentResponseDTO response(Integer id, String title) {
        AssessmentResponseDTO response = new AssessmentResponseDTO();
        response.setId(id);
        response.setTitle(title);
        response.setDescription("Core Java assessment");
        response.setDurationMinutes(60);
        response.setRecruiterName("Recruiter");
        response.setQuestions(List.of());
        response.setStudentEmails(List.of());
        return response;
    }
}
