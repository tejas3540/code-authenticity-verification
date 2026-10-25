package com.vericode.vericode_backend.Service;

import com.vericode.vericode_backend.dto.TestCaseRequestDTO;
import com.vericode.vericode_backend.dto.TestCaseResponseDTO;
import com.vericode.vericode_backend.model.Question;
import com.vericode.vericode_backend.model.TestCase;
import com.vericode.vericode_backend.repository.QuestionRepository;
import com.vericode.vericode_backend.repository.TestCaseRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class TestCaseService {
    private final TestCaseRepository testCaseRepository;
    private final QuestionRepository questionRepository;

    public TestCaseService(TestCaseRepository testCaseRepository, QuestionRepository questionRepository) {
        this.testCaseRepository = testCaseRepository;
        this.questionRepository = questionRepository;
    }

    @CacheEvict(cacheNames = "test-cases", allEntries = true)
    public TestCaseResponseDTO create(TestCaseRequestDTO request) {
        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        TestCase testCase = new TestCase();
        testCase.setQuestion(question);
        testCase.setInput(request.getInput());
        testCase.setExpectedOutput(request.getExpectedOutput());
        testCase.setHidden(request.isHidden());
        testCase.setPoints(request.getPoints());
        return toResponse(testCaseRepository.save(testCase), false);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "test-cases", key = "#questionId + ':' + #studentView")
    public List<TestCaseResponseDTO> findForQuestion(Integer questionId, boolean studentView) {
        return testCaseRepository.findByQuestion_IdOrderByIdAsc(questionId)
                .stream()
                .map(testCase -> toResponse(testCase, studentView))
                .toList();
    }

    @CacheEvict(cacheNames = "test-cases", allEntries = true)
    public void delete(Integer id) {
        testCaseRepository.deleteById(id);
    }

    private TestCaseResponseDTO toResponse(TestCase testCase, boolean studentView) {
        boolean hide = studentView && testCase.isHidden();
        return new TestCaseResponseDTO(
                testCase.getId(),
                testCase.getQuestion().getId(),
                hide ? "" : testCase.getInput(),
                hide ? "" : testCase.getExpectedOutput(),
                testCase.isHidden(),
                testCase.getPoints()
        );
    }
}
