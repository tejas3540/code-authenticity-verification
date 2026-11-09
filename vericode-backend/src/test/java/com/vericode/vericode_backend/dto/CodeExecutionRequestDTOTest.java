package com.vericode.vericode_backend.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeExecutionRequestDTOTest {

    private ValidatorFactory validatorFactory;
    private Validator validator;

    @BeforeEach
    void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterEach
    void tearDown() {
        validatorFactory.close();
    }

    @Test
    void acceptsRequestWithSourceCodeAndDefaultTimeLimit() {
        CodeExecutionRequestDTO request = validRequest();

        Set<ConstraintViolation<CodeExecutionRequestDTO>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
        assertEquals(5, request.getTimeLimitSeconds());
    }

    @Test
    void rejectsBlankSourceCode() {
        CodeExecutionRequestDTO request = validRequest();
        request.setSourceCode("   ");

        Set<ConstraintViolation<CodeExecutionRequestDTO>> violations = validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("sourceCode")));
    }

    @Test
    void rejectsZeroTimeLimit() {
        CodeExecutionRequestDTO request = validRequest();
        request.setTimeLimitSeconds(0);

        Set<ConstraintViolation<CodeExecutionRequestDTO>> violations = validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("timeLimitSeconds")));
    }

    @Test
    void rejectsNullTimeLimit() {
        CodeExecutionRequestDTO request = validRequest();
        request.setTimeLimitSeconds(null);

        Set<ConstraintViolation<CodeExecutionRequestDTO>> violations = validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("timeLimitSeconds")));
    }

    @Test
    void treatsNullOptionalInputFieldsAsEmptyStrings() {
        CodeExecutionRequestDTO request = validRequest();
        request.setInput(null);
        request.setExpectedOutput(null);

        assertEquals("", request.getInput());
        assertEquals("", request.getExpectedOutput());
        assertFalse(validator.validate(request).stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("input")));
    }

    private CodeExecutionRequestDTO validRequest() {
        CodeExecutionRequestDTO request = new CodeExecutionRequestDTO();
        request.setSourceCode("public class Main { public static void main(String[] args) {} }");
        return request;
    }
}
