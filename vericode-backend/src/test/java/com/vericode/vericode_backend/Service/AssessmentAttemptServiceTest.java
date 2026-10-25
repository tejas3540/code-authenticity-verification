package com.vericode.vericode_backend.Service;

import com.vericode.vericode_backend.model.Assessment;
import com.vericode.vericode_backend.model.AssessmentAttempt;
import com.vericode.vericode_backend.model.UserAccount;
import com.vericode.vericode_backend.repository.AssessmentAttemptRepository;
import com.vericode.vericode_backend.repository.AssessmentRepository;
import com.vericode.vericode_backend.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssessmentAttemptServiceTest {
    private static final int ASSESSMENT_ID = 3;
    private static final int STUDENT_ID = 5;
    private static final Instant START = Instant.parse("2026-10-25T10:00:00Z");

    @Mock private AssessmentAttemptRepository attempts;
    @Mock private AssessmentRepository assessments;
    @Mock private UserAccountRepository users;

    private AssessmentAttemptService service;

    @BeforeEach
    void setUp() {
        service = new AssessmentAttemptService(attempts, assessments, users,
                Clock.fixed(START, ZoneOffset.UTC));
    }

    @Test
    void startingRequiresRecordingConsent() {
        assertThrows(IllegalArgumentException.class,
                () -> service.start(ASSESSMENT_ID, STUDENT_ID, false));
        verify(attempts, never()).save(any());
    }

    @Test
    void startCreatesDeadlineFromServerTimeAndAssessmentDuration() {
        Assessment assessment = new Assessment();
        assessment.setDurationMinutes(30);
        UserAccount student = new UserAccount();
        student.setId(STUDENT_ID);
        when(attempts.findByAssessment_IdAndStudent_Id(ASSESSMENT_ID, STUDENT_ID))
                .thenReturn(Optional.empty());
        when(assessments.findById(ASSESSMENT_ID)).thenReturn(Optional.of(assessment));
        when(assessments.isStudentAssignedToAssessment(ASSESSMENT_ID, STUDENT_ID)).thenReturn(true);
        when(users.getReferenceById(STUDENT_ID)).thenReturn(student);
        when(attempts.save(any(AssessmentAttempt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.start(ASSESSMENT_ID, STUDENT_ID, true);

        assertEquals(START, response.getConsentAt());
        assertEquals(START, response.getStartedAt());
        assertEquals(START.plusSeconds(1800), response.getDeadlineAt());
        assertEquals(1800, response.getRemainingSeconds());
        assertTrue(response.isSubmissionAllowed());
    }

    @Test
    void refreshReturnsExistingAttemptAndServerCalculatedRemainingTime() {
        AssessmentAttempt attempt = attemptWithDeadline(START.plusSeconds(60));
        service = new AssessmentAttemptService(attempts, assessments, users,
                Clock.fixed(START.plusSeconds(30), ZoneOffset.UTC));
        when(attempts.findByAssessment_IdAndStudent_Id(ASSESSMENT_ID, STUDENT_ID))
                .thenReturn(Optional.of(attempt));

        var response = service.get(ASSESSMENT_ID, STUDENT_ID).orElseThrow();

        assertEquals(START, response.getStartedAt());
        assertEquals(START.plusSeconds(60), response.getDeadlineAt());
        assertEquals(30, response.getRemainingSeconds());
        verify(attempts, never()).save(any());
    }

    @Test
    void submissionIsAllowedAtGraceBoundaryButRejectedAfterIt() {
        AssessmentAttempt attempt = attemptWithDeadline(START.plusSeconds(60));
        when(attempts.findByAssessment_IdAndStudent_Id(ASSESSMENT_ID, STUDENT_ID))
                .thenReturn(Optional.of(attempt));

        service = new AssessmentAttemptService(attempts, assessments, users,
                Clock.fixed(START.plusSeconds(70), ZoneOffset.UTC));
        assertEquals(AssessmentAttemptService.SubmissionStatus.OPEN,
                service.submissionStatus(ASSESSMENT_ID, STUDENT_ID));

        service = new AssessmentAttemptService(attempts, assessments, users,
                Clock.fixed(START.plusSeconds(70).plusNanos(1), ZoneOffset.UTC));
        assertEquals(AssessmentAttemptService.SubmissionStatus.EXPIRED,
                service.submissionStatus(ASSESSMENT_ID, STUDENT_ID));
    }

    private AssessmentAttempt attemptWithDeadline(Instant deadline) {
        AssessmentAttempt attempt = new AssessmentAttempt();
        attempt.setConsentAt(START);
        attempt.setStartedAt(START);
        attempt.setDeadlineAt(deadline);
        return attempt;
    }
}
