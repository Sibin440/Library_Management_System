# OPAC Management System (Spring Boot)

This is a simple OPAC (Online Public Access Catalog) demo built with Spring Boot, Spring Data JPA, and Thymeleaf. It includes role-based authentication and basic admin tools.

## Features implemented
- Books CRUD API (`/api/books`) with search
- User and Role entities with many-to-many relationship
- Role-based access control (roles: `ROLE_USER`, `ROLE_LIBRARIAN`, `ROLE_ADMIN`)
- Signup and Login pages (Thymeleaf)
- Admin dashboard templates for basic user management
- Loan and AuditLog entities added
- Flyway migrations (initial schema)
- Admin account seeding at startup (`admin` / `adminpass`)

## How to run
1. Build the project (requires Maven):

```powershell
cd "d:\OPAC\OPAC_MANAGEMENT_SYSTEM_USING_SPIRNGBOOT_-_MAVEN-main\javaproject\opac"
# Use local Maven in workspace or your installed Maven
d:\OPAC\maven\apache-maven-3.9.6\bin\mvn.cmd clean package -DskipTests
```

2. Run the JAR:

```powershell
java -jar target\opac-0.0.1-SNAPSHOT.jar
```

3. Open the app:
- App: http://localhost:8081
- Login: http://localhost:8081/login
- Signup: http://localhost:8081/signup
- Admin dashboard: http://localhost:8081/admin/dashboard

Default admin credentials (demo only):
- username: `admin`
- password: `adminpass`

> NOTE: Change or remove the seeded admin password before sharing or deploying. For production, externalize credentials via environment variables and secure secrets store.

## Database
- Configured for MySQL via `src/main/resources/application.properties`.
- Flyway migrations are located at `src/main/resources/db/migration`.
- Current setup seeds the admin at startup via `DataInitializer`.

## API endpoints
- `GET /api/books` — list books
- `GET /api/books/{id}` — get book
- `POST /api/books` — add book (admin/librarian)
- `PUT /api/books/{id}` — update book (admin/librarian)
- `DELETE /api/books/{id}` — delete book (admin only)
- `GET /api/books/search?query=...` — search
- `GET /actuator/health` — health

## Admin pages
- `/admin/users` — manage users and roles
- `/admin/dashboard` — admin home

## Next recommended steps
- Replace runtime admin seeding with secure Flyway seed or environment-based secret.
- Add integration tests for auth flows and protected APIs.
- Improve admin UI: show existing roles as checked boxes, add user create/edit forms.
- Add CI pipeline and Maven wrapper to ensure reproducible builds.

If you want, I can now:
- Finish tests and add example integration tests.
- Replace DataInitializer seeding with a Flyway seed migration.
- Improve admin UI to pre-check existing roles and allow role changes smoothly.

Tell me which next step to take and I'll implement it.
