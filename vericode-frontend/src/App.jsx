import { useCallback, useEffect, useState } from "react";
import "./App.css";

const API_URL = "http://localhost:8080/Candidates";

const emptyForm = {
  name: "",
  email: "",
};

function App() {
  const [candidates, setCandidates] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const loadCandidates = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const response = await fetch(API_URL);

      if (!response.ok) {
        throw new Error("Unable to load candidates.");
      }

      const data = await response.json();
      setCandidates(data);
    } catch (err) {
      setError(err.message || "Unable to connect to the backend.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadCandidates();
  }, [loadCandidates]);

  const handleChange = (event) => {
    setForm({
      ...form,
      [event.target.name]: event.target.value,
    });
    setMessage("");
    setError("");
  };

  const resetForm = () => {
    setForm(emptyForm);
    setEditingId(null);
    setMessage("");
    setError("");
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setMessage("");
    setError("");

    const isEditing = editingId !== null;
    const url = isEditing ? `${API_URL}/${editingId}` : API_URL;

    try {
      const response = await fetch(url, {
        method: isEditing ? "PUT" : "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(form),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || "Unable to save candidate.");
      }

      setMessage(isEditing ? "Candidate updated successfully." : "Candidate created successfully.");
      setForm(emptyForm);
      setEditingId(null);
      await loadCandidates();
    } catch (err) {
      setError(err.message || "Something went wrong.");
    } finally {
      setSaving(false);
    }
  };

  const handleEdit = (candidate) => {
    setEditingId(candidate.id);
    setForm({
      name: candidate.name,
      email: candidate.email,
    });
    setMessage("");
    setError("");
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleDelete = async (id) => {
    const confirmed = window.confirm("Delete this candidate?");

    if (!confirmed) {
      return;
    }

    setMessage("");
    setError("");

    try {
      const response = await fetch(`${API_URL}/${id}`, {
        method: "DELETE",
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || "Unable to delete candidate.");
      }

      setMessage("Candidate deleted successfully.");

      if (editingId === id) {
        resetForm();
      }

      await loadCandidates();
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

        <div className="api-status">
          <span className="status-dot" />
          Backend API
        </div>
      </header>

      <main className="dashboard">
        <section className="hero">
          <div>
            <p className="eyebrow">ADMIN CONSOLE</p>
            <h2>Candidate Management</h2>
            <p className="hero-copy">
              Manage candidate records through the Spring Boot REST API.
            </p>
          </div>

          <div className="stat-card">
            <span>Total candidates</span>
            <strong>{candidates.length}</strong>
          </div>
        </section>

        <div className="content-grid">
          <section className="panel form-panel">
            <div className="panel-heading">
              <div>
                <p className="section-label">{editingId ? "EDIT RECORD" : "NEW RECORD"}</p>
                <h3>{editingId ? "Update candidate" : "Add candidate"}</h3>
              </div>
              {editingId && (
                <button className="text-button" type="button" onClick={resetForm}>
                  Cancel
                </button>
              )}
            </div>

            <form onSubmit={handleSubmit}>
              <label>
                Full name
                <input
                  name="name"
                  value={form.name}
                  onChange={handleChange}
                  placeholder="Enter candidate name"
                  required
                />
              </label>

              <label>
                Email address
                <input
                  type="email"
                  name="email"
                  value={form.email}
                  onChange={handleChange}
                  placeholder="candidate@example.com"
                  required
                />
              </label>

              <button className="primary-button" type="submit" disabled={saving}>
                {saving
                  ? "Saving..."
                  : editingId
                    ? "Update candidate"
                    : "Create candidate"}
              </button>
            </form>

            {message && <div className="alert success">{message}</div>}
            {error && <div className="alert error">{error}</div>}
          </section>

          <section className="panel table-panel">
            <div className="panel-heading">
              <div>
                <p className="section-label">DIRECTORY</p>
                <h3>Candidates</h3>
              </div>
              <button className="refresh-button" type="button" onClick={loadCandidates}>
                Refresh
              </button>
            </div>

            {loading ? (
              <div className="empty-state">Loading candidates...</div>
            ) : candidates.length === 0 ? (
              <div className="empty-state">
                <strong>No candidates yet</strong>
                <span>Create your first candidate using the form.</span>
              </div>
            ) : (
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Candidate</th>
                      <th>Email</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {candidates.map((candidate) => (
                      <tr key={candidate.id}>
                        <td>
                          <span className="id-badge">#{candidate.id}</span>
                        </td>
                        <td className="candidate-name">{candidate.name}</td>
                        <td>{candidate.email}</td>
                        <td>
                          <div className="actions">
                            <button
                              className="action-button edit"
                              type="button"
                              onClick={() => handleEdit(candidate)}
                            >
                              Edit
                            </button>
                            <button
                              className="action-button delete"
                              type="button"
                              onClick={() => handleDelete(candidate.id)}
                            >
                              Delete
                            </button>
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
      </main>
    </div>
  );
}

export default App;
