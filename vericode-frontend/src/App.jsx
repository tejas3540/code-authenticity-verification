import { useCallback, useEffect, useState } from "react";
import "./App.css";

const CANDIDATE_API = "http://localhost:8080/Candidates";
const QUESTION_API = "http://localhost:8080/Questions";

const emptyCandidateForm = {
  name: "",
  email: "",
};

const emptyQuestionForm = {
  title: "",
  description: "",
  inputDescription: "",
  outputDescription: "",
  constraints: "",
  timeLimitSeconds: 60,
  language: "JAVA",
};

function App() {
  const [candidates, setCandidates] = useState([]);
  const [questions, setQuestions] = useState([]);
  const [candidateForm, setCandidateForm] = useState(emptyCandidateForm);
  const [questionForm, setQuestionForm] = useState(emptyQuestionForm);
  const [editingCandidateId, setEditingCandidateId] = useState(null);
  const [editingQuestionId, setEditingQuestionId] = useState(null);
  const [loadingCandidates, setLoadingCandidates] = useState(true);
  const [loadingQuestions, setLoadingQuestions] = useState(true);
  const [savingCandidate, setSavingCandidate] = useState(false);
  const [savingQuestion, setSavingQuestion] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const loadCandidates = useCallback(async () => {
    setLoadingCandidates(true);

    try {
      const response = await fetch(CANDIDATE_API);

      if (!response.ok) {
        throw new Error("Unable to load candidates.");
      }

      setCandidates(await response.json());
    } catch (err) {
      setError(err.message || "Unable to connect to the backend.");
    } finally {
      setLoadingCandidates(false);
    }
  }, []);

  const loadQuestions = useCallback(async () => {
    setLoadingQuestions(true);

    try {
      const response = await fetch(QUESTION_API);

      if (!response.ok) {
        throw new Error("Unable to load questions.");
      }

      setQuestions(await response.json());
    } catch (err) {
      setError(err.message || "Unable to load questions.");
    } finally {
      setLoadingQuestions(false);
    }
  }, []);

  useEffect(() => {
    loadCandidates();
    loadQuestions();
  }, [loadCandidates, loadQuestions]);

  const handleCandidateChange = (event) => {
    setCandidateForm({
      ...candidateForm,
      [event.target.name]: event.target.value,
    });
    setMessage("");
    setError("");
  };

  const handleQuestionChange = (event) => {
    setQuestionForm({
      ...questionForm,
      [event.target.name]: event.target.value,
    });
    setMessage("");
    setError("");
  };

  const resetCandidateForm = () => {
    setCandidateForm(emptyCandidateForm);
    setEditingCandidateId(null);
  };

  const resetQuestionForm = () => {
    setQuestionForm(emptyQuestionForm);
    setEditingQuestionId(null);
  };

  const handleCandidateSubmit = async (event) => {
    event.preventDefault();
    setSavingCandidate(true);
    setMessage("");
    setError("");

    const isEditing = editingCandidateId !== null;
    const url = isEditing
      ? `${CANDIDATE_API}/${editingCandidateId}`
      : CANDIDATE_API;

    try {
      const response = await fetch(url, {
        method: isEditing ? "PUT" : "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(candidateForm),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || "Unable to save candidate.");
      }

      setMessage(
        isEditing
          ? "Candidate updated successfully."
          : "Candidate created successfully."
      );
      resetCandidateForm();
      await loadCandidates();
    } catch (err) {
      setError(err.message || "Something went wrong.");
    } finally {
      setSavingCandidate(false);
    }
  };

  const handleQuestionSubmit = async (event) => {
    event.preventDefault();
    setSavingQuestion(true);
    setMessage("");
    setError("");

    const isEditing = editingQuestionId !== null;
    const url = isEditing
      ? `${QUESTION_API}/${editingQuestionId}`
      : QUESTION_API;

    try {
      const response = await fetch(url, {
        method: isEditing ? "PUT" : "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          ...questionForm,
          timeLimitSeconds: Number(questionForm.timeLimitSeconds),
        }),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || "Unable to save question.");
      }

      setMessage(
        isEditing
          ? "Question updated successfully."
          : "Question created successfully."
      );
      resetQuestionForm();
      await loadQuestions();
    } catch (err) {
      setError(err.message || "Something went wrong.");
    } finally {
      setSavingQuestion(false);
    }
  };

  const handleEditCandidate = (candidate) => {
    setEditingCandidateId(candidate.id);
    setCandidateForm({
      name: candidate.name,
      email: candidate.email,
    });
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleEditQuestion = (question) => {
    setEditingQuestionId(question.id);
    setQuestionForm({
      title: question.title,
      description: question.description,
      inputDescription: question.inputDescription,
      outputDescription: question.outputDescription,
      constraints: question.constraints,
      timeLimitSeconds: question.timeLimitSeconds,
      language: question.language,
    });
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleDeleteCandidate = async (id) => {
    if (!window.confirm("Delete this candidate?")) return;

    try {
      const response = await fetch(`${CANDIDATE_API}/${id}`, {
        method: "DELETE",
      });
      const data = await response.json();

      if (!response.ok) throw new Error(data.message || "Unable to delete candidate.");

      setMessage("Candidate deleted successfully.");
      await loadCandidates();
    } catch (err) {
      setError(err.message || "Something went wrong.");
    }
  };

  const handleDeleteQuestion = async (id) => {
    if (!window.confirm("Delete this question?")) return;

    try {
      const response = await fetch(`${QUESTION_API}/${id}`, {
        method: "DELETE",
      });
      const data = await response.json();

      if (!response.ok) throw new Error(data.message || "Unable to delete question.");

      setMessage("Question deleted successfully.");
      if (editingQuestionId === id) resetQuestionForm();
      await loadQuestions();
    } catch (err) {
      setError(err.message || "Something went wrong.");
    }
  };

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand">
          <div className="brand-mark">VC</div>
          <div>
            <h1>VeriCode</h1>
            <span>Code Authenticity Verification System</span>
          </div>
        </div>
        <div className="api-status"><span className="status-dot" />Backend API</div>
      </header>

      <main className="dashboard">
        <section className="hero">
          <div>
            <p className="eyebrow">ADMIN CONSOLE</p>
            <h2>Question Management</h2>
            <p className="hero-copy">Create and manage coding questions for the verification system.</p>
          </div>
          <div className="stat-card">
            <span>Total questions</span>
            <strong>{questions.length}</strong>
          </div>
        </section>

        {(message || error) && (
          <div className={`alert ${message ? "success" : "error"}`}>
            {message || error}
          </div>
        )}

        <div className="content-grid">
          <section className="panel form-panel">
            <div className="panel-heading">
              <div>
                <p className="section-label">{editingQuestionId ? "EDIT QUESTION" : "NEW QUESTION"}</p>
                <h3>{editingQuestionId ? "Update question" : "Create question"}</h3>
              </div>
              {editingQuestionId && (
                <button className="text-button" type="button" onClick={resetQuestionForm}>Cancel</button>
              )}
            </div>

            <form onSubmit={handleQuestionSubmit}>
              <label>
                Question title
                <input name="title" value={questionForm.title} onChange={handleQuestionChange} placeholder="e.g. Two Sum" required />
              </label>
              <label>
                Problem description
                <textarea name="description" value={questionForm.description} onChange={handleQuestionChange} placeholder="Describe the problem" rows="4" required />
              </label>
              <label>
                Input description
                <textarea name="inputDescription" value={questionForm.inputDescription} onChange={handleQuestionChange} placeholder="Describe the input" rows="3" required />
              </label>
              <label>
                Output description
                <textarea name="outputDescription" value={questionForm.outputDescription} onChange={handleQuestionChange} placeholder="Describe the output" rows="3" required />
              </label>
              <label>
                Constraints
                <textarea name="constraints" value={questionForm.constraints} onChange={handleQuestionChange} placeholder="Enter constraints" rows="3" required />
              </label>
              <label>
                Time limit (seconds)
                <input type="number" min="1" name="timeLimitSeconds" value={questionForm.timeLimitSeconds} onChange={handleQuestionChange} required />
              </label>
              <label>
                Language
                <select name="language" value={questionForm.language} onChange={handleQuestionChange}>
                  <option value="JAVA">Java</option>
                </select>
              </label>
              <button className="primary-button" type="submit" disabled={savingQuestion}>
                {savingQuestion ? "Saving..." : editingQuestionId ? "Update question" : "Create question"}
              </button>
            </form>
          </section>

          <section className="panel table-panel">
            <div className="panel-heading">
              <div>
                <p className="section-label">QUESTION BANK</p>
                <h3>Coding questions</h3>
              </div>
              <button className="refresh-button" type="button" onClick={loadQuestions}>Refresh</button>
            </div>

            {loadingQuestions ? (
              <div className="empty-state">Loading questions...</div>
            ) : questions.length === 0 ? (
              <div className="empty-state">
                <strong>No questions yet</strong>
                <span>Create the first coding question using the form.</span>
              </div>
            ) : (
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr><th>ID</th><th>Question</th><th>Language</th><th>Time</th><th>Actions</th></tr>
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
                            <button className="action-button edit" type="button" onClick={() => handleEditQuestion(question)}>Edit</button>
                            <button className="action-button delete" type="button" onClick={() => handleDeleteQuestion(question.id)}>Delete</button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        </div>

        <section className="panel table-panel">
          <div className="panel-heading">
            <div>
              <p className="section-label">CANDIDATE DIRECTORY</p>
              <h3>Candidates</h3>
            </div>
            <div className="actions">
              <span>{candidates.length} candidate{candidates.length === 1 ? "" : "s"}</span>
              <button className="refresh-button" type="button" onClick={loadCandidates}>Refresh</button>
            </div>
          </div>

          {loadingCandidates ? (
            <div className="empty-state">Loading candidates...</div>
          ) : candidates.length === 0 ? (
            <div className="empty-state">No candidates yet.</div>
          ) : (
            <div className="table-wrap">
              <table>
                <thead><tr><th>ID</th><th>Candidate</th><th>Email</th><th>Actions</th></tr></thead>
                <tbody>
                  {candidates.map((candidate) => (
                    <tr key={candidate.id}>
                      <td><span className="id-badge">#{candidate.id}</span></td>
                      <td className="candidate-name">{candidate.name}</td>
                      <td>{candidate.email}</td>
                      <td>
                        <div className="actions">
                          <button className="action-button edit" type="button" onClick={() => handleEditCandidate(candidate)}>Edit</button>
                          <button className="action-button delete" type="button" onClick={() => handleDeleteCandidate(candidate.id)}>Delete</button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </main>
    </div>
  );
}

export default App;
