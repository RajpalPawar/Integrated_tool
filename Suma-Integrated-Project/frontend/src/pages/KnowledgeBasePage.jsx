import { useEffect, useState } from "react";
import { BookOpen, FilePlus2, RefreshCw, Search } from "lucide-react";
import {
  createKnowledgeArticle,
  getKnowledgeArticles,
  updateKnowledgeArticle,
} from "../auth/authService.js";

const EMPTY_FORM = { title: "", category: "General", content: "", published: false };

function canManage(user) {
  return user.role === "Super User" || (user.permissions || []).includes("knowledge.manage");
}

export default function KnowledgeBasePage({ user }) {
  const [articles, setArticles] = useState([]);
  const [selected, setSelected] = useState(null);
  const [form, setForm] = useState(null);
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const editable = canManage(user);

  async function load(search = query) {
    setLoading(true);
    setError("");
    try {
      const rows = await getKnowledgeArticles(search.trim());
      setArticles(Array.isArray(rows) ? rows : []);
      setSelected((current) => current && rows.find((article) => article.id === current.id) || null);
    } catch (requestError) {
      setError(requestError.message || "Could not load knowledge articles.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    const timer = setTimeout(() => load(query), 250);
    return () => clearTimeout(timer);
  }, [query]);

  function edit(article) {
    setSelected(article);
    setForm(article
      ? { id: article.id, title: article.title, category: article.category, content: article.content, published: article.published }
      : { ...EMPTY_FORM });
    setError("");
    setNotice("");
  }

  async function save(event) {
    event.preventDefault();
    setSaving(true);
    setError("");
    setNotice("");
    try {
      const { id, ...article } = form;
      const saved = id
        ? await updateKnowledgeArticle(id, article)
        : await createKnowledgeArticle(article);
      setForm(null);
      setSelected(saved);
      setNotice(id ? "Article updated." : "Article created.");
      await load(query);
    } catch (requestError) {
      setError(requestError.message || "Could not save this article.");
    } finally {
      setSaving(false);
    }
  }

  const editForm = form;

  return (
    <div className="d-grid gap-3">
      <section className="panel">
        <div className="panel__head d-flex align-items-center justify-content-between flex-wrap gap-3">
          <div>
            <h2 className="mb-1"><BookOpen size={20} className="me-2" />Knowledge Base</h2>
            <p className="muted mb-0">Search practical guides, FAQs, and troubleshooting steps.</p>
          </div>
          {editable && <button className="btn btn-primary" onClick={() => edit(null)}><FilePlus2 size={16} className="me-2" />New article</button>}
        </div>

        <label className="input-group mt-3" style={{ maxWidth: 520 }}>
          <span className="input-group-text"><Search size={16} /></span>
          <input className="form-control" type="search" maxLength={100} value={query} placeholder="Search articles" onChange={(event) => setQuery(event.target.value)} />
          <button type="button" className="btn btn-outline-secondary" onClick={() => load()} disabled={loading} aria-label="Refresh articles"><RefreshCw size={16} /></button>
        </label>

        {error && <div className="alert alert-danger mt-3 mb-0" role="alert">{error}</div>}
        {notice && <div className="alert alert-success mt-3 mb-0" role="status">{notice}</div>}
      </section>

      {editForm ? (
        <section className="panel">
          <div className="panel__head"><h2>{editForm.id ? "Edit article" : "New article"}</h2></div>
          <form className="d-grid gap-3" onSubmit={save}>
            <label className="field">
              <span>Title</span>
              <input className="form-control" required maxLength={180} value={editForm.title} onChange={(event) => setForm((current) => ({ ...current, title: event.target.value }))} />
            </label>
            <label className="field">
              <span>Category</span>
              <input className="form-control" maxLength={100} value={editForm.category} onChange={(event) => setForm((current) => ({ ...current, category: event.target.value }))} />
            </label>
            <label className="field">
              <span>Article</span>
              <textarea className="form-control" required rows={12} maxLength={50000} value={editForm.content} onChange={(event) => setForm((current) => ({ ...current, content: event.target.value }))} />
            </label>
            <label className="form-check">
              <input className="form-check-input" type="checkbox" checked={editForm.published} onChange={(event) => setForm((current) => ({ ...current, published: event.target.checked }))} />
              <span className="form-check-label">Published and visible to users with Knowledge Base access</span>
            </label>
            <div className="d-flex gap-2">
              <button className="btn btn-primary" type="submit" disabled={saving}>{saving ? "Saving…" : "Save article"}</button>
              <button className="btn btn-outline-secondary" type="button" disabled={saving} onClick={() => setForm(null)}>Cancel</button>
            </div>
          </form>
        </section>
      ) : (
        <div className="row g-3">
          <div className="col-lg-5">
            <section className="panel h-100">
              <div className="panel__head"><h2>Articles</h2></div>
              {loading ? <p className="empty">Loading articles…</p> : articles.length === 0 ? <p className="empty">No articles match this search.</p> : (
                <div className="list-group list-group-flush">
                  {articles.map((article) => (
                    <button
                      type="button"
                      key={article.id}
                      className={`list-group-item list-group-item-action${selected?.id === article.id ? " active" : ""}`}
                      onClick={() => setSelected(article)}
                    >
                      <span className="d-flex justify-content-between gap-2">
                        <strong>{article.title}</strong>
                        {editable && !article.published && <span className="badge text-bg-secondary">Draft</span>}
                      </span>
                      <small className="d-block mt-1">{article.category}</small>
                    </button>
                  ))}
                </div>
              )}
            </section>
          </div>
          <div className="col-lg-7">
            <section className="panel h-100">
              {selected ? (
                <>
                  <div className="panel__head d-flex justify-content-between align-items-start gap-3">
                    <div><span className="badge text-bg-light mb-2">{selected.category}</span><h2>{selected.title}</h2></div>
                    {editable && <button className="btn btn-outline-secondary btn-sm" onClick={() => edit(selected)}>Edit</button>}
                  </div>
                  <p className="muted small mb-3">Updated {new Date(selected.updatedAt).toLocaleString()}{selected.author ? ` · ${selected.author}` : ""}</p>
                  <div style={{ whiteSpace: "pre-wrap", overflowWrap: "anywhere" }}>{selected.content}</div>
                </>
              ) : <p className="empty">Select an article to read it.</p>}
            </section>
          </div>
        </div>
      )}
    </div>
  );
}
