import { useEffect, useState } from "react";
import "./App.css";

const AUTH_API = "http://localhost:8080/Auth";

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

function Dashboard({ user, onLogout }) {
  const isStudent = user.role === "STUDENT";
  const isRecruiter = user.role === "RECRUITER";

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
      </main>
    </div>
  );
}

function FeatureCard({ title, text }) {
  return (
    <section className="panel feature-card">
      <p className="section-label">VERICODE</p>
      <h3>{title}</h3>
      <p>{text}</p>
    </section>
  );
}

export default App;
