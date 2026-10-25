package com.vericode.vericode_backend.Service;

import com.vericode.vericode_backend.dto.QuestionRequestDTO;
import com.vericode.vericode_backend.dto.QuestionResponseDTO;
import com.vericode.vericode_backend.model.Question;
import com.vericode.vericode_backend.repository.QuestionRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @CacheEvict(cacheNames = {"questions", "question"}, allEntries = true)
    public QuestionResponseDTO save(QuestionRequestDTO request) {
        Question question = new Question();
        mapRequestToQuestion(request, question);

        Question savedQuestion = questionRepository.save(question);
        return toResponseDTO(savedQuestion);
    }

    @Cacheable("questions")
    public List<QuestionResponseDTO> getAllQuestions() {
        return questionRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Cacheable(value = "question", key = "#id")
    public QuestionResponseDTO getQuestionById(Integer id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Question not found with id " + id));

        return toResponseDTO(question);
    }

    @CacheEvict(cacheNames = {"questions", "question"}, allEntries = true)
    public QuestionResponseDTO updateQuestion(Integer id, QuestionRequestDTO request) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Question not found with id " + id));

        mapRequestToQuestion(request, question);

        Question updatedQuestion = questionRepository.save(question);
        return toResponseDTO(updatedQuestion);
    }

    @CacheEvict(cacheNames = {"questions", "question"}, allEntries = true)
    public QuestionResponseDTO deleteQuestion(Integer id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Question not found with id " + id));

        questionRepository.delete(question);
        return toResponseDTO(question);
    }

    private void mapRequestToQuestion(QuestionRequestDTO request, Question question) {
        question.setTitle(request.getTitle());
        question.setDescription(request.getDescription());
        question.setInputDescription(request.getInputDescription());
        question.setOutputDescription(request.getOutputDescription());
        question.setConstraints(request.getConstraints());
        question.setTimeLimitSeconds(request.getTimeLimitSeconds());
        question.setLanguage(request.getLanguage());
    }

    private QuestionResponseDTO toResponseDTO(Question question) {
        QuestionResponseDTO response = new QuestionResponseDTO();
        response.setId(question.getId());
        response.setTitle(question.getTitle());
        response.setDescription(question.getDescription());
        response.setInputDescription(question.getInputDescription());
        response.setOutputDescription(question.getOutputDescription());
        response.setConstraints(question.getConstraints());
        response.setTimeLimitSeconds(question.getTimeLimitSeconds());
        response.setLanguage(question.getLanguage());
        return response;
    }
}
