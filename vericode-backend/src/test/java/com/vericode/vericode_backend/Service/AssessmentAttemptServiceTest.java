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
    void startReusesExistingAttemptInsteadOfCreatingAnother() {
        AssessmentAttempt existing = attemptWithDeadline(START.plusSeconds(120));
        when(attempts.findByAssessment_IdAndStudent_Id(ASSESSMENT_ID, STUDENT_ID))
                .thenReturn(Optional.of(existing));

        var response = service.start(ASSESSMENT_ID, STUDENT_ID, true);

        assertEquals(START, response.getStartedAt());
        assertEquals(START.plusSeconds(120), response.getDeadlineAt());
        verify(attempts, never()).save(any(AssessmentAttempt.class));
        verifyNoInteractions(assessments, users);
    }

    @Test
    void startRejectsAssessmentThatDoesNotExist() {
        when(attempts.findByAssessment_IdAndStudent_Id(ASSESSMENT_ID, STUDENT_ID))
                .thenReturn(Optional.empty());
        when(assessments.findById(ASSESSMENT_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.start(ASSESSMENT_ID, STUDENT_ID, true));

        verify(assessments, never()).isStudentAssignedToAssessment(any(), any());
        verify(attempts, never()).save(any(AssessmentAttempt.class));
    }

    @Test
    void startRejectsAssessmentNotAssignedToStudent() {
        when(attempts.findByAssessment_IdAndStudent_Id(ASSESSMENT_ID, STUDENT_ID))
                .thenReturn(Optional.empty());
        when(assessments.findById(ASSESSMENT_ID)).thenReturn(Optional.of(new Assessment()));
        when(assessments.isStudentAssignedToAssessment(ASSESSMENT_ID, STUDENT_ID)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> service.start(ASSESSMENT_ID, STUDENT_ID, true));

        verify(users, never()).getReferenceById(any());
        verify(attempts, never()).save(any(AssessmentAttempt.class));
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
    void submissionStatusIsNotStartedWhenNoAttemptExists() {
        when(attempts.findByAssessment_IdAndStudent_Id(ASSESSMENT_ID, STUDENT_ID))
                .thenReturn(Optional.empty());

        assertEquals(AssessmentAttemptService.SubmissionStatus.NOT_STARTED,
                service.submissionStatus(ASSESSMENT_ID, STUDENT_ID));
    }

    @Test
    void submissionStatusIsCompleteWhenAttemptWasSubmitted() {
        AssessmentAttempt attempt = attemptWithDeadline(START.plusSeconds(60));
        attempt.setSubmittedAt(START.plusSeconds(45));
        when(attempts.findByAssessment_IdAndStudent_Id(ASSESSMENT_ID, STUDENT_ID))
                .thenReturn(Optional.of(attempt));

        assertEquals(AssessmentAttemptService.SubmissionStatus.COMPLETE,
                service.submissionStatus(ASSESSMENT_ID, STUDENT_ID));
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

    @Test
    void completionRequiresAnExistingAttempt() {
        when(attempts.findByAssessment_IdAndStudent_Id(ASSESSMENT_ID, STUDENT_ID))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> service.markCompleteIfAllQuestionsSubmitted(
                        ASSESSMENT_ID, STUDENT_ID, 2, 2));

        verify(attempts, never()).save(any(AssessmentAttempt.class));
    }

    @Test
    void completionDoesNotOccurUntilEveryQuestionIsSubmitted() {
        AssessmentAttempt attempt = attemptWithDeadline(START.plusSeconds(60));
        when(attempts.findByAssessment_IdAndStudent_Id(ASSESSMENT_ID, STUDENT_ID))
                .thenReturn(Optional.of(attempt));

        boolean completed = service.markCompleteIfAllQuestionsSubmitted(
                ASSESSMENT_ID, STUDENT_ID, 2, 3);

        assertFalse(completed);
        assertNull(attempt.getSubmittedAt());
        verify(attempts, never()).save(any(AssessmentAttempt.class));
    }

    @Test
    void completionRecordsServerTimestampWhenAllQuestionsAreSubmitted() {
        AssessmentAttempt attempt = attemptWithDeadline(START.plusSeconds(60));
        when(attempts.findByAssessment_IdAndStudent_Id(ASSESSMENT_ID, STUDENT_ID))
                .thenReturn(Optional.of(attempt));
        when(attempts.save(any(AssessmentAttempt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        boolean completed = service.markCompleteIfAllQuestionsSubmitted(
                ASSESSMENT_ID, STUDENT_ID, 3, 3);

        assertTrue(completed);
        assertEquals(START, attempt.getSubmittedAt());
        verify(attempts).save(attempt);
    }

    @Test
    void completionIsIdempotentForAnAlreadySubmittedAttempt() {
        AssessmentAttempt attempt = attemptWithDeadline(START.plusSeconds(60));
        Instant submittedAt = START.minusSeconds(5);
        attempt.setSubmittedAt(submittedAt);
        when(attempts.findByAssessment_IdAndStudent_Id(ASSESSMENT_ID, STUDENT_ID))
                .thenReturn(Optional.of(attempt));

        assertTrue(service.markCompleteIfAllQuestionsSubmitted(
                ASSESSMENT_ID, STUDENT_ID, 1, 4));

        assertEquals(submittedAt, attempt.getSubmittedAt());
        verify(attempts, never()).save(any(AssessmentAttempt.class));
    }

    @Test
    void zeroQuestionAssessmentIsNotAutomaticallyMarkedComplete() {
        AssessmentAttempt attempt = attemptWithDeadline(START.plusSeconds(60));
        when(attempts.findByAssessment_IdAndStudent_Id(ASSESSMENT_ID, STUDENT_ID))
                .thenReturn(Optional.of(attempt));

        assertFalse(service.markCompleteIfAllQuestionsSubmitted(
                ASSESSMENT_ID, STUDENT_ID, 0, 0));

        assertNull(attempt.getSubmittedAt());
        verify(attempts, never()).save(any(AssessmentAttempt.class));
    }

    private AssessmentAttempt attemptWithDeadline(Instant deadline) {
        AssessmentAttempt attempt = new AssessmentAttempt();
        attempt.setConsentAt(START);
        attempt.setStartedAt(START);
        attempt.setDeadlineAt(deadline);
        return attempt;
    }
}
