import { useCallback, useEffect, useState } from "react";
import "./App.css";

const AUTH_API = "http://localhost:8080/Auth";
const QUESTION_API = "http://localhost:8080/Questions";

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
              ? "Access assigned coding assessments and the coding platform."
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
  const [showQuestionManager, setShowQuestionManager] = useState(false);

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
        <section className="hero">
          <div>
            <p className="eyebrow">{user.role}</p>
            <h2>Welcome, {user.name}</h2>
            <p className="hero-copy">
              {isStudent
                ? "Take coding assessments and use the coding platform."
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
              <FeatureCard title="My Assessments" text="View coding assessments assigned to you." />
              <FeatureCard title="Coding Platform" text="Open an assessment and write your solution." />
            </>
          )}

          {isRecruiter && (
            <>
              <FeatureCard title="Assessments" text="Create and manage coding assessments." />
              <FeatureCard title="Candidates" text="View candidates and assessment submissions." />
              <FeatureCard
                title="Manage Questions"
                text="Create, edit, and delete coding questions for your assessments."
                action="Open Question Manager"
                onAction={() => setShowQuestionManager(true)}
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

        {isRecruiter && showQuestionManager && (
          <QuestionManager onClose={() => setShowQuestionManager(false)} />
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
          <h3>{editingId ? "Edit coding question" : "Add coding question"}</h3>
        </div>
        <button className="text-button" type="button" onClick={onClose}>Close</button>
      </div>

      <form onSubmit={handleSubmit}>
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
      </form>

      {message && <div className="alert success">{message}</div>}
      {error && <div className="alert error">{error}</div>}

      <div className="question-list">
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
            <span>Add the first coding question above.</span>
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
      </div>
    </section>
  );
}

export default App;
