import { useEffect, useState } from "react";
import "./QuestionImport.css";

const API = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
const AUTH_API = `${API}/Auth`;
const QUESTION_API = `${API}/Questions`;
const IMPORT_API = `${API}/QuestionImport/preview`;

const emptyForm = {
  title: "",
  description: "",
  inputDescription: "",
  outputDescription: "",
  constraints: "",
  timeLimitSeconds: 60,
  language: "JAVA",
};

export default function QuestionImport() {
  const [user, setUser] = useState(null);
  const [open, setOpen] = useState(false);
  const [source, setSource] = useState("LEETCODE");
  const [url, setUrl] = useState("");
  const [form, setForm] = useState(emptyForm);
  const [note, setNote] = useState("");
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  useEffect(() => {
    fetch(`${AUTH_API}/me`, { credentials: "include" })
      .then((r) => (r.ok ? r.json() : null))
      .then(setUser)
      .catch(() => setUser(null));
  }, []);

  if (user?.role !== "RECRUITER") return null;

  const update = (event) => {
    setForm({ ...form, [event.target.name]: event.target.value });
    setError("");
    setMessage("");
  };

  const close = () => {
    setOpen(false);
    setUrl("");
    setForm(emptyForm);
    setNote("");
    setError("");
    setMessage("");
  };

  const previewQuestion = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    setMessage("");
    try {
      const response = await fetch(IMPORT_API, {
        method: "POST",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ url }),
      });
      const data = await response.json();
      if (!response.ok) throw new Error(data.message || "Could not import this question.");
      setSource(data.source);
      setForm({
        title: data.title || "",
        description: data.description || "",
        inputDescription: data.inputDescription || "",
        outputDescription: data.outputDescription || "",
        constraints: data.constraints || "",
        timeLimitSeconds: data.timeLimitSeconds || 60,
        language: data.language || "JAVA",
      });
      setNote(data.note || "Review the imported content before saving.");
    } catch (e) {
      setError(e.message || "Import failed.");
    } finally {
      setLoading(false);
    }
  };

  const saveQuestion = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError("");
    setMessage("");
    try {
      const response = await fetch(QUESTION_API, {
        method: "POST",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ...form, timeLimitSeconds: Number(form.timeLimitSeconds) }),
      });
      const data = await response.json();
      if (!response.ok) throw new Error(data.message || "Could not save the question.");
      setMessage(`"${data.title}" was added to your question bank.`);
      setNote("");
    } catch (e) {
      setError(e.message || "Save failed.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <button className="question-import-trigger" type="button" onClick={() => setOpen(true)}>
        <span>↗</span> Import Question
      </button>

      {open && (
        <div className="question-import-backdrop" role="dialog" aria-modal="true">
          <div className="question-import-modal">
            <div className="question-import-header">
              <div>
                <div className="question-import-kicker">QUESTION LIBRARY</div>
                <h2>Import a coding question</h2>
                <p>Bring a problem into VeriCode as an editable draft.</p>
              </div>
              <button className="question-import-close" type="button" onClick={close}>×</button>
            </div>

            <div className="question-source-tabs">
              <button type="button" className={source === "LEETCODE" ? "active" : ""} onClick={() => setSource("LEETCODE")}>LeetCode</button>
              <button type="button" className={source === "HACKERRANK" ? "active" : ""} onClick={() => setSource("HACKERRANK")}>HackerRank</button>
            </div>

            <form onSubmit={previewQuestion}>
              <label className="question-import-label">
                {source === "LEETCODE" ? "LeetCode problem URL" : "HackerRank challenge URL"}
                <input
                  value={url}
                  onChange={(e) => setUrl(e.target.value)}
                  placeholder={source === "LEETCODE" ? "https://leetcode.com/problems/two-sum/" : "https://www.hackerrank.com/challenges/..."}
                  required
                />
              </label>
              <button className="question-import-primary" disabled={loading} type="submit">
                {loading ? "Fetching question…" : "Fetch question"}
              </button>
            </form>

            {error && <div className="question-import-error">{error}</div>}
            {message && <div className="question-import-success">{message}</div>}

            {form.title && (
              <form className="question-import-preview" onSubmit={saveQuestion}>
                <div className="question-import-preview-head">
                  <div>
                    <span className="question-source-badge">{source === "LEETCODE" ? "LEETCODE" : "HACKERRANK"}</span>
                    <h3>Review before adding</h3>
                  </div>
                  <span className="question-draft">DRAFT</span>
                </div>

                {note && <p className="question-import-note">{note}</p>}

                <label className="question-import-label">Question title<input name="title" value={form.title} onChange={update} required /></label>
                <label className="question-import-label">Problem description<textarea name="description" value={form.description} onChange={update} rows="7" required /></label>
                <div className="question-import-grid">
                  <label className="question-import-label">Input description<textarea name="inputDescription" value={form.inputDescription} onChange={update} rows="4" required /></label>
                  <label className="question-import-label">Output description<textarea name="outputDescription" value={form.outputDescription} onChange={update} rows="4" required /></label>
                </div>
                <label className="question-import-label">Constraints<textarea name="constraints" value={form.constraints} onChange={update} rows="4" required /></label>
                <div className="question-import-grid small">
                  <label className="question-import-label">Time limit (seconds)<input type="number" min="1" name="timeLimitSeconds" value={form.timeLimitSeconds} onChange={update} required /></label>
                  <label className="question-import-label">Language<select name="language" value={form.language} onChange={update}><option value="JAVA">Java</option></select></label>
                </div>

                <div className="question-import-footer">
                  <button className="question-import-secondary" type="button" onClick={() => setForm(emptyForm)}>Clear draft</button>
                  <button className="question-import-primary" type="submit" disabled={saving}>{saving ? "Adding…" : "Add to question bank"}</button>
                </div>
              </form>
            )}

            <div className="question-import-footer plain">
              <button className="question-import-secondary" type="button" onClick={close}>Close</button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
