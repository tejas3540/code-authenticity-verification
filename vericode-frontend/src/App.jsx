import { useCallback, useEffect, useState } from "react";
import "./App.css";

const AUTH_API = "http://localhost:8080/Auth";
const QUESTION_API = "http://localhost:8080/Questions";
const ASSESSMENT_API = "http://localhost:8080/Assessments";
const SUBMISSION_API = "http://localhost:8080/Submissions";

const emptyStudent = {
  name: "",
  email: "",
  password: "",
  college: "",
  currentStudyField: "",
};

const emptyRecruiter = {
  recruiterName: "",
  email: "",
  password: "",
  organizationName: "",
};

function App() {
  const [role, setRole] = useState("STUDENT");
  const [mode, setMode] = useState("login");
  const [form, setForm] = useState(emptyStudent);
  const [user, setUser] = useState(null);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetch(`${AUTH_API}/me`, { credentials: "include" })
      .then((response) => response.ok ? response.json() : null)
      .then((data) => data && setUser(data))
      .catch(() => {});
  }, []);

  const selectRole = (nextRole) => {
    setRole(nextRole);
    setMode("login");
    setMessage("");
    setError("");
    setForm(nextRole === "STUDENT" ? emptyStudent : emptyRecruiter);
  };

  const handleChange = (event) => {
    setForm({ ...form, [event.target.name]: event.target.value });
    setMessage("");
    setError("");
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setMessage("");
    setError("");

    try {
      const endpoint =
        mode === "register"
          ? `${AUTH_API}/register/${role === "STUDENT" ? "student" : "recruiter"}`
          : `${AUTH_API}/login`;

      const body = mode === "login" ? { ...form, role } : form;

      const response = await fetch(endpoint, {
        method: "POST",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });

      const data = response.status === 204 ? null : await response.json();

      if (!response.ok) {
        throw new Error(data?.message || "Request failed.");
      }

      setUser(data);
      setMessage(mode === "register" ? "Account created successfully." : "Login successful.");
    } catch (err) {
      setError(err.message || "Something went wrong.");
    } finally {
      setLoading(false);
    }
  };

  const logout = async () => {
    await fetch(`${AUTH_API}/logout`, {
      method: "POST",
      credentials: "include",
    });
    setUser(null);
    setMessage("");
    setError("");
  };

  if (user) {
    return <Dashboard user={user} onLogout={logout} />;
  }

  const isStudent = role === "STUDENT";

  return (
    <div className="auth-shell">
      <div className="auth-card">
        <div className="brand auth-brand">
          <div className="brand-mark">VC</div>
          <div>
            <h1>VeriCode</h1>
            <span>Code Authenticity Verification System</span>
          </div>
        </div>

        <div className="role-tabs">
          {[
            ["STUDENT", "Student"],
            ["RECRUITER", "Recruiter / Institute"],
            ["ADMIN", "Admin"],
          ].map(([value, label]) => (
            <button
              key={value}
              className={role === value ? "role-tab active" : "role-tab"}
              type="button"
              onClick={() => selectRole(value)}
            >
              {label}
            </button>
          ))}
        </div>

        <div className="auth-heading">
          <p className="eyebrow">{mode === "login" ? "SECURE LOGIN" : "ACCOUNT SETUP"}</p>
          <h2>{isStudent ? "Student account" : role === "RECRUITER" ? "Recruiter account" : "Administrator account"}</h2>
          <p>
            {isStudent
              ? "View your assigned assessments and start each assessment when it is available."
              : role === "RECRUITER"
                ? "Manage assessments and review coding behavior and authenticity analysis."
                : "Manage users, assessments, permissions, and system controls."}
          </p>
        </div>

        {role === "ADMIN" && mode === "register" && (
          <div className="alert error">Admin accounts are managed by the system administrator.</div>
        )}

        <form onSubmit={handleSubmit}>
          {mode === "register" && role === "STUDENT" && (
            <>
              <Field label="Name" name="name" value={form.name} onChange={handleChange} />
              <Field label="Email" name="email" type="email" value={form.email} onChange={handleChange} />
              <Field label="Password" name="password" type="password" value={form.password} onChange={handleChange} />
              <Field label="College" name="college" value={form.college} onChange={handleChange} />
              <Field label="Current study field" name="currentStudyField" value={form.currentStudyField} onChange={handleChange} />
            </>
          )}

          {mode === "register" && role === "RECRUITER" && (
            <>
              <Field label="Recruiter name" name="recruiterName" value={form.recruiterName} onChange={handleChange} />
              <Field label="Email" name="email" type="email" value={form.email} onChange={handleChange} />
              <Field label="Password" name="password" type="password" value={form.password} onChange={handleChange} />
              <Field label="Organization name" name="organizationName" value={form.organizationName} onChange={handleChange} />
            </>
          )}

          {mode === "login" && (
            <>
              <Field label="Email" name="email" type="email" value={form.email} onChange={handleChange} />
              <Field label="Password" name="password" type="password" value={form.password} onChange={handleChange} />
            </>
          )}

          {role !== "ADMIN" && (
            <button className="primary-button" type="submit" disabled={loading}>
              {loading ? "Please wait..." : mode === "login" ? "Login" : "Create account"}
            </button>
          )}

          {role === "ADMIN" && mode === "login" && (
            <button className="primary-button" type="submit" disabled={loading}>
              {loading ? "Please wait..." : "Admin login"}
            </button>
          )}
        </form>

        {role !== "ADMIN" && (
          <button
            className="text-button auth-switch"
            type="button"
            onClick={() => {
              setMode(mode === "login" ? "register" : "login");
              setForm(role === "STUDENT" ? emptyStudent : emptyRecruiter);
              setMessage("");
              setError("");
            }}
          >
            {mode === "login" ? "Need an account? Register" : "Already have an account? Login"}
          </button>
        )}

        {message && <div className="alert success">{message}</div>}
        {error && <div className="alert error">{error}</div>}
      </div>
    </div>
  );
}

function Field({ label, name, type = "text", value, onChange }) {
  return (
    <label>
      {label}
      <input name={name} type={type} value={value || ""} onChange={onChange} required />
    </label>
  );
}

function FeatureCard({ title, text, action, onAction }) {
  return (
    <section className="panel feature-card">
      <p className="section-label">VERICODE</p>
      <h3>{title}</h3>
      <p>{text}</p>
      {action && (
        <button className="primary-button" type="button" onClick={onAction}>
          {action}
        </button>
      )}
    </section>
  );
}

function Dashboard({ user, onLogout }) {
  const isStudent = user.role === "STUDENT";
  const isRecruiter = user.role === "RECRUITER";
  const [view, setView] = useState("dashboard");

  return (
    <div className="dashboard-shell">
      <header className="topbar">
        <div className="brand">
          <div className="brand-mark">VC</div>
          <div>
            <h1>VeriCode</h1>
            <span>{isStudent ? "Student Portal" : isRecruiter ? "Recruiter Portal" : "Admin Console"}</span>
          </div>
        </div>
        <button className="text-button logout" type="button" onClick={onLogout}>Logout</button>
      </header>

      <main className="dashboard">
        {view === "dashboard" && <>
        <section className="hero">
          <div>
            <p className="eyebrow">{user.role}</p>
            <h2>Welcome, {user.name}</h2>
            <p className="hero-copy">
              {isStudent
                ? "View your assigned assessments and open the coding workspace from Start Assessment."
                : isRecruiter
                  ? "Manage assessments and review behavior tracking and authenticity analysis."
                  : "Manage the VeriCode platform and its users."}
            </p>
          </div>
          <div className="stat-card">
            <span>Account role</span>
            <strong>{user.role}</strong>
          </div>
        </section>

        <div className="dashboard-cards">
          {isStudent && (
            <>
              <FeatureCard title="My Assessments" text="View assessments assigned to your account and open their questions." action="View Assessments" onAction={() => setView("studentAssessments")} />
            </>
          )}

          {isRecruiter && (
            <>
              <FeatureCard title="Assessments" text="Create assessments, choose questions, and assign them to registered students by email." action="Create Assessment" onAction={() => setView("assessmentManager")} />
              <FeatureCard title="Candidates" text="View candidates and assessment submissions." />
              <FeatureCard
                title="Manage Questions"
                text="Create, edit, and delete coding questions for your assessments."
                action="Open Question Manager"
                onAction={() => setView("questionManager")}
              />
              <FeatureCard title="Behavior Tracking" text="Review coding-session behavior signals." />
              <FeatureCard title="Authenticity Analysis" text="Review authenticity scores, risk levels, and evidence." />
            </>
          )}

          {!isStudent && !isRecruiter && (
            <>
              <FeatureCard title="Manage Users" text="Manage platform accounts and access." />
              <FeatureCard title="Manage Recruiters & Institutes" text="Manage organizations using VeriCode." />
              <FeatureCard title="Manage Students" text="Manage student accounts." />
              <FeatureCard title="Manage Questions" text="Manage the coding question bank." />
              <FeatureCard title="Manage Assessments" text="Manage platform-wide assessments." />
              <FeatureCard title="System-wide Activity" text="Review system activity and analysis." />
              <FeatureCard title="Roles & Permissions" text="Control role-based access." />
              <FeatureCard title="System Controls" text="Manage system-level settings." />
            </>
          )}
        </div>
        </>}

        {view !== "dashboard" && (
          <div className="page-view">
            <button className="back-link" type="button" onClick={() => setView("dashboard")}>
              ← Back to dashboard
            </button>
            {isRecruiter && view === "assessmentManager" && (
              <AssessmentManager onClose={() => setView("dashboard")} />
            )}
            {isRecruiter && view === "questionManager" && (
              <QuestionManager onClose={() => setView("dashboard")} />
            )}
            {isStudent && view === "studentAssessments" && (
              <StudentAssessmentPlatform onClose={() => setView("dashboard")} />
            )}
          </div>
        )}
      </main>
    </div>
  );
}

function QuestionManager({ onClose }) {
  const emptyQuestion = {
    title: "",
    description: "",
    inputDescription: "",
    outputDescription: "",
    constraints: "",
    timeLimitSeconds: 60,
    language: "JAVA",
  };

  const [questions, setQuestions] = useState([]);
  const [form, setForm] = useState(emptyQuestion);
  const [editingId, setEditingId] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const loadQuestions = useCallback(async () => {
    setLoading(true);
    try {
      const response = await fetch(QUESTION_API);
      if (!response.ok) throw new Error("Unable to load questions.");
      setQuestions(await response.json());
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadQuestions();
  }, [loadQuestions]);

  const handleChange = (event) => {
    setForm({ ...form, [event.target.name]: event.target.value });
    setMessage("");
    setError("");
  };

  const resetForm = () => {
    setForm(emptyQuestion);
    setEditingId(null);
    setShowForm(false);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setMessage("");
    setError("");

    const editing = editingId !== null;
    const url = editing ? `${QUESTION_API}/${editingId}` : QUESTION_API;

    try {
      const response = await fetch(url, {
        method: editing ? "PUT" : "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          ...form,
          timeLimitSeconds: Number(form.timeLimitSeconds),
        }),
      });

      const data = await response.json();
      if (!response.ok) throw new Error(data.message || "Unable to save question.");

      setMessage(editing ? "Question updated successfully." : "Question created successfully.");
      resetForm();
      await loadQuestions();
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  };

  const editQuestion = (question) => {
    setEditingId(question.id);
    setShowForm(true);
    setForm({
      title: question.title,
      description: question.description,
      inputDescription: question.inputDescription,
      outputDescription: question.outputDescription,
      constraints: question.constraints,
      timeLimitSeconds: question.timeLimitSeconds,
      language: question.language,
    });
  };

  const deleteQuestion = async (id) => {
    if (!window.confirm("Delete this question?")) return;

    try {
      const response = await fetch(`${QUESTION_API}/${id}`, { method: "DELETE" });
      const data = await response.json();
      if (!response.ok) throw new Error(data.message || "Unable to delete question.");
      setMessage("Question deleted successfully.");
      await loadQuestions();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <section className="question-manager panel">
      <div className="panel-heading">
        <div>
          <p className="section-label">RECRUITER QUESTION BANK</p>
          <h3>{showForm ? (editingId ? "Edit coding question" : "Add coding question") : "Manage coding questions"}</h3>
        </div>
        <div className="actions">
          {!showForm && <button className="primary-button" type="button" onClick={() => { resetForm(); setShowForm(true); }}>+ Add Question</button>}
          {showForm && <button className="text-button" type="button" onClick={resetForm}>Back to questions</button>}
          <button className="text-button" type="button" onClick={onClose}>Close</button>
        </div>
      </div>

      {showForm && <form onSubmit={handleSubmit}>
        <label>
          Question title
          <input name="title" value={form.title} onChange={handleChange} placeholder="e.g. Two Sum" required />
        </label>
        <label>
          Problem description
          <textarea name="description" value={form.description} onChange={handleChange} rows="4" required />
        </label>
        <label>
          Input description
          <textarea name="inputDescription" value={form.inputDescription} onChange={handleChange} rows="3" required />
        </label>
        <label>
          Output description
          <textarea name="outputDescription" value={form.outputDescription} onChange={handleChange} rows="3" required />
        </label>
        <label>
          Constraints
          <textarea name="constraints" value={form.constraints} onChange={handleChange} rows="3" required />
        </label>
        <label>
          Time limit (seconds)
          <input type="number" min="1" name="timeLimitSeconds" value={form.timeLimitSeconds} onChange={handleChange} required />
        </label>
        <label>
          Language
          <select name="language" value={form.language} onChange={handleChange}>
            <option value="JAVA">Java</option>
          </select>
        </label>
        <div className="actions">
          <button className="primary-button" type="submit" disabled={saving}>
            {saving ? "Saving..." : editingId ? "Update question" : "Add question"}
          </button>
          {editingId && (
            <button className="text-button" type="button" onClick={resetForm}>Cancel edit</button>
          )}
        </div>
      </form>}

      {message && <div className="alert success">{message}</div>}
      {error && <div className="alert error">{error}</div>}

      {!showForm && <div className="question-list">
        <div className="panel-heading">
          <div>
            <p className="section-label">QUESTION BANK</p>
            <h3>Existing questions</h3>
          </div>
          <button className="text-button" type="button" onClick={loadQuestions}>Refresh</button>
        </div>

        {loading ? (
          <div className="empty-state">Loading questions...</div>
        ) : questions.length === 0 ? (
          <div className="empty-state">
            <strong>No questions yet</strong>
            <span>Use the Add Question action to create your first problem.</span>
          </div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Question</th>
                  <th>Language</th>
                  <th>Time</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {questions.map((question) => (
                  <tr key={question.id}>
                    <td><span className="id-badge">#{question.id}</span></td>
                    <td className="candidate-name">{question.title}</td>
                    <td>{question.language}</td>
                    <td>{question.timeLimitSeconds}s</td>
                    <td>
                      <div className="actions">
                        <button className="action-button edit" type="button" onClick={() => editQuestion(question)}>Edit</button>
                        <button className="action-button delete" type="button" onClick={() => deleteQuestion(question.id)}>Delete</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>}
    </section>
  );
}


function AssessmentManager({ onClose }) {
  const [questions, setQuestions] = useState([]);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [durationMinutes, setDurationMinutes] = useState(60);
  const [studentEmails, setStudentEmails] = useState("");
  const [questionIds, setQuestionIds] = useState([]);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    fetch(QUESTION_API)
      .then((r) => { if (!r.ok) throw new Error("Could not load questions."); return r.json(); })
      .then(setQuestions)
      .catch((e) => setError(e.message));
  }, []);

  const toggleQuestion = (id) => setQuestionIds((old) =>
    old.includes(id) ? old.filter((item) => item !== id) : [...old, id]
  );

  const createAssessment = async (event) => {
    event.preventDefault();
    setSaving(true); setError(""); setMessage("");
    try {
      const response = await fetch(ASSESSMENT_API, {
        method: "POST",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          title, description, durationMinutes: Number(durationMinutes), questionIds,
          studentEmails: studentEmails.split(/[;,\n]/).map((email) => email.trim()).filter(Boolean),
        }),
      });
      const data = response.status === 204 ? null : await response.json();
      if (!response.ok) throw new Error(data?.message || "Assessment creation failed. Check student emails and selected questions.");
      setMessage(`Assessment "${data.title}" created and assigned to ${data.studentEmails.length} student(s).`);
      setTitle(""); setDescription(""); setDurationMinutes(60); setStudentEmails(""); setQuestionIds([]);
    } catch (e) { setError(e.message); }
    finally { setSaving(false); }
  };

  return (
    <section className="panel question-manager">
      <div className="panel-heading"><div><p className="section-label">ASSESSMENT BUILDER</p><h3>Create and assign assessment</h3></div><button className="text-button" type="button" onClick={onClose}>Close</button></div>
      <form onSubmit={createAssessment}>
        <label>Assessment title<input value={title} onChange={(e) => setTitle(e.target.value)} required placeholder="e.g. Java Fundamentals Test" /></label>
        <label>Description<textarea value={description} onChange={(e) => setDescription(e.target.value)} rows="3" /></label>
        <label>Duration (minutes)<input type="number" min="1" value={durationMinutes} onChange={(e) => setDurationMinutes(e.target.value)} required /></label>
        <label>Student email addresses<textarea value={studentEmails} onChange={(e) => setStudentEmails(e.target.value)} rows="3" required placeholder="student1@example.com, student2@example.com" /><span>Enter registered student emails separated by commas or new lines.</span></label>
        <div><p><strong>Select questions</strong></p>
          {questions.length === 0 ? <p className="empty-state">No questions available. Add questions in Manage Questions first.</p> : questions.map((q) => (
            <label key={q.id} className="question-choice"><input type="checkbox" checked={questionIds.includes(q.id)} onChange={() => toggleQuestion(q.id)} /><span><strong>{q.title}</strong><small>{q.language} · {q.timeLimitSeconds}s</small></span></label>
          ))}
        </div>
        <button className="primary-button" type="submit" disabled={saving || questions.length === 0 || questionIds.length === 0}>{saving ? "Creating..." : "Create and assign assessment"}</button>
      </form>
      {message && <div className="alert success">{message}</div>}
      {error && <div className="alert error">{error}</div>}
    </section>
  );
}

function StudentAssessmentPlatform({ onClose }) {
  const [assessments, setAssessments] = useState([]);
  const [selected, setSelected] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  const load = useCallback(async () => {
    setLoading(true); setError("");
    try {
      const response = await fetch(`${ASSESSMENT_API}/my`, { credentials: "include" });
      if (!response.ok) throw new Error(response.status === 401 ? "Your session expired. Please log in again." : "Could not load assigned assessments.");
      setAssessments(await response.json());
    } catch (e) { setError(e.message); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, [load]);

  return (
    <section className="panel question-manager">
      <div className="panel-heading"><div><p className="section-label">STUDENT ASSESSMENTS</p><h3>{selected ? selected.title : "My assigned assessments"}</h3></div><button className="text-button" type="button" onClick={() => selected ? setSelected(null) : onClose()}>{selected ? "Back to assessments" : "Close"}</button></div>
      {error && <div className="alert error">{error}</div>}
      {notice && <div className="alert success">{notice}</div>}
      {loading ? <p>Loading assessments...</p> : !selected ? (
        assessments.length === 0 ? <div className="empty-state"><strong>No assessments assigned yet</strong><span>Your recruiter must create an assessment and assign it to your registered email address.</span></div> :
        <div className="assessment-list">{assessments.map((assessment) => (
          <article className="assessment-item" key={assessment.id}>
            <div><h4>{assessment.title}</h4><p>{assessment.description || "Coding assessment"}</p><small>{assessment.questions.length} question(s) · {assessment.durationMinutes} minutes · Created by {assessment.recruiterName}</small></div>
            <button className="primary-button" type="button" onClick={() => { setSelected(assessment); setNotice(""); }}>Start Assessment</button>
          </article>
        ))}</div>
      ) : (
        <div className="coding-workspace">
          <p>{selected.description}</p><p><strong>Duration:</strong> {selected.durationMinutes} minutes · <strong>Language:</strong> Java</p>
          {selected.questions.map((question) => <CodeQuestion key={question.id} assessmentId={selected.id} question={question} onSubmitted={(text) => setNotice(text)} />)}
        </div>
      )}
    </section>
  );
}

function CodeQuestion({ assessmentId, question, onSubmitted }) {
  const [code, setCode] = useState("public class Main {\n    public static void main(String[] args) {\n        // Write your solution here\n    }\n}");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const submit = async () => {
    setSaving(true); setError("");
    try {
      const response = await fetch(SUBMISSION_API, {
        method: "POST", credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ assessmentId, questionId: question.id, sourceCode: code }),
      });
      const data = await response.json();
      if (!response.ok) throw new Error(data.message || "Submission failed.");
      onSubmitted(`Submitted "${question.title}" successfully at ${new Date(data.submittedAt).toLocaleString()}.`);
    } catch (e) { setError(e.message); }
    finally { setSaving(false); }
  };

  return (
    <article className="code-question">
      <h4>{question.title}</h4><p>{question.description}</p>
      <p><strong>Input:</strong> {question.inputDescription}</p><p><strong>Output:</strong> {question.outputDescription}</p><p><strong>Constraints:</strong> {question.constraints}</p>
      <label>Java solution<textarea className="code-editor" spellCheck="false" value={code} onChange={(e) => setCode(e.target.value)} rows="14" /></label>
      <button className="primary-button" type="button" disabled={saving || !code.trim()} onClick={submit}>{saving ? "Submitting..." : "Submit solution"}</button>
      {error && <div className="alert error">{error}</div>}
      <p className="editor-note">Submissions are saved. Code execution and automated test-case evaluation are not implemented yet.</p>
    </article>
  );
}

export default App;
