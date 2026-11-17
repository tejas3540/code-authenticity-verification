package com.vericode.vericode_backend.Service;

import com.vericode.vericode_backend.dto.QuestionRequestDTO;
import com.vericode.vericode_backend.dto.QuestionResponseDTO;
import com.vericode.vericode_backend.model.Language;
import com.vericode.vericode_backend.model.Question;
import com.vericode.vericode_backend.repository.QuestionRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private QuestionService questionService;

    @Test
    void saveMapsRequestFieldsAndReturnsSavedQuestion() {
        QuestionRequestDTO request = request("Arrays", "Array problem");
        when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> {
            Question question = invocation.getArgument(0);
            question.setId(12);
            return question;
        });

        QuestionResponseDTO response = questionService.save(request);

        assertQuestionResponse(response, 12, "Arrays", "Array problem");
        verify(questionRepository).save(any(Question.class));
    }

    @Test
    void getAllQuestionsMapsEveryEntityToResponse() {
        Question first = question(1, "Arrays", "Array problem");
        Question second = question(2, "Strings", "String problem");
        when(questionRepository.findAll()).thenReturn(List.of(first, second));

        List<QuestionResponseDTO> responses = questionService.getAllQuestions();

        assertEquals(2, responses.size());
        assertQuestionResponse(responses.get(0), 1, "Arrays", "Array problem");
        assertQuestionResponse(responses.get(1), 2, "Strings", "String problem");
        verify(questionRepository).findAll();
    }

    @Test
    void getQuestionByIdReturnsMappedQuestion() {
        Question existing = question(7, "Trees", "Tree traversal");
        when(questionRepository.findById(7)).thenReturn(Optional.of(existing));

        QuestionResponseDTO response = questionService.getQuestionById(7);

        assertQuestionResponse(response, 7, "Trees", "Tree traversal");
        verify(questionRepository).findById(7);
    }

    @Test
    void getQuestionByIdThrowsWhenQuestionDoesNotExist() {
        when(questionRepository.findById(404)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> questionService.getQuestionById(404));

        assertEquals("Question not found with id 404", exception.getMessage());
    }

    @Test
    void updateQuestionChangesFieldsAndPreservesId() {
        Question existing = question(9, "Old title", "Old description");
        QuestionRequestDTO request = request("Updated title", "Updated description");
        when(questionRepository.findById(9)).thenReturn(Optional.of(existing));
        when(questionRepository.save(any(Question.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        QuestionResponseDTO response = questionService.updateQuestion(9, request);

        assertQuestionResponse(response, 9, "Updated title", "Updated description");

        ArgumentCaptor<Question> savedQuestion = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).save(savedQuestion.capture());
        assertEquals(9, savedQuestion.getValue().getId());
        assertEquals(Language.JAVA, savedQuestion.getValue().getLanguage());
    }

    @Test
    void updateQuestionDoesNotSaveWhenQuestionDoesNotExist() {
        when(questionRepository.findById(404)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> questionService.updateQuestion(404, request("Title", "Description")));

        verify(questionRepository, never()).save(any(Question.class));
    }

    @Test
    void deleteQuestionDeletesEntityAndReturnsItsDetails() {
        Question existing = question(15, "Stacks", "Stack problem");
        when(questionRepository.findById(15)).thenReturn(Optional.of(existing));

        QuestionResponseDTO response = questionService.deleteQuestion(15);

        assertQuestionResponse(response, 15, "Stacks", "Stack problem");
        verify(questionRepository).delete(existing);
    }

    @Test
    void deleteQuestionDoesNotDeleteWhenQuestionDoesNotExist() {
        when(questionRepository.findById(404)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> questionService.deleteQuestion(404));

        verify(questionRepository, never()).delete(any(Question.class));
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

    private Question question(Integer id, String title, String description) {
        Question question = new Question();
        question.setId(id);
        question.setTitle(title);
        question.setDescription(description);
        question.setInputDescription("Input values");
        question.setOutputDescription("Expected result");
        question.setConstraints("1 <= n <= 1000");
        question.setTimeLimitSeconds(2);
        question.setLanguage(Language.JAVA);
        return question;
    }

    private void assertQuestionResponse(
            QuestionResponseDTO response,
            Integer id,
            String title,
            String description) {
        assertEquals(id, response.getId());
        assertEquals(title, response.getTitle());
        assertEquals(description, response.getDescription());
        assertEquals("Input values", response.getInputDescription());
        assertEquals("Expected result", response.getOutputDescription());
        assertEquals("1 <= n <= 1000", response.getConstraints());
        assertEquals(2, response.getTimeLimitSeconds());
        assertEquals(Language.JAVA, response.getLanguage());
    }
}
