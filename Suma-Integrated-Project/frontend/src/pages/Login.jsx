import { useState } from "react";
import { motion } from "framer-motion";
import {
  login,
  submitAccountRequest,
  requestPasswordReset,
} from "../auth/authService.js";
import "./Login-reference.css";

const ACCESS_LEVELS = [
  {
    name: "Super User",
    description: "Manages users and sees every project.",
    icon: "M12 3l7 3v6c0 4-3 7.5-7 9-4-1.5-7-5-7-9V6z",
  },
  {
    name: "Admin",
    description: "Runs projects and reviews reports.",
    icon: "M3 21h18M5 21V8l7-4 7 4v13M9 21v-5h6v5",
  },
  {
    name: "Project Lead",
    description: "Tracks work across assigned projects.",
    icon: "M3 7h18v13H3zM8 7V4h8v3",
  },
];

export default function Login({ onSuccess }) {
  const [mode, setMode] = useState("login");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [loading, setLoading] = useState(false);

  function changeMode(nextMode) {
    setMode(nextMode);
    setError("");
    setNotice("");
    setPassword("");
    setEmail("");
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setNotice("");
    setLoading(true);

    try {
      if (mode === "login") {
        if (!username.trim() || !password) {
          throw new Error("Enter your username or email address and password.");
        }

        const user = await login(username, password);
        onSuccess(user);
      } else if (mode === "create") {
        if (!name.trim() || !username.trim() || !email.trim()) {
          throw new Error("Enter your name, requested username, and email address.");
        }

        if (!/^[a-z0-9._-]{3,30}$/i.test(username.trim())) {
          throw new Error("Username must be 3-30 characters using letters, numbers, dots, underscores, or hyphens.");
        }

        await submitAccountRequest({ name, username, email });

        setUsername(username.trim().toLowerCase());
        setPassword("");
        setName("");
        setEmail("");
        setNotice("Your enquiry was sent to the Super User. If approved, they will create your account and share your sign-in details.");
        setMode("login");
      } else if (mode === "forgot") {
        if (!email.trim()) {
          throw new Error("Enter your registered email address.");
        }

        await requestPasswordReset(email);
        setNotice("If an account exists with that email address, a password reset link has been sent. Please check your inbox.");
        setEmail("");
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Something went wrong. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  const title =
    mode === "create"
      ? "Request an account"
      : mode === "forgot"
        ? "Forgot password?"
        : "Sign in";

  return (
    <motion.div
      className="suma-auth"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 0.35 }}
    >
      <div className="suma-auth__grid" aria-hidden="true" />
      <div className="suma-auth__scan" aria-hidden="true" />

      <header className="suma-auth__top">
        <div className="suma-auth__brand" aria-label="Suma Tracking">
          <span className="suma-auth__brand-mark" aria-hidden="true">S</span>
          <span>
            <strong>Suma Tracking</strong>
            <small>Support operations</small>
          </span>
        </div>
        <span className="suma-auth__top-label">PROJECT WORKSPACE</span>
      </header>

      <main className="suma-auth__main">
        <motion.section
          className="suma-auth__card"
          aria-labelledby="signin-title"
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4, delay: 0.08 }}
        >
          <span className="suma-auth__corner suma-auth__corner--tl" aria-hidden="true" />
          <span className="suma-auth__corner suma-auth__corner--tr" aria-hidden="true" />
          <span className="suma-auth__corner suma-auth__corner--bl" aria-hidden="true" />
          <span className="suma-auth__corner suma-auth__corner--br" aria-hidden="true" />

          <div className="suma-auth__eyebrow">
            <span className="suma-auth__dot" aria-hidden="true" />
            {mode === "login" ? "SECURE SIGN-IN" : "ACCOUNT SERVICES"}
          </div>
          <h1 id="signin-title">{title}</h1>
          <p className="suma-auth__lead">
            {mode === "create"
              ? "Send an account enquiry to the Super User. Approved accounts are created by the Super User."
              : mode === "forgot"
                ? "Enter the email address associated with your account. We'll send you a password reset link."
                : "Use the username and password provided to you."}
          </p>

          <form className="suma-auth__form" onSubmit={handleSubmit} noValidate>
            {error && <p className="suma-auth__message suma-auth__message--error" role="alert">{error}</p>}
            {notice && <p className="suma-auth__message suma-auth__message--notice" role="status">{notice}</p>}

            {mode === "create" && (
              <label className="suma-auth__field">
                <span>Full name</span>
                <input
                  type="text"
                  name="fullName"
                  autoComplete="name"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                />
              </label>
            )}

            {mode === "forgot" ? (
              <label className="suma-auth__field">
                <span>Registered email address</span>
                <input
                  type="email"
                  name="email"
                  autoComplete="email"
                  autoFocus
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </label>
            ) : (
              <label className="suma-auth__field">
                <span>{mode === "create" ? "Requested username" : "Username or email"}</span>
                <input
                  type="text"
                  name="username"
                  autoComplete="username"
                  autoFocus
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                />
              </label>
            )}

            {mode === "login" && (
              <label className="suma-auth__field">
                <span>Password</span>
                <div className="suma-auth__password">
                  <input
                    type={showPassword ? "text" : "password"}
                    name="password"
                    autoComplete="current-password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword((shown) => !shown)}
                    aria-pressed={showPassword}
                  >
                    {showPassword ? "Hide" : "Show"}
                  </button>
                </div>
              </label>
            )}

            {mode === "create" && (
              <>
                <label className="suma-auth__field">
                  <span>Email address</span>
                  <input
                    type="email"
                    name="email"
                    autoComplete="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                  />
                </label>
                <p className="suma-auth__fine">
                  The Super User reviews enquiries and creates approved accounts. Do not enter a password here.
                </p>
              </>
            )}

            {mode === "login" && (
              <button
                type="button"
                className="suma-auth__text-button suma-auth__forgot"
                onClick={() => changeMode("forgot")}
              >
                Forgot password?
              </button>
            )}

            <button className="suma-auth__submit" type="submit" disabled={loading}>
              {loading
                ? "Please wait..."
                : mode === "create"
                  ? "Send enquiry"
                  : mode === "forgot"
                    ? "Send reset link"
                    : "Sign in"}
            </button>

            {mode === "login" ? (
              <p className="suma-auth__switch">
                Need an account?{" "}
                <button
                  type="button"
                  className="suma-auth__text-button"
                  onClick={() => changeMode("create")}
                >
                  Send an enquiry
                </button>
                {" "}to the Super User.
              </p>
            ) : (
              <button
                type="button"
                className="suma-auth__text-button suma-auth__back"
                onClick={() => changeMode("login")}
              >
                Back to sign in
              </button>
            )}
          </form>
        </motion.section>

        <section className="suma-auth__levels" aria-labelledby="levels-title">
          <div className="suma-auth__eyebrow">
            <span className="suma-auth__dot suma-auth__dot--amber" aria-hidden="true" />
            ACCESS LEVELS
          </div>
          <h2 id="levels-title">One workspace, three roles</h2>
          <p className="suma-auth__lead">
            Your role is set on your account and determines which projects and actions are available.
          </p>
          <ul className="suma-auth__role-list">
            {ACCESS_LEVELS.map((role) => (
              <li className="suma-auth__role" key={role.name}>
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path
                    d={role.icon}
                    stroke="currentColor"
                    strokeWidth="1.8"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  />
                </svg>
                <span>
                  <strong>{role.name}</strong>
                  <small>{role.description}</small>
                </span>
              </li>
            ))}
          </ul>
          <p className="suma-auth__foot">A clearer view of work, from kickoff to delivery.</p>
        </section>
      </main>
    </motion.div>
  );
}
