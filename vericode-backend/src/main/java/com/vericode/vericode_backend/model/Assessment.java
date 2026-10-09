package com.vericode.vericode_backend.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assessments")
public class Assessment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer durationMinutes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recruiter_id", nullable = false)
    private UserAccount recruiter;

    @ManyToMany
    @JoinTable(name = "assessment_questions",
        joinColumns = @JoinColumn(name = "assessment_id"),
        inverseJoinColumns = @JoinColumn(name = "question_id"))
    private List<Question> questions = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "assessment_students",
        joinColumns = @JoinColumn(name = "assessment_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id"))
    private List<UserAccount> students = new ArrayList<>();

    public Assessment() {}
    public Integer getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public UserAccount getRecruiter() { return recruiter; }
    public List<Question> getQuestions() { return questions; }
    public List<UserAccount> getStudents() { return students; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public void setRecruiter(UserAccount recruiter) { this.recruiter = recruiter; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
    public void setStudents(List<UserAccount> students) { this.students = students; }
}