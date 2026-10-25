package com.vericode.vericode_backend.controller;

import com.vericode.vericode_backend.model.*;
import com.vericode.vericode_backend.repository.AssessmentRepository;
import com.vericode.vericode_backend.repository.QuestionRepository;
import com.vericode.vericode_backend.repository.TestCaseRepository;
import com.vericode.vericode_backend.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:vericode-test-cases;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.open-in-view=false"
})
@AutoConfigureMockMvc
class TestCaseControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private UserAccountRepository users;
    @Autowired private QuestionRepository questions;
    @Autowired private AssessmentRepository assessments;
    @Autowired private TestCaseRepository testCases;

    @Test
    void assignedStudentCanLoadTestCasesWithOpenInViewDisabled() throws Exception {
        UserAccount recruiter = new UserAccount();
        recruiter.setName("Recruiter");
        recruiter.setEmail("recruiter@example.test");
        recruiter.setPassword("hashed-password");
        recruiter.setRole(Role.RECRUITER);
        recruiter = users.saveAndFlush(recruiter);

        UserAccount student = new UserAccount();
        student.setName("Student");
        student.setEmail("student@example.test");
        student.setPassword("hashed-password");
        student.setRole(Role.STUDENT);
        student = users.saveAndFlush(student);

        Question question = new Question();
        question.setTitle("Add two numbers");
        question.setDescription("Read two integers and print their sum.");
        question.setInputDescription("Two integers");
        question.setOutputDescription("Their sum");
        question.setConstraints("Integers fit in 32 bits");
        question.setTimeLimitSeconds(5);
        question.setLanguage(Language.JAVA);
        question = questions.saveAndFlush(question);

        TestCase testCase = new TestCase();
        testCase.setQuestion(question);
        testCase.setInput("2 3");
        testCase.setExpectedOutput("5");
        testCase.setHidden(false);
        testCase.setPoints(10);
        testCases.saveAndFlush(testCase);

        Assessment assessment = new Assessment();
        assessment.setTitle("Math assessment");
        assessment.setDescription("Test question assignment.");
        assessment.setDurationMinutes(10);
        assessment.setRecruiter(recruiter);
        assessment.setQuestions(List.of(question));
        assessment.setStudents(List.of(student));
        assessments.saveAndFlush(assessment);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", student.getId());
        session.setAttribute("role", "STUDENT");

        mockMvc.perform(get("/TestCases/question/{questionId}", question.getId()).session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"input\":\"2 3\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"expectedOutput\":\"5\"")));
    }
}
