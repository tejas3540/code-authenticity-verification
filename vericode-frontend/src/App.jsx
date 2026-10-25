import { lazy, Suspense, useCallback, useEffect, useMemo, useState } from "react";
import "./App.css";
const QuestionImport = lazy(() => import("./QuestionImport"));

const API = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
const AUTH = `${API}/Auth`;
const QUESTIONS = `${API}/Questions`;
const TESTCASES = `${API}/TestCases`;
const ASSESSMENTS = `${API}/Assessments`;
const EXECUTION = `${API}/CodeExecution`;
const SUBMISSIONS = `${API}/Submissions`;

const blankStudent = { name: "", email: "", password: "", college: "", currentStudyField: "" };
const blankRecruiter = { recruiterName: "", email: "", password: "", organizationName: "" };
const starterCode = `public class Main {
    public static void main(String[] args) {
        // Write your solution here
    }
}`;

async function jsonFetch(url, options = {}) {
  const response = await fetch(url, { credentials: "include", ...options });
  const data = response.status === 204 ? null : await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(data?.message || `Request failed (${response.status}).`);
  return data;
}

function App() {
  const [user, setUser] = useState(null);
  const [checking, setChecking] = useState(true);
  const [role, setRole] = useState("STUDENT");
  const [mode, setMode] = useState("login");
  const [form, setForm] = useState(blankStudent);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    let mounted = true;
    async function checkSession() {
      try {
        const currentUser = await jsonFetch(`${AUTH}/me`);
        if (mounted) setUser(currentUser);
      } catch {
        // No active session is a normal unauthenticated state.
      } finally {
        if (mounted) setChecking(false);
      }
    }
    checkSession();
    return () => { mounted = false; };
  }, []);

  if (checking) return <div className="boot-screen"><div className="brand-mark">VC</div><span>Loading VeriCode</span></div>;
  if (user) return <Dashboard user={user} onLogout={() => { jsonFetch(`${AUTH}/logout`, { method: "POST" }).finally(() => setUser(null)); }} />;

  const changeRole = (next) => {
    setRole(next);
    setMode("login");
    setForm(next === "STUDENT" ? blankStudent : blankRecruiter);
    setError("");
  };

  const update = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async (event) => {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      const endpoint = mode === "register"
        ? `${AUTH}/register/${role === "STUDENT" ? "student" : "recruiter"}`
        : `${AUTH}/login`;
      const body = mode === "login" ? { ...form, role } : form;
      setUser(await jsonFetch(endpoint, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      }));
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  };

  return <div className="auth-shell">
    <aside className="auth-side">
      <div className="brand inverse"><div className="brand-mark">VC</div><div><b>VeriCode</b><span>Technical assessment platform</span></div></div>
      <div className="auth-side-copy">
        <span className="eyebrow">CODING ASSESSMENTS</span>
        <h2>A serious coding workspace for technical hiring.</h2>
        <p>Create assessments, run Java solutions in isolated Docker containers, and review candidate submissions from one workspace.</p>
        <div className="auth-points"><span>✓ Timed coding assessments</span><span>✓ Automatic test-case evaluation</span><span>✓ Recruiter question library</span></div>
      </div>
    </aside>
    <section className="auth-card">
      <div className="brand"><div className="brand-mark">VC</div><div><b>VeriCode</b><span>Sign in to continue</span></div></div>
      <div className="role-tabs">
        <button type="button" className={role === "STUDENT" ? "role-tab active" : "role-tab"} onClick={() => changeRole("STUDENT")}>Student</button>
        <button type="button" className={role === "RECRUITER" ? "role-tab active" : "role-tab"} onClick={() => changeRole("RECRUITER")}>Recruiter</button>
      </div>
      <div className="auth-heading"><span className="eyebrow">{mode === "login" ? "WELCOME BACK" : "ACCOUNT SETUP"}</span><h1>{mode === "login" ? "Sign in" : "Create your account"}</h1><p>{role === "STUDENT" ? "Open assessments assigned to you." : "Build assessments and review candidate submissions."}</p></div>
      <form onSubmit={submit}>
        {mode === "register" && role === "STUDENT" && <><Field label="Full name" name="name" value={form.name} onChange={update}/><Field label="College" name="college" value={form.college} onChange={update}/><Field label="Study field" name="currentStudyField" value={form.currentStudyField} onChange={update}/></>}
        {mode === "register" && role === "RECRUITER" && <><Field label="Recruiter name" name="recruiterName" value={form.recruiterName} onChange={update}/><Field label="Organization" name="organizationName" value={form.organizationName} onChange={update}/></>}
        <Field label="Email" name="email" type="email" value={form.email} onChange={update}/>
        <Field label="Password" name="password" type="password" value={form.password} onChange={update}/>
        {error && <div className="alert error">{error}</div>}
        <button className="primary-button wide" disabled={busy}>{busy ? "Please wait…" : mode === "login" ? "Sign in" : "Create account"}</button>
      </form>
      <button type="button" className="text-button auth-switch" onClick={() => { setMode(mode === "login" ? "register" : "login"); setForm(role === "STUDENT" ? blankStudent : blankRecruiter); setError(""); }}>{mode === "login" ? "Create an account" : "Back to sign in"}</button>
    </section>
  </div>;
}

function Field({ label, name, type = "text", value, onChange }) { return <label>{label}<input required name={name} type={type} value={value || ""} onChange={onChange}/></label>; }
function FieldArea({ label, value, onChange, rows = 4, required = true }) { return <label>{label}<textarea rows={rows} value={value || ""} onChange={onChange} required={required}/></label>; }
function Metric({ label, value }) { return <section className="metric"><span>{label}</span><strong>{value}</strong></section>; }

function Dashboard({ user, onLogout }) {
  const student = user.role === "STUDENT";
  const recruiter = user.role === "RECRUITER";
  const [view, setView] = useState("dashboard");
  const [collapsed, setCollapsed] = useState(false);
  const nav = student
    ? [["dashboard","Overview","⌂"],["assessments","My Assessments","▣"]]
    : recruiter
      ? [["dashboard","Overview","⌂"],["questions","Question Library","◇"],["assessments","Assessments","▤"],["candidates","Candidates","◎"],["submissions","Submissions","◷"]]
      : [["dashboard","Overview","⌂"]];

  return <div className="app-shell">
    <aside className={collapsed ? "sidebar collapsed" : "sidebar"}>
      <div className="side-brand"><div className="brand-mark">VC</div>{!collapsed && <div><b>VeriCode</b><span>{student ? "Student workspace" : "Recruiter workspace"}</span></div>}</div>
      <nav>{nav.map(([key,label,icon]) => <button type="button" key={key} className={view === key ? "nav-item active" : "nav-item"} onClick={() => setView(key)}><span>{icon}</span>{!collapsed && label}</button>)}</nav>
      <div className="side-bottom">{!collapsed && <div className="account-mini"><div className="avatar">{(user.name || user.recruiterName || "U")[0]}</div><div><b>{user.name || user.recruiterName}</b><span>{user.email}</span></div></div>}<button type="button" className="nav-item" onClick={onLogout}><span>↪</span>{!collapsed && "Sign out"}</button></div>
    </aside>
    <main className="main-shell">
      <header className="main-header"><button type="button" className="menu-button" onClick={() => setCollapsed(!collapsed)}>☰</button><strong>{nav.find(n => n[0] === view)?.[1] || "Overview"}</strong><div className="header-user"><span>{user.role}</span><div className="avatar small">{(user.name || user.recruiterName || "U")[0]}</div></div></header>
      <div className="content">
        {view === "dashboard" && <Home user={user} student={student} go={setView}/>}
        {student && view === "assessments" && <StudentAssessments/>}
        {recruiter && view === "questions" && <QuestionLibrary/>}
        {recruiter && view === "assessments" && <AssessmentBuilder/>}
        {recruiter && view === "submissions" && <Submissions/>}
        {recruiter && view === "candidates" && <Candidates/>}
      </div>
    </main>
  </div>;
}

function Home({ user, student, go }) {
  return <div className="page">
    <div className="page-intro"><div><span className="eyebrow">{student ? "STUDENT WORKSPACE" : "RECRUITER WORKSPACE"}</span><h1>{student ? `Good to see you, ${user.name}.` : `Welcome back, ${user.recruiterName || user.name}.`}</h1><p>{student ? "Your assigned assessments are ready when you are." : "Build coding assessments and review candidate results."}</p></div></div>
    {student ? <><div className="dashboard-grid"><Metric label="Assigned assessments" value="Open"/><Metric label="Coding workspace" value="Ready"/><Metric label="Language" value="Java 21"/></div><section className="panel quick-panel"><div><span className="eyebrow">NEXT STEP</span><h2>Take an assessment</h2><p className="muted">Open an assigned assessment to enter the timed coding workspace.</p></div><button type="button" className="primary-button" onClick={() => go("assessments")}>View assessments</button></section></> : <><div className="dashboard-grid"><Metric label="Question library" value="Manage"/><Metric label="Assessments" value="Build"/><Metric label="Submissions" value="Review"/></div><section className="panel quick-panel"><div><span className="eyebrow">QUICK ACTIONS</span><h2>Recruiter tools</h2></div><div className="quick-actions"><button type="button" onClick={() => go("questions")}>＋ New question</button><button type="button" onClick={() => go("assessments")}>＋ New assessment</button><button type="button" onClick={() => go("submissions")}>View submissions</button></div></section></>}
  </div>;
}

function QuestionLibrary() {
  const [questions, setQuestions] = useState([]);
  const [selected, setSelected] = useState(null);
  const [search, setSearch] = useState("");
  const [debouncedSearch, setDebouncedSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const load = async () => {
    setLoading(true);
    setError("");
    try {
      const data = await jsonFetch(QUESTIONS);
      setQuestions(Array.isArray(data) ? data : []);
    } catch (e) {
      setQuestions([]);
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  useEffect(() => {
    const timeout = window.setTimeout(() => setDebouncedSearch(search), 250);
    return () => window.clearTimeout(timeout);
  }, [search]);

  const filtered = useMemo(() => questions.filter(q => (q.title || "").toLowerCase().includes(debouncedSearch.toLowerCase())), [questions, debouncedSearch]);

  return <div className="page">
    <div className="page-intro row"><div><span className="eyebrow">QUESTION LIBRARY</span><h1>Coding problems</h1><p>Create original problems or import a public problem as an editable draft.</p></div><div className="header-actions"><Suspense fallback={<button type="button" className="ghost-button" disabled>Loading importer…</button>}><QuestionImport/></Suspense><button type="button" className="primary-button" onClick={() => setSelected({})}>＋ Create question</button></div></div>
    {selected !== null ? <QuestionEditor question={selected} onDone={() => { setSelected(null); load(); }}/> : <section className="panel">
      <div className="library-toolbar"><input className="search" placeholder="Search questions…" value={search} onChange={e => setSearch(e.target.value)}/><button type="button" className="ghost-button" onClick={load}>Refresh</button></div>
      {loading ? <div className="question-skeleton" role="status" aria-label="Loading questions">{[0, 1, 2].map(item => <div className="question-skeleton-row" key={item}><span/><div><i/><i/></div></div>)}</div> : error ? <div className="empty-state"><strong>Could not load questions</strong><span>{error}</span><button type="button" className="primary-button" onClick={load}>Try again</button></div> : !filtered.length ? <div className="empty-state"><strong>No questions found</strong><span>Create a question or import one to start your library.</span></div> : <div className="question-table">{filtered.map((q, i) => <div className="question-row" key={q.id}><div className="q-main"><div className="q-number">{String(i + 1).padStart(2, "0")}</div><div><h3>{q.title}</h3><p>{q.language} · {q.timeLimitSeconds}s</p></div></div><div className="row-actions"><button type="button" onClick={() => setSelected(q)}>Edit</button></div></div>)}</div>}
    </section>}
  </div>;
}

function QuestionEditor({ question, onDone }) {
  const isNew = !question.id;
  const [form, setForm] = useState({ title: question.title || "", description: question.description || "", inputDescription: question.inputDescription || "", outputDescription: question.outputDescription || "", constraints: question.constraints || "", timeLimitSeconds: question.timeLimitSeconds || 60, language: question.language || "JAVA" });
  const [cases, setCases] = useState([]);
  const [caseForm, setCaseForm] = useState({ input: "", expectedOutput: "", hidden: false, points: 10 });
  const [savedId, setSavedId] = useState(question.id || null);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const loadCases = useCallback(async () => {
    if (!savedId) return;
    try { setCases(await jsonFetch(`${TESTCASES}/question/${savedId}`)); }
    catch (e) { setCases([]); setError(e.message); }
  }, [savedId]);

  useEffect(() => {
    loadCases();
  }, [loadCases]);

  const save = async e => {
    e.preventDefault();
    setError("");
    try {
      const data = await jsonFetch(savedId ? `${QUESTIONS}/${savedId}` : QUESTIONS, { method: savedId ? "PUT" : "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ ...form, timeLimitSeconds: Number(form.timeLimitSeconds) }) });
      setSavedId(data.id);
      setMessage("Question saved. Add test cases below.");
    } catch (err) { setError(err.message); }
  };

  const addCase = async e => {
    e.preventDefault();
    setError("");
    try {
      await jsonFetch(TESTCASES, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ ...caseForm, questionId: savedId, points: Number(caseForm.points) }) });
      setCaseForm({ input: "", expectedOutput: "", hidden: false, points: 10 });
      setMessage("Test case added.");
      loadCases();
    } catch (err) { setError(err.message); }
  };

  const deleteCase = async id => {
    try { await jsonFetch(`${TESTCASES}/${id}`, { method: "DELETE" }); loadCases(); }
    catch (err) { setError(err.message); }
  };

  return <div className="page">
    <button type="button" className="back-link" onClick={onDone}>← Back to question library</button>
    <form className="panel editor-form" onSubmit={save}>
      <div className="panel-heading"><div><span className="eyebrow">{isNew ? "NEW QUESTION" : "EDIT QUESTION"}</span><h2>{isNew ? "Create coding problem" : form.title}</h2></div><button className="primary-button">Save question</button></div>
      <div className="form-grid two"><Field label="Title" value={form.title} onChange={e => setForm({ ...form, title: e.target.value })}/><label>Language<select value={form.language} onChange={e => setForm({ ...form, language: e.target.value })}><option value="JAVA">Java</option></select></label></div>
      <FieldArea label="Problem description" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} rows={7}/>
      <div className="form-grid two"><FieldArea label="Input description" value={form.inputDescription} onChange={e => setForm({ ...form, inputDescription: e.target.value })}/><FieldArea label="Output description" value={form.outputDescription} onChange={e => setForm({ ...form, outputDescription: e.target.value })}/></div>
      <div className="form-grid two"><FieldArea label="Constraints" value={form.constraints} onChange={e => setForm({ ...form, constraints: e.target.value })}/><label>Time limit (seconds)<input type="number" min="1" value={form.timeLimitSeconds} onChange={e => setForm({ ...form, timeLimitSeconds: e.target.value })}/></label></div>
      {message && <div className="alert success">{message}</div>}{error && <div className="alert error">{error}</div>}
    </form>
    {savedId && <section className="panel case-section"><div className="section-heading"><div><span className="eyebrow">TEST CASES</span><h3>Evaluation cases</h3><p>Samples are visible to candidates; hidden cases stay private.</p></div></div>
      {cases.map((c, i) => <div className="case-card" key={c.id}><div className="case-head"><strong>Case {i + 1}</strong><span className="difficulty">{c.hidden ? "HIDDEN" : "SAMPLE"} · {c.points} pts</span></div><div className="form-grid two"><pre className="case-preview">{c.input}</pre><pre className="case-preview">{c.expectedOutput}</pre></div><div className="case-foot"><button className="danger-button" type="button" onClick={() => deleteCase(c.id)}>Delete</button></div></div>)}
      <form className="case-card add-case" onSubmit={addCase}><div className="case-head"><strong>Add test case</strong><label className="switch"><input type="checkbox" checked={caseForm.hidden} onChange={e => setCaseForm({ ...caseForm, hidden: e.target.checked })}/> Hidden</label></div><div className="form-grid two"><FieldArea label="Input" value={caseForm.input} onChange={e => setCaseForm({ ...caseForm, input: e.target.value })} rows={5}/><FieldArea label="Expected output" value={caseForm.expectedOutput} onChange={e => setCaseForm({ ...caseForm, expectedOutput: e.target.value })} rows={5}/></div><div className="case-foot"><label>Points<input type="number" min="0" value={caseForm.points} onChange={e => setCaseForm({ ...caseForm, points: e.target.value })}/></label><button className="primary-button">Add test case</button></div></form>
    </section>}
  </div>;
}

function AssessmentBuilder() {
  const [questions, setQuestions] = useState([]);
  const [selected, setSelected] = useState([]);
  const [form, setForm] = useState({ title: "", description: "", durationMinutes: 60, studentEmails: "" });
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    let mounted = true;
    async function loadQuestions() {
      try { const data = await jsonFetch(QUESTIONS); if (mounted) setQuestions(Array.isArray(data) ? data : []); }
      catch (e) { if (mounted) setError(e.message); }
    }
    loadQuestions();
    return () => { mounted = false; };
  }, []);

  const toggle = id => setSelected(x => x.includes(id) ? x.filter(v => v !== id) : [...x, id]);
  const create = async e => {
    e.preventDefault();
    try {
      await jsonFetch(ASSESSMENTS, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ title: form.title, description: form.description, durationMinutes: Number(form.durationMinutes), questionIds: selected, studentEmails: form.studentEmails.split(/[,\n]/).map(x => x.trim()).filter(Boolean) }) });
      setMessage("Assessment created and assigned.");
      setForm({ title: "", description: "", durationMinutes: 60, studentEmails: "" });
      setSelected([]);
    } catch (err) { setError(err.message); }
  };

  return <div className="page"><div className="page-intro"><div><span className="eyebrow">ASSESSMENT BUILDER</span><h1>Create an assessment</h1><p>Select questions, set a duration, and assign registered students.</p></div></div><form className="panel assessment-builder" onSubmit={create}><Field label="Assessment title" value={form.title} onChange={e => setForm({ ...form, title: e.target.value })}/><FieldArea label="Description" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })}/><div className="form-grid two"><label>Duration (minutes)<input type="number" min="1" value={form.durationMinutes} onChange={e => setForm({ ...form, durationMinutes: e.target.value })}/></label><FieldArea label="Student emails" value={form.studentEmails} onChange={e => setForm({ ...form, studentEmails: e.target.value })} rows={3}/></div><div className="builder-questions"><div className="section-heading"><div><span className="eyebrow">QUESTION BANK</span><h3>Select questions</h3></div><span>{selected.length} selected</span></div>{questions.map(q => <label className={selected.includes(q.id) ? "select-question selected" : "select-question"} key={q.id}><input type="checkbox" checked={selected.includes(q.id)} onChange={() => toggle(q.id)}/><span><b>{q.title}</b><small>{q.language} · {q.timeLimitSeconds}s</small></span></label>)}</div>{message && <div className="alert success">{message}</div>}{error && <div className="alert error">{error}</div>}<div className="form-footer"><button className="primary-button" disabled={!selected.length}>Create assessment</button></div></form></div>;
}

function StudentAssessments() {
  const [items, setItems] = useState([]);
  const [active, setActive] = useState(null);
  const [openingId, setOpeningId] = useState(null);
  const [error, setError] = useState("");

  const load = async () => {
    try { setItems(await jsonFetch(ASSESSMENTS)); setError(""); }
    catch (e) { setItems([]); setError(e.message); }
  };

  const openAssessment = async assessment => {
    setOpeningId(assessment.id);
    setError("");
    try {
      const attempt = await jsonFetch(`${ASSESSMENTS}/${assessment.id}/attempt`);
      setActive({ assessment, attempt, consent: !attempt, complete: Boolean(attempt?.submittedAt) });
    } catch (e) {
      setError(e.message);
    } finally {
      setOpeningId(null);
    }
  };

  const startAssessment = async assessment => {
    const attempt = await jsonFetch(`${ASSESSMENTS}/${assessment.id}/start`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ consentConfirmed: true }),
    });
    setActive({ assessment, attempt, consent: false, complete: false });
  };

  useEffect(() => { load(); }, []);
  if (active?.complete) return <div className="exam-shell"><section className="panel assessment-complete"><span className="eyebrow">ASSESSMENT COMPLETE</span><h1>{active.assessment.title}</h1><p>Your submitted solutions are saved.</p><button type="button" className="primary-button" onClick={() => setActive(null)}>Return to assessments</button></section></div>;
  if (active?.consent) return <AssessmentConsent assessment={active.assessment} onCancel={() => setActive(null)} onStart={() => startAssessment(active.assessment)}/>;
  if (active?.attempt) return <CodingWorkspace assessment={active.assessment} attempt={active.attempt} onExit={() => setActive(null)}/>;

  return <div className="page"><div className="page-intro row"><div><span className="eyebrow">MY ASSESSMENTS</span><h1>Assigned assessments</h1><p>Complete each assessment inside the timed coding workspace.</p></div><button type="button" className="ghost-button" onClick={load}>Refresh</button></div>{error && <div className="alert error">{error}</div>}{!items.length && !error ? <section className="panel empty-state"><strong>No assessments assigned</strong><span>Your recruiter will assign an assessment to your account.</span></section> : <div className="assessment-grid">{items.map(a => <article className="assessment-card" key={a.id}><div className="assessment-top"><span className="eyebrow">CODING ASSESSMENT</span><span>{a.durationMinutes} min</span></div><h2>{a.title}</h2><p>{a.description || "Technical coding assessment"}</p><div className="assessment-meta"><span>{a.questions?.length || 0} questions</span><span>Java 21</span><span>Timed</span></div><button type="button" className="primary-button" disabled={openingId === a.id} onClick={() => openAssessment(a)}>{openingId === a.id ? "Loading attempt…" : "Open assessment →"}</button></article>)}</div>}</div>;
}

function AssessmentConsent({ assessment, onCancel, onStart }) {
  const [confirmed, setConfirmed] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  const start = async () => {
    setBusy(true);
    setError("");
    try { await onStart(); }
    catch (e) { setError(e.message); }
    finally { setBusy(false); }
  };

  return <div className="page consent-page"><section className="panel consent-card"><span className="eyebrow">BEFORE YOU START</span><h1>{assessment.title}</h1><p>This timed assessment records the following activity and associates it with your submitted solutions:</p><ul><li>Code edit activity, including timestamps and code length</li><li>Paste events in the code editor</li><li>Code execution attempts</li><li>When this browser window loses focus or visibility and when it returns</li></ul><p>Your submitted source code and recorded activity are available to the recruiter for review. These signals are review indicators and do not prove misconduct.</p><label className="consent-check"><input type="checkbox" checked={confirmed} onChange={event => setConfirmed(event.target.checked)}/><span>I have read and agree to this recording notice.</span></label>{error && <div className="alert error">{error}</div>}<div className="form-footer"><button type="button" className="ghost-button" onClick={onCancel}>Back</button><button type="button" className="primary-button" disabled={!confirmed || busy} onClick={start}>{busy ? "Starting…" : "I agree and start"}</button></div></section></div>;
}

function CodingWorkspace({ assessment, attempt, onExit }) {
  const [index, setIndex] = useState(0);
  const [code, setCode] = useState(starterCode);
  const [input, setInput] = useState("");
  const [cases, setCases] = useState([]);
  const [casesLoading, setCasesLoading] = useState(true);
  const [casesError, setCasesError] = useState("");
  const [result, setResult] = useState(null);
  const [seconds, setSeconds] = useState(attempt.remainingSeconds);
  const [graceSeconds, setGraceSeconds] = useState(attempt.graceSecondsRemaining);
  const [events, setEvents] = useState(() => {
    try { return JSON.parse(sessionStorage.getItem(`vericode-events-${assessment.id}`) || "[]"); }
    catch { return []; }
  });
  const [busy, setBusy] = useState(false);
  const [completed, setCompleted] = useState(Boolean(attempt.submittedAt));
  const question = assessment.questions[index];
  const samples = cases.filter(c => !c.hidden);
  const eventStorageKey = `vericode-events-${assessment.id}`;

  const record = useCallback((eventType, details = "") => setEvents(old => {
    const next = [...old, { eventType, occurredAt: new Date().toISOString(), details }].slice(-300);
    try { sessionStorage.setItem(eventStorageKey, JSON.stringify(next)); } catch { /* Storage can be unavailable in private browsing. */ }
    return next;
  }), [eventStorageKey]);

  useEffect(() => {
    let mounted = true;
    async function loadCases() {
      setCasesLoading(true);
      setCasesError("");
      try { const data = await jsonFetch(`${TESTCASES}/question/${question.id}`); if (mounted) setCases(data); }
      catch (e) { if (mounted) { setCases([]); setCasesError(e.message); } }
      finally { if (mounted) setCasesLoading(false); }
      if (mounted) setResult(null);
    }
    loadCases();
    return () => { mounted = false; };
  }, [question.id]);

  useEffect(() => {
    const t = setInterval(() => {
      setSeconds(value => Math.max(0, value - 1));
      setGraceSeconds(value => Math.max(0, value - 1));
    }, 1000);
    return () => clearInterval(t);
  }, []);

  useEffect(() => {
    const blur = () => record("window_blur", "Assessment window lost focus");
    const focus = () => record("window_focus", "Assessment window regained focus");
    const visibility = () => record(document.visibilityState === "hidden" ? "visibility_hidden" : "visibility_visible", document.visibilityState);
    window.addEventListener("blur", blur);
    window.addEventListener("focus", focus);
    document.addEventListener("visibilitychange", visibility);
    return () => { window.removeEventListener("blur", blur); window.removeEventListener("focus", focus); document.removeEventListener("visibilitychange", visibility); };
  }, [record]);

  const run = async custom => {
    setBusy(true); setResult({ loading: true });
    record("code_run", custom ? "Custom input" : "Sample test");
    try {
      const sample = samples[0];
      const data = await jsonFetch(`${EXECUTION}/run`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ sourceCode: code, input: custom ? input : sample?.input || "", expectedOutput: custom ? "" : sample?.expectedOutput || "", timeLimitSeconds: question.timeLimitSeconds }) });
      setResult({ single: data });
    } catch (e) { setResult({ error: e.message }); }
    finally { setBusy(false); }
  };

  const submit = async () => {
    if (graceSeconds <= 0) {
      setResult({ error: "The submission deadline and 10-second grace period have passed." });
      return;
    }
    setBusy(true);
    try {
      const evaluation = await jsonFetch(`${EXECUTION}/evaluate`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ questionId: question.id, sourceCode: code }) });
      const submitEvent = { eventType: "submit", occurredAt: new Date().toISOString(), details: `${evaluation.passed}/${evaluation.total} test cases passed` };
      const submission = await jsonFetch(SUBMISSIONS, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ assessmentId: assessment.id, questionId: question.id, sourceCode: code, behaviorEvents: [...events, submitEvent] }) });
      setEvents([]);
      try { sessionStorage.removeItem(eventStorageKey); } catch { /* Storage can be unavailable in private browsing. */ }
      setResult({ evaluation });
      if (submission.assessmentComplete) {
        setCompleted(true);
      } else if (index < assessment.questions.length - 1) {
        setTimeout(() => { setIndex(index + 1); setCode(starterCode); }, 700);
      }
    } catch (e) { setResult({ error: e.message }); }
    finally { setBusy(false); }
  };

  if (completed) return <div className="exam-shell"><section className="panel assessment-complete"><span className="eyebrow">ASSESSMENT COMPLETE</span><h1>{assessment.title}</h1><p>Your submitted solutions are saved.</p><button type="button" className="primary-button" onClick={onExit}>Return to assessments</button></section></div>;

  const time = `${String(Math.floor(seconds / 60)).padStart(2, "0")}:${String(seconds % 60).padStart(2, "0")}`;
  return <div className="exam-shell"><header className="exam-header"><div className="exam-brand"><div className="brand-mark">VC</div><div><b>{assessment.title}</b><span>Question {index + 1} of {assessment.questions.length}</span></div></div><div className={seconds < 300 ? "timer danger" : "timer"}>◷ {time}</div><button type="button" className="ghost-button dark" onClick={onExit}>Exit</button></header><div className="exam-body"><aside className="question-nav"><span className="eyebrow">QUESTIONS</span>{assessment.questions.map((q, i) => <button type="button" key={q.id} className={i === index ? "question-nav-item active" : "question-nav-item"} onClick={() => setIndex(i)}><span>{i + 1}</span><div><b>{q.title}</b><small>{i === index ? "In progress" : "Open"}</small></div></button>)}</aside><section className="problem-pane"><div className="problem-scroll"><span className="eyebrow">PROBLEM {index + 1}</span><h1>{question.title}</h1><div className="problem-tags"><span>Java</span><span>{question.timeLimitSeconds}s</span></div><div className="statement"><p>{question.description}</p><h3>Input</h3><p>{question.inputDescription}</p><h3>Output</h3><p>{question.outputDescription}</p><h3>Constraints</h3><pre>{question.constraints}</pre>{casesLoading ? <p className="muted">Loading visible examples…</p> : casesError ? <div className="alert error">Could not load test cases: {casesError}</div> : samples.length > 0 ? <><h3>Examples</h3>{samples.map((c, i) => <div className="example" key={i}><div><span>Input</span><pre>{c.input}</pre></div><div><span>Output</span><pre>{c.expectedOutput}</pre></div></div>)}</> : <p className="muted">No visible sample cases were added for this question.</p>}</div></div></section><section className="editor-pane"><div className="editor-top"><div><span className="eyebrow">CODE EDITOR</span><strong>Java 21</strong></div><span className="editor-status">Docker execution</span></div><textarea className="ide" spellCheck="false" value={code} onPaste={() => record("paste", "Paste event in code editor")} onChange={e => { setCode(e.target.value); record("code_edit", `${e.target.value.length} characters`); }}/><div className="console"><div className="console-head"><b>Test results</b><span>{result?.evaluation ? `${result.evaluation.passed}/${result.evaluation.total} passed` : ""}</span></div>{result?.loading ? <div className="console-body">Running your code…</div> : result?.error ? <div className="console-body error-text">{result.error}</div> : result?.evaluation ? <div className="console-body"><strong>{result.evaluation.passed}/{result.evaluation.total} test cases passed</strong><div className="test-result-list">{result.evaluation.results.map((r, i) => <div className={r.passed ? "test-result pass" : "test-result fail"} key={i}><span>{r.passed ? "✓" : "×"}</span>Test case {i + 1}{r.hidden ? " · hidden" : ""}<small>{r.executionTimeMs} ms</small></div>)}</div></div> : result?.single ? <div className="console-body"><strong className={result.single.passed ? "result-pass" : "result-fail"}>{result.single.passed ? "✓ Sample passed" : "× Sample failed"}</strong>{result.single.error && <pre>{result.single.error}</pre>}{result.single.output && <pre>{result.single.output}</pre>}</div> : <div className="console-empty">Run your code to see execution results.</div>}</div><div className="editor-actions"><button type="button" className="ghost-button" disabled={busy || graceSeconds === 0} onClick={() => run(false)}>Run sample</button><button type="button" className="ghost-button" disabled={busy || graceSeconds === 0} onClick={() => run(true)}>Run custom</button><button type="button" className="primary-button" disabled={busy || graceSeconds === 0} onClick={submit}>{busy ? "Evaluating…" : "Submit solution"}</button></div>{seconds === 0 && graceSeconds > 0 && <div className="submission-grace" role="status">Time is up. You have {graceSeconds} seconds to submit.</div>}{seconds === 0 && graceSeconds === 0 && <div className="submission-grace" role="status">The 10-second submission grace period has passed.</div>}<label className="custom-input">Custom input<textarea rows="3" value={input} onChange={e => setInput(e.target.value)} placeholder="Try your own input"/></label></section></div></div>;
}

function Submissions() {
  const [items, setItems] = useState([]);
  const [selected, setSelected] = useState(null);
  const [error, setError] = useState("");
  const load = async () => {
    try { setItems(await jsonFetch(`${SUBMISSIONS}/recruiter`)); setError(""); }
    catch (e) { setItems([]); setError(e.message); }
  };
  useEffect(() => { load(); }, []);
  if (selected) return <div className="page"><button type="button" className="back-link" onClick={() => setSelected(null)}>← Back to submissions</button><SubmissionDetail item={selected}/></div>;
  return <div className="page"><div className="page-intro row"><div><span className="eyebrow">SUBMISSIONS</span><h1>Candidate results</h1><p>Review a submission only when you need the underlying evidence.</p></div><button type="button" className="ghost-button" onClick={load}>Refresh</button></div>{error && <div className="alert error">{error}</div>}<section className="panel">{!items.length && !error ? <div className="empty-state"><strong>No submissions yet</strong><span>Completed coding submissions will appear here.</span></div> : <div className="submission-table"><div className="submission-head"><span>Candidate</span><span>Assessment</span><span>Question</span><span>Submitted</span><span></span></div>{items.map(x => <div className="submission-row" key={x.id}><div><b>{x.studentName}</b><small>{x.studentEmail}</small></div><div>{x.assessmentTitle}</div><div>{x.questionTitle}</div><div>{new Date(x.submittedAt).toLocaleString()}</div><button type="button" className="table-link" onClick={() => setSelected(x)}>View</button></div>)}</div>}</section></div>;
}

function SubmissionDetail({ item: x }) { return <section className="panel submission-detail"><div className="detail-grid"><Metric label="Candidate" value={x.studentName}/><Metric label="Assessment" value={x.assessmentTitle}/><Metric label="Question" value={x.questionTitle}/><Metric label="Submitted" value={new Date(x.submittedAt).toLocaleString()}/></div><span className="eyebrow">SOURCE CODE</span><h2>Submitted solution</h2><pre className="submission-code">{x.sourceCode}</pre><span className="eyebrow">SESSION SUMMARY</span><h2>Activity</h2><div className="detail-grid"><Metric label="Source lines" value={x.sourceLines}/><Metric label="Characters" value={x.sourceCharacters}/><Metric label="Paste events" value={x.eventCounts?.paste || 0}/><Metric label="Focus changes" value={(x.eventCounts?.window_blur || 0) + (x.eventCounts?.visibility_hidden || 0)}/></div></section>; }
function Candidates() { return <div className="page"><div className="page-intro"><span className="eyebrow">CANDIDATES</span><h1>Candidate management</h1><p>Candidate assignments are managed from the assessment builder.</p></div><section className="panel empty-state"><strong>Assignments are assessment-based</strong><span>Create an assessment, select questions, and enter registered student email addresses.</span></section></div>; }

export default App;
