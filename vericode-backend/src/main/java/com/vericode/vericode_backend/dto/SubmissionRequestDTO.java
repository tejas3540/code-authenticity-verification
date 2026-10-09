package com.vericode.vericode_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class SubmissionRequestDTO {
    @NotNull private Integer assessmentId;
    @NotNull private Integer questionId;
    @NotBlank private String sourceCode;
    private List<BehaviorEventDTO> behaviorEvents = new ArrayList<>();

    public Integer getAssessmentId() { return assessmentId; }
    public Integer getQuestionId() { return questionId; }
    public String getSourceCode() { return sourceCode; }
    public List<BehaviorEventDTO> getBehaviorEvents() { return behaviorEvents; }
    public void setAssessmentId(Integer assessmentId) { this.assessmentId = assessmentId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }
    public void setBehaviorEvents(List<BehaviorEventDTO> behaviorEvents) { this.behaviorEvents = behaviorEvents; }

    public static class BehaviorEventDTO {
        private String eventType;
        private String occurredAt;
        private String details;

        public String getEventType() { return eventType; }
        public String getOccurredAt() { return occurredAt; }
        public String getDetails() { return details; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public void setOccurredAt(String occurredAt) { this.occurredAt = occurredAt; }
        public void setDetails(String details) { this.details = details; }
    }
}