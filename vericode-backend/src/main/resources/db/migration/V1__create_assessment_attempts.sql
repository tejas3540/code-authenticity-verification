CREATE TABLE assessment_attempts (
    id INT NOT NULL AUTO_INCREMENT,
    assessment_id INT NOT NULL,
    student_id INT NOT NULL,
    consent_at TIMESTAMP(6) NOT NULL,
    started_at TIMESTAMP(6) NOT NULL,
    deadline_at TIMESTAMP(6) NOT NULL,
    submitted_at TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_assessment_attempt_assessment_student UNIQUE (assessment_id, student_id),
    INDEX idx_assessment_attempt_student (student_id),
    INDEX idx_assessment_attempt_deadline (deadline_at)
);
