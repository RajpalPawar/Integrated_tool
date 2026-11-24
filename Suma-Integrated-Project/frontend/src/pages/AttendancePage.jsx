import { useCallback, useEffect, useState } from "react";
import { Clock3, LogIn, LogOut, RefreshCw } from "lucide-react";
import { apiRequest } from "../auth/authService.js";

function formatTime(value) {
  if (!value) return "—";
  const [hour, minute] = value.split(":");
  const date = new Date();
  date.setHours(Number(hour), Number(minute), 0, 0);
  return date.toLocaleTimeString([], { hour: "numeric", minute: "2-digit" });
}

export default function AttendancePage({ user }) {
  const [today, setToday] = useState(null);
  const [history, setHistory] = useState([]);
  const [team, setTeam] = useState([]);
  const [schedules, setSchedules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [scheduleBusy, setScheduleBusy] = useState(null);
  const canManage = user.role === "Super User" || (user.permissions || []).includes("attendance.manage");

  const load = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const [current, mine] = await Promise.all([
        apiRequest("/api/attendance/today"),
        apiRequest("/api/attendance/my"),
      ]);
      setToday(current);
      setHistory(Array.isArray(mine) ? mine : []);
      if (canManage) {
        const [teamRows, scheduleRows] = await Promise.all([
          apiRequest("/api/attendance/team"),
          apiRequest("/api/attendance/schedules"),
        ]);
        setTeam(Array.isArray(teamRows) ? teamRows : []);
        setSchedules(Array.isArray(scheduleRows) ? scheduleRows : []);
      }
    } catch (err) {
      setError(err.message || "Unable to load attendance.");
    } finally {
      setLoading(false);
    }
  }, [canManage]);

  useEffect(() => { load(); }, [load]);

  async function submit(action, body) {
    setBusy(true);
    setError("");
    setMessage("");
    try {
      const updated = await apiRequest(`/api/attendance/${action}`, {
        method: "POST",
        ...(body ? { body: JSON.stringify(body) } : {}),
      });
      setToday(updated);
      const messages = {
        "check-in": "Your check-in has been recorded.",
        "check-out": "Your check-out has been recorded.",
        "break-start": "Your break has started.",
        "break-end": "Your break has ended.",
      };
      setMessage(messages[action]);
      await load();
    } catch (err) {
      setError(err.message || "Unable to update attendance.");
    } finally {
      setBusy(false);
    }
  }

  async function saveSchedule(row, shiftMinutes, breakMinutes) {
    setScheduleBusy(row.userId);
    setError("");
    setMessage("");
    try {
      const updated = await apiRequest(`/api/attendance/schedules/${row.userId}`, {
        method: "PUT",
        body: JSON.stringify({ shiftMinutes: Number(shiftMinutes), breakMinutes: Number(breakMinutes) }),
      });
      setSchedules((current) => current.map((item) => item.userId === updated.userId ? updated : item));
      setMessage(`Schedule updated for ${updated.employeeName}.`);
    } catch (err) {
      setError(err.message || "Unable to update the attendance schedule.");
    } finally {
      setScheduleBusy(null);
    }
  }

  const checkedIn = Boolean(today?.checkIn);
  const checkedOut = Boolean(today?.checkOut);
  const activeBreak = today?.breaks?.find((item) => item.active);
  const todayLabel = new Date().toLocaleDateString(undefined, { weekday: "long", day: "numeric", month: "long", year: "numeric" });

  return (
    <div className="d-grid gap-4">
      <section className="panel">
        <div className="panel__head d-flex align-items-center justify-content-between flex-wrap gap-2">
          <div>
            <h2 className="mb-1">My attendance</h2>
            <p className="muted mb-0">{todayLabel}</p>
          </div>
          <button className="btn btn-outline-secondary" onClick={load} disabled={loading || busy}>
            <RefreshCw size={16} className="me-2" />Refresh
          </button>
        </div>
        {error && <div className="alert alert-danger mt-3" role="alert">{error}</div>}
        {message && <div className="alert alert-success mt-3" role="status">{message}</div>}
        <div className="row g-3 mt-1">
          <div className="col-md-4">
            <div className="border rounded-3 p-3 h-100">
              <div className="muted small mb-2">Check-in</div>
              <div className="fs-4 fw-semibold">{formatTime(today?.checkIn)}</div>
            </div>
          </div>
          <div className="col-md-4">
            <div className="border rounded-3 p-3 h-100">
              <div className="muted small mb-2">Check-out</div>
              <div className="fs-4 fw-semibold">{formatTime(today?.checkOut)}</div>
            </div>
          </div>
          <div className="col-md-4">
            <div className="border rounded-3 p-3 h-100">
              <div className="muted small mb-2">Status</div>
              <div className="fs-5 fw-semibold">{checkedOut ? "Completed" : checkedIn ? "Checked in" : "Not checked in"}</div>
            </div>
          </div>
        </div>
        <div className="d-flex flex-wrap gap-2 mt-4">
          <button className="btn btn-primary" disabled={loading || busy || checkedIn} onClick={() => submit("check-in")}>
            <LogIn size={16} className="me-2" />{busy ? "Please wait…" : "Check in"}
          </button>
          <button className="btn btn-outline-primary" disabled={loading || busy || !checkedIn || checkedOut || Boolean(activeBreak)} onClick={() => submit("check-out")}>
            <LogOut size={16} className="me-2" />Check out
          </button>
          {checkedIn && !checkedOut && (
            <button
              className="btn btn-outline-secondary"
              disabled={loading || busy}
              onClick={() => activeBreak ? submit("break-end") : submit("break-start", { breakType: "GENERAL" })}
            >
              {activeBreak ? "End break" : "Start break"}
            </button>
          )}
        </div>
        {today?.breaks?.length > 0 && (
          <div className="table-responsive mt-4">
            <table className="table table-sm align-middle mb-0">
              <thead><tr><th>Break</th><th>Started</th><th>Ended</th><th>Duration</th><th>Status</th></tr></thead>
              <tbody>{today.breaks.map((item) => <tr key={item.id}>
                <td>{item.breakType}</td><td>{formatTime(item.startedAt)}</td><td>{formatTime(item.endedAt)}</td>
                <td>{item.active ? "In progress" : `${item.durationMinutes} min`}</td><td>{item.active ? "Active" : "Complete"}</td>
              </tr>)}</tbody>
            </table>
          </div>
        )}
        <p className="muted small mt-3 mb-0">Attendance is recorded using the server's local date and time. Multiple breaks are supported; an active break must end before check-out.</p>
      </section>

      <section className="panel">
        <div className="panel__head"><h2>My attendance history</h2></div>
        {loading ? <p className="empty">Loading attendance…</p> : history.length === 0 ? <p className="empty">No attendance records yet.</p> : (
          <div className="table-responsive">
            <table className="table align-middle mb-0">
              <thead><tr><th>Date</th><th>Check-in</th><th>Check-out</th><th>Status</th></tr></thead>
              <tbody>{history.map((row) => <tr key={row.id}><td>{row.workDate}</td><td>{formatTime(row.checkIn)}</td><td>{formatTime(row.checkOut)}</td><td>{row.checkOut ? "Completed" : "Present"}</td></tr>)}</tbody>
            </table>
          </div>
        )}
      </section>

      {canManage && <section className="panel">
        <div className="panel__head"><h2>Team attendance today</h2></div>
        {loading ? <p className="empty">Loading team attendance…</p> : team.length === 0 ? <p className="empty">No team attendance recorded today.</p> : (
          <div className="table-responsive"><table className="table align-middle mb-0"><thead><tr><th>Employee</th><th>Username</th><th>Check-in</th><th>Check-out</th><th>Status</th></tr></thead><tbody>
            {team.map((row) => <tr key={row.id}><td>{row.employeeName}</td><td>{row.username}</td><td>{formatTime(row.checkIn)}</td><td>{formatTime(row.checkOut)}</td><td>{row.checkOut ? "Completed" : "Present"}</td></tr>)}
          </tbody></table></div>
        )}
      </section>}

      {canManage && <section className="panel">
        <div className="panel__head">
          <div><h2>Shift and break schedules</h2><p className="muted mb-0">Set the planned daily shift and break allowance for active users.</p></div>
        </div>
        {loading ? <p className="empty">Loading schedules…</p> : schedules.length === 0 ? <p className="empty">No active users found.</p> : (
          <div className="table-responsive">
            <table className="table align-middle mb-0">
              <thead><tr><th>Employee</th><th>Username</th><th>Shift (minutes)</th><th>Break (minutes)</th><th></th></tr></thead>
              <tbody>{schedules.map((row) => <ScheduleRow key={row.userId} row={row} busy={scheduleBusy === row.userId} onSave={saveSchedule} />)}</tbody>
            </table>
          </div>
        )}
      </section>}
    </div>
  );
}

function ScheduleRow({ row, busy, onSave }) {
  const [shiftMinutes, setShiftMinutes] = useState(row.shiftMinutes);
  const [breakMinutes, setBreakMinutes] = useState(row.breakMinutes);
  useEffect(() => {
    setShiftMinutes(row.shiftMinutes);
    setBreakMinutes(row.breakMinutes);
  }, [row.shiftMinutes, row.breakMinutes]);

  return (
    <tr>
      <td>{row.employeeName}</td>
      <td>{row.username}</td>
      <td><input className="form-control form-control-sm" type="number" min="1" max="1440" step="15" value={shiftMinutes} onChange={(event) => setShiftMinutes(event.target.value)} aria-label={`Shift minutes for ${row.employeeName}`} /></td>
      <td><input className="form-control form-control-sm" type="number" min="0" max="480" step="5" value={breakMinutes} onChange={(event) => setBreakMinutes(event.target.value)} aria-label={`Break minutes for ${row.employeeName}`} /></td>
      <td><button className="btn btn-sm btn-outline-primary" disabled={busy} onClick={() => onSave(row, shiftMinutes, breakMinutes)}>{busy ? "Saving…" : "Save"}</button></td>
    </tr>
  );
}
