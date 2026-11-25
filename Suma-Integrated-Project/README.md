# Project Tracking Tool

A project, task, ticket and reporting platform with role/permission management.

- **Frontend:** React 18 + Vite (`frontend/`)
- **Backend:** Spring Boot 3.3 / Java 17, JWT authentication (`backend/`)
- **Database:** PostgreSQL 16, schema managed by Flyway (`backend/src/main/resources/db/migration`)

> Read `docs/TESTING-REPORT.md` first: it states exactly what has and has not been verified.

## Modules

Login & password reset · Dashboard · Projects · Project Management (clients, leads, shifts, priorities, categories, SLAs) · Tasks · Tickets (assign / resolve / close / history) · Reports & Analytics (daily/weekly/monthly, monthly-log Excel import, scheduled e-mail reports) · Project Analytics · Employee Directory · Attendance · Knowledge Base · Notifications · Profile & Settings · Account requests · User management · Per-user permissions.

## Permission model

- **Super User**: always has every permission; creates, edits, and deletes non-Manager accounts; grants and revokes individual permissions.
- **Other roles**: can only do what the Super User has individually ticked under *User permissions*. The role name alone grants nothing. Every API call is checked on the server (`CurrentUserService.require`), and the menu is hidden in the UI to match.
- **Manager accounts**: protected from edits, permission changes, and deletion by other accounts, including Super Users. A Manager can delete only their own account from **Settings**; they cannot change their own role or permissions. Super Users can still manage the roles and permissions of non-Manager accounts.
- **Employee Directory**: access is explicitly limited to Manager-role accounts. This restriction also blocks Super Users from the employee and salary API, despite their normal permission bypass.
- **Employee attendance**: employees use their linked non-Manager login account to record their own check-in, check-out, and multiple break intervals. Only Managers can view the team's attendance records and break details; non-Managers can access only their own attendance.

## Employee directory and salary privacy

Managers can create, view, update, and remove employee records, including employee ID, name, department, job title, work email, employment status, and monthly gross salary in INR. Annual gross salary is calculated as monthly salary × 12. The Manager can optionally link an employee record to one existing, active non-Manager login account with attendance access. Login accounts remain separate from employee records.

Salary values are encrypted with AES-256-GCM before being stored in PostgreSQL. Configure `SALARY_ENCRYPTION_KEY` as a Base64-encoded random 32-byte key in `.env`; do not commit or share it. The database stores ciphertext, while the employee API returns salary data only to authenticated users with the Manager role. A Super User is deliberately denied this API. Database or server administrators who can access both the database and the encryption key can still decrypt the values. Back up the key securely with the database backups; losing it makes stored salary values unrecoverable.

## Prerequisites

| Tool | Version | Check |
|---|---|---|
| Java JDK | 17+ | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Node.js | 18+ (20 or 22 recommended) | `node -v` |
| PostgreSQL | 14+ (16 recommended) **or** Docker Desktop | `psql --version` |

## Quick start

### 1. Configure secrets

```bash
cp .env.example .env          # Windows PowerShell: Copy-Item .env.example .env
```

Open `.env` and replace **every** `change-me…` value (database password, Super User password, `JWT_SECRET`, `SALARY_ENCRYPTION_KEY`). Never commit `.env`.

### 2. Database (pick one)

**A. Docker (easiest)** – from the project root:

```bash
docker compose up --build        # starts PostgreSQL *and* the backend on :8080
```

Then skip step 3 and go to step 4.

**B. Local PostgreSQL** – see `docs/DATABASE-SETUP.md` for the full beginner guide. In short:

```bash
psql -U postgres -v db_password=YourDbPassword -f database/create-database.sql
```

Use the same password as `DATABASE_PASSWORD` in `.env`.

### 3. Start the backend (only for option B)

```bash
cd backend
mvn spring-boot:run
```

The backend reads `../.env` automatically. On first start Flyway creates all tables and the Super User from `BOOTSTRAP_SUPERUSER_*` is created. Wait for `Started ConnectProjectsApplication`.

### 4. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

### 5. Open the app

| What | URL |
|---|---|
| Application | http://localhost:5173 |
| API | http://localhost:8080/api |

Sign in with `BOOTSTRAP_SUPERUSER_USERNAME` / `BOOTSTRAP_SUPERUSER_PASSWORD` from your `.env`.

## Typical first steps

1. Sign in as Super User → **User Management** → create users.
2. **User permissions** → tick exactly what each user may do → *Save permissions*.
3. **Project Management** → add clients, projects, leads, SLAs → then use Tasks and Tickets.
4. Sign in with a Manager account → **Employee Directory** → add the company roster and link each employee to their existing login account. Employees use **Attendance** to punch in/out and record breaks; Managers use **Employee Directory → Employee attendance** to review daily records. Only Managers can see team records and salary values.

## Environment variables

See `.env.example` (every variable is commented). Frontend: `frontend/.env.example` (`VITE_API_BASE_URL`, optional).

## Production build

```bash
cd frontend && npm run build     # output in frontend/dist (SPA routing config: netlify.toml)
cd backend  && mvn package       # runs unit tests, builds target/account-api-1.0.0.jar
```

Set `VITE_API_BASE_URL` at frontend build time and `CORS_ALLOWED_ORIGINS` on the API for a split deployment.

## Project layout

```
Project-Tracking-Tool/
├── README.md  .env.example  docker-compose.yml  .gitignore
├── backend/                Spring Boot API (pom.xml, Dockerfile, src/)
├── frontend/               React app (package.json, vite.config.js, src/)
├── database/               create-database.sql, verify.sql
├── docs/                   DATABASE-SETUP.md, MERGE-REPORT.md, TESTING-REPORT.md
└── reference/suma-tracking Source of the second project's modules that were not merged
```

## Troubleshooting

- `Could not resolve placeholder 'DATABASE_PASSWORD'` → `.env` is missing or not in the project root.
- `password authentication failed` → `.env` password differs from the one used in `create-database.sql`.
- Login works but pages say "Sign in is required" → token expired (8 h); sign in again.
- Browser CORS error → add your frontend origin to `CORS_ALLOWED_ORIGINS`.
- More database help: `docs/DATABASE-SETUP.md`.
