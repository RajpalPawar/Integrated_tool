# Suma Integrated Project — Integration Status

## Technology baseline

- Backend: Java 17 + Spring Boot 3.3.5
- Frontend: React 18.3.1 + Vite 5.4
- Database: PostgreSQL
- Existing authentication: JWT (retained from the main Project-Tracking-Tool application)
- Database migrations: Flyway (existing migrations retained; attendance added as V9)

## Source strategy

`Project-Tracking-Tool` (the `finaltool` repository) is the application foundation. The Suma Tracking repository is retained under `reference/suma-tracking` as a source reference. Its modules are being adapted individually rather than copying its Spring Boot 4 / React 19 application over the foundation.

## Implemented in this build

### Attendance management

Backend files:
- `backend/src/main/java/com/connectprojects/backend/attendance/AttendanceRecord.java`
- `backend/src/main/java/com/connectprojects/backend/attendance/AttendanceBreak.java`
- `backend/src/main/java/com/connectprojects/backend/attendance/AttendanceSchedule.java`
- `backend/src/main/java/com/connectprojects/backend/attendance/AttendanceRepository.java`
- `backend/src/main/java/com/connectprojects/backend/attendance/AttendanceScheduleRepository.java`
- `backend/src/main/java/com/connectprojects/backend/attendance/AttendanceResponse.java`
- `backend/src/main/java/com/connectprojects/backend/attendance/AttendanceService.java`
- `backend/src/main/java/com/connectprojects/backend/attendance/AttendanceController.java`

Database:
- `backend/src/main/resources/db/migration/V9__create_attendance_records.sql`
- `backend/src/main/resources/db/migration/V10__advance_attendance_management.sql`

Frontend:
- `frontend/src/pages/AttendancePage.jsx`
- Attendance navigation and permission group added to `frontend/src/pages/Dashboard.jsx`

Permissions:
- `attendance.view`: view personal attendance
- `attendance.manage`: view team attendance

API endpoints (JWT required):
- `GET /api/attendance/today`
- `GET /api/attendance/my`
- `POST /api/attendance/check-in`
- `POST /api/attendance/check-out`
- `POST /api/attendance/break-start`
- `POST /api/attendance/break-end`
- `GET /api/attendance/team?date=YYYY-MM-DD` (`attendance.manage` required)
- `GET /api/attendance/schedules` (`attendance.manage` required)
- `PUT /api/attendance/schedules/{userId}` (`attendance.manage` required)

Rules:
- A user can create one attendance record per server-local calendar date.
- A user can record multiple breaks, with only one active break at a time; an active break must end before checkout.
- A user can check out once, and the checkout time cannot precede check-in.
- Users can only view their own history. Team attendance is permission-gated.
- Managers can set each active user's planned shift and break minutes; defaults are 540 and 60 minutes.
- The current implementation records the server's local date/time. It does not yet apply employee-specific shifts, holidays, break rules, geofencing, device tracking, or attendance correction workflows from the source application.

### Knowledge Base

The integrated app now has a permissioned article library with keyword search across titles, categories, and content. Users with `knowledge.manage` can create articles, edit them, and publish or unpublish drafts. Users with `knowledge.view` can access published articles.

API endpoints (JWT required):
- `GET /api/knowledge/articles?q=...`
- `GET /api/knowledge/articles/{slug}`
- `POST /api/knowledge/articles` (`knowledge.manage` required)
- `PUT /api/knowledge/articles/{id}` (`knowledge.manage` required)

The database migration adds article storage and grants view access to existing users; existing Admin and Manager accounts also receive article management access. Article version history, feedback, and unanswered-question tracking from the source application are not included yet.

## Important integration note

Existing users whose permission lists were already stored before this module was added may need `attendance.view` and, where appropriate, `attendance.manage` assigned by a Super User in User Permissions. New users receive defaults based on their role.

## Validation status

Source files and project wiring have been added. A full Maven compile, frontend production build, PostgreSQL migration run, and authenticated end-to-end test have not been completed in this environment. Do not treat this archive as production-ready until those checks pass.

## Remaining requested modules

1. Advanced SLA engine and business calendar.
2. Automation rules.
3. Excel ticket import.
4. Audit logs.
5. Integration management (live connector requires a provider decision).
6. Public landing page.

## Build commands for a developer machine

Backend (Java 17; Maven Wrapper downloads Maven on first use):

```bash
cd backend
./mvnw test
```

Frontend (Node.js/npm required):

```bash
cd frontend
npm ci
npm run build
```

The backend requires a PostgreSQL database and the environment variables described in `.env.example` before the application can start.
