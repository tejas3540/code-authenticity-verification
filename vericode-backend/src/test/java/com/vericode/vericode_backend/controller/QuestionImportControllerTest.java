package com.vericode.vericode_backend.controller;

import com.vericode.vericode_backend.Service.QuestionImportService;
import com.vericode.vericode_backend.dto.QuestionImportRequestDTO;
import com.vericode.vericode_backend.dto.QuestionImportResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuestionImportControllerTest {

    @Test
    void previewRejectsUnauthenticatedRequests() {
        StubQuestionImportService service = new StubQuestionImportService();
        QuestionImportController controller = new QuestionImportController(service);

        var response = controller.preview(request(), new MockHttpSession());

        assertEquals(401, response.getStatusCode().value());
        assertEquals(0, service.calls);
    }

    @Test
    void previewRejectsStudentSessions() {
        StubQuestionImportService service = new StubQuestionImportService();
        QuestionImportController controller = new QuestionImportController(service);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 7);
        session.setAttribute("role", "STUDENT");

        var response = controller.preview(request(), session);

        assertEquals(403, response.getStatusCode().value());
        assertEquals(0, service.calls);
    }

    private QuestionImportRequestDTO request() {
        QuestionImportRequestDTO request = new QuestionImportRequestDTO();
        request.setUrl("https://leetcode.com/problems/two-sum/");
        return request;
    }

    private static class StubQuestionImportService extends QuestionImportService {
        private int calls;

        @Override
        public QuestionImportResponseDTO preview(String url) {
            calls++;
            return new QuestionImportResponseDTO();
        }
    }
}
