import { useCallback, useEffect, useState } from "react";
import { apiRequest } from "../auth/authService.js";

const EMPTY_FORM = {
  employeeId: "",
  fullName: "",
  department: "",
  jobTitle: "",
  workEmail: "",
  status: "ACTIVE",
  monthlySalary: "",
  linkedUserId: "",
};

function localDateValue() {
  const date = new Date();
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function formatTime(value) {
  if (!value) return "—";
  const [hour, minute] = value.split(":");
  const date = new Date();
  date.setHours(Number(hour), Number(minute), 0, 0);
  return date.toLocaleTimeString([], { hour: "numeric", minute: "2-digit" });
}

const formatINR = (amount) =>
  new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 2,
  }).format(Number(amount));

export default function EmployeesPage() {
  const [directory, setDirectory] = useState({
    employeeCount: 0,
    activeEmployeeCount: 0,
    employees: [],
  });
  const [form, setForm] = useState(EMPTY_FORM);
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [accountOptions, setAccountOptions] = useState([]);
  const [accountsLoading, setAccountsLoading] = useState(false);
  const [attendanceDate, setAttendanceDate] = useState(localDateValue);
  const [attendanceRows, setAttendanceRows] = useState([]);
  const [attendanceLoading, setAttendanceLoading] = useState(true);
  const [attendanceError, setAttendanceError] = useState("");

  const loadDirectory = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const result = await apiRequest("/api/employees");
      setDirectory({
        employeeCount: result.employeeCount || 0,
        activeEmployeeCount: result.activeEmployeeCount || 0,
        employees: Array.isArray(result.employees) ? result.employees : [],
      });
    } catch (requestError) {
      setError(requestError.message || "Unable to load the employee directory.");
    } finally {
      setLoading(false);
    }
  }, []);

  const loadAccountOptions = useCallback(async (employeeId) => {
    setAccountsLoading(true);
    try {
      const query = employeeId ? `?employeeId=${encodeURIComponent(employeeId)}` : "";
      const result = await apiRequest(`/api/employees/attendance-accounts${query}`);
      setAccountOptions(Array.isArray(result) ? result : []);
    } catch (requestError) {
      setError(requestError.message || "Unable to load employee login accounts.");
    } finally {
      setAccountsLoading(false);
    }
  }, []);

  const loadAttendance = useCallback(async () => {
    setAttendanceLoading(true);
    setAttendanceError("");
    try {
      const result = await apiRequest(
        `/api/employees/attendance?date=${encodeURIComponent(attendanceDate)}`
      );
      setAttendanceRows(Array.isArray(result) ? result : []);
    } catch (requestError) {
      setAttendanceError(requestError.message || "Unable to load employee attendance.");
    } finally {
      setAttendanceLoading(false);
    }
  }, [attendanceDate]);

  useEffect(() => {
    loadDirectory();
  }, [loadDirectory]);

  useEffect(() => {
    loadAccountOptions();
  }, [loadAccountOptions]);

  useEffect(() => {
    loadAttendance();
  }, [loadAttendance]);

  function updateForm(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  }

  function beginEdit(employee) {
    setEditingId(employee.id);
    setForm({
      employeeId: employee.employeeId,
      fullName: employee.fullName,
      department: employee.department,
      jobTitle: employee.jobTitle,
      workEmail: employee.workEmail || "",
      status: employee.status,
      monthlySalary: String(employee.monthlySalary),
      linkedUserId: employee.linkedUserId ? String(employee.linkedUserId) : "",
    });
    loadAccountOptions(employee.id);
    setMessage("");
    setError("");
  }

  function cancelEdit() {
    setEditingId(null);
    setForm(EMPTY_FORM);
    setError("");
    loadAccountOptions();
  }

  async function saveEmployee(event) {
    event.preventDefault();
    setSaving(true);
    setError("");
    setMessage("");
    const payload = {
      ...form,
      workEmail: form.workEmail.trim() || null,
      linkedUserId: form.linkedUserId ? Number(form.linkedUserId) : null,
    };
    try {
      await apiRequest(
        editingId ? `/api/employees/${encodeURIComponent(editingId)}` : "/api/employees",
        {
          method: editingId ? "PUT" : "POST",
          body: JSON.stringify(payload),
        }
      );
      setMessage(editingId ? "Employee record updated." : "Employee added.");
      setEditingId(null);
      setForm(EMPTY_FORM);
      await Promise.all([loadDirectory(), loadAccountOptions(), loadAttendance()]);
    } catch (requestError) {
      setError(requestError.message || "Unable to save the employee record.");
    } finally {
      setSaving(false);
    }
  }

  async function removeEmployee(employee) {
    if (!window.confirm(`Remove ${employee.fullName} from the employee directory?`)) return;
    setDeletingId(employee.id);
    setError("");
    setMessage("");
    try {
      await apiRequest(`/api/employees/${encodeURIComponent(employee.id)}`, {
        method: "DELETE",
      });
      if (editingId === employee.id) cancelEdit();
      setMessage("Employee removed.");
      await Promise.all([loadDirectory(), loadAccountOptions(), loadAttendance()]);
    } catch (requestError) {
      setError(requestError.message || "Unable to remove the employee.");
    } finally {
      setDeletingId(null);
    }
  }

  return (
    <div className="d-grid gap-4">
      <section className="panel">
        <div className="panel__head">
          <h2>Company employees</h2>
          <p className="muted mb-0">Employee and salary information is restricted to Manager accounts.</p>
        </div>
        {error && <div className="alert alert-danger mt-3" role="alert">{error}</div>}
        {message && <div className="alert alert-success mt-3" role="status">{message}</div>}
        <div className="row g-3 mt-1">
          <div className="col-sm-6">
            <div className="border rounded-3 p-3 h-100">
              <div className="muted small mb-2">Total employees</div>
              <div className="fs-3 fw-semibold">{directory.employeeCount}</div>
            </div>
          </div>
          <div className="col-sm-6">
            <div className="border rounded-3 p-3 h-100">
              <div className="muted small mb-2">Active employees</div>
              <div className="fs-3 fw-semibold">{directory.activeEmployeeCount}</div>
            </div>
          </div>
        </div>
      </section>

      <section className="panel">
        <div className="panel__head">
          <h2>{editingId ? "Edit employee" : "Add employee"}</h2>
        </div>
        <form className="row g-3" onSubmit={saveEmployee}>
          <div className="col-md-4">
            <label className="form-label" htmlFor="employeeId">Employee ID</label>
            <input id="employeeId" name="employeeId" className="form-control" maxLength="40" required value={form.employeeId} onChange={updateForm} />
          </div>
          <div className="col-md-4">
            <label className="form-label" htmlFor="fullName">Full name</label>
            <input id="fullName" name="fullName" className="form-control" maxLength="120" required value={form.fullName} onChange={updateForm} />
          </div>
          <div className="col-md-4">
            <label className="form-label" htmlFor="department">Department</label>
            <input id="department" name="department" className="form-control" maxLength="120" required value={form.department} onChange={updateForm} />
          </div>
          <div className="col-md-4">
            <label className="form-label" htmlFor="jobTitle">Job title</label>
            <input id="jobTitle" name="jobTitle" className="form-control" maxLength="120" required value={form.jobTitle} onChange={updateForm} />
          </div>
          <div className="col-md-4">
            <label className="form-label" htmlFor="workEmail">Work email</label>
            <input id="workEmail" name="workEmail" type="email" className="form-control" maxLength="254" value={form.workEmail} onChange={updateForm} />
          </div>
          <div className="col-md-4">
            <label className="form-label" htmlFor="status">Employment status</label>
            <select id="status" name="status" className="form-select" value={form.status} onChange={updateForm}>
              <option value="ACTIVE">Active</option>
              <option value="INACTIVE">Inactive</option>
            </select>
          </div>
          <div className="col-md-4">
            <label className="form-label" htmlFor="monthlySalary">Monthly gross salary (INR)</label>
            <input id="monthlySalary" name="monthlySalary" type="number" className="form-control" min="0.01" max="999999999999.99" step="0.01" required value={form.monthlySalary} onChange={updateForm} />
            <div className="form-text">Annual gross salary is calculated as monthly salary × 12.</div>
          </div>
          <div className="col-md-8">
            <label className="form-label" htmlFor="linkedUserId">Employee login account</label>
            <select id="linkedUserId" name="linkedUserId" className="form-select" value={form.linkedUserId} onChange={updateForm} disabled={accountsLoading}>
              <option value="">{accountsLoading ? "Loading accounts…" : "Not linked"}</option>
              {accountOptions.map((account) => (
                <option key={account.id} value={account.id}>
                  {account.fullName} — {account.username} ({account.role.replaceAll("_", " ")})
                </option>
              ))}
            </select>
            <div className="form-text">Link an existing account to enable self-service check-in, check-out, and breaks.</div>
          </div>
          <div className="col-12 d-flex gap-2">
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? "Saving…" : editingId ? "Save changes" : "Add employee"}
            </button>
            {editingId && <button type="button" className="btn btn-outline-secondary" onClick={cancelEdit} disabled={saving}>Cancel</button>}
          </div>
        </form>
      </section>

      <section className="panel">
        <div className="panel__head"><h2>Employee directory and salaries</h2></div>
        {loading ? <p className="empty">Loading employee directory…</p> : directory.employees.length === 0 ? (
          <p className="empty">No employee records yet. Add the company roster above.</p>
        ) : (
          <div className="table-responsive">
            <table className="table align-middle mb-0">
              <thead>
                <tr>
                  <th>Employee ID</th><th>Name</th><th>Department</th><th>Job title</th>
                  <th>Login account</th><th>Status</th><th>Monthly gross</th><th>Annual gross</th><th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {directory.employees.map((employee) => (
                  <tr key={employee.id}>
                    <td>{employee.employeeId}</td>
                    <td>{employee.fullName}{employee.workEmail && <div className="small muted">{employee.workEmail}</div>}</td>
                    <td>{employee.department}</td>
                    <td>{employee.jobTitle}</td>
                    <td>{employee.linkedUsername || "Not linked"}</td>
                    <td>{employee.status === "ACTIVE" ? "Active" : "Inactive"}</td>
                    <td>{formatINR(employee.monthlySalary)}</td>
                    <td>{formatINR(employee.annualSalary)}</td>
                    <td>
                      <div className="d-flex gap-2">
                        <button type="button" className="btn btn-sm btn-outline-primary" onClick={() => beginEdit(employee)}>Edit</button>
                        <button type="button" className="btn btn-sm btn-outline-danger" onClick={() => removeEmployee(employee)} disabled={deletingId === employee.id}>
                          {deletingId === employee.id ? "Removing…" : "Remove"}
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

      <section className="panel">
        <div className="panel__head d-flex align-items-center justify-content-between flex-wrap gap-3">
          <div>
            <h2>Employee attendance</h2>
            <p className="muted mb-0">Only Managers can view team check-in, check-out, and break records.</p>
          </div>
          <label className="field mb-0">
            <span className="form-label">Work date</span>
            <input type="date" className="form-control" value={attendanceDate} onChange={(event) => setAttendanceDate(event.target.value)} />
          </label>
        </div>
        {attendanceError && <div className="alert alert-danger mt-3" role="alert">{attendanceError}</div>}
        {attendanceLoading ? <p className="empty">Loading attendance…</p> : attendanceRows.length === 0 ? (
          <p className="empty">No active employees to show.</p>
        ) : (
          <div className="table-responsive">
            <table className="table align-middle mb-0">
              <thead>
                <tr><th>Employee</th><th>Login</th><th>Check-in</th><th>Check-out</th><th>Break intervals</th><th>Status</th></tr>
              </thead>
              <tbody>
                {attendanceRows.map((row) => (
                  <tr key={row.employeeRecordId}>
                    <td><strong>{row.employeeName}</strong><div className="small muted">{row.employeeId}</div></td>
                    <td>{row.linkedUsername || "Not linked"}</td>
                    <td>{formatTime(row.checkIn)}</td>
                    <td>{formatTime(row.checkOut)}</td>
                    <td>
                      {!row.linkedUsername ? "Link account to enable attendance" : row.breaks.length === 0 ? "No breaks" : (
                        <div>
                          {row.breaks.map((breakRecord) => (
                            <div key={breakRecord.id}>
                              {formatTime(breakRecord.startedAt)}–{breakRecord.endedAt ? formatTime(breakRecord.endedAt) : "In progress"}
                              {" · "}{breakRecord.active ? "In progress" : `${breakRecord.durationMinutes} min`}
                            </div>
                          ))}
                          <strong className="small">
                            Total: {row.breaks.reduce((total, item) => total + item.durationMinutes, 0)} min
                          </strong>
                        </div>
                      )}
                    </td>
                    <td>{!row.linkedUsername ? "Not linked" : row.checkOut ? "Completed" : row.checkIn ? "Checked in" : "Not checked in"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}
