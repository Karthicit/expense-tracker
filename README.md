# Expense Tracker

A full-stack personal finance app. Users log income and expenses by category and get a monthly dashboard with charts. Each user only ever sees their own data.

**Live demo:** https://expense-tracker-ui-snowy-seven.vercel.app
**API docs (Swagger):** https://expense-tracker-sq5f.onrender.com/swagger-ui/index.html
**Demo login:** `demo@example.com` / `demo12345`

> Hosted on free tiers: the first request after a quiet period can take up to 1-2 minutes while the server wakes up.

## Screenshots

| Dashboard | Transactions |
|---|---|
| ![Dashboard](docs/screenshots/dashboard.png) | ![Transactions](docs/screenshots/transactions.png) |

| Login | Swagger |
|---|---|
| ![Login](docs/screenshots/login.png) | ![Swagger](docs/screenshots/swagger.png) |

## Features

- Register and log in with JWT authentication; passwords stored as BCrypt hashes
- Categories and transactions (income or expense) with full CRUD and validation
- Per-user data isolation: other users' records return `404`
- Monthly summary: income, expense, savings and savings rate
- Dashboard: 6-month trend, spend by category, daily spend and top 5 expenses
- Swagger UI for the whole API, with a bearer-token Authorize button

## Tech stack

| Layer | Tech |
|---|---|
| Backend | Java 17, Spring Boot 4.1.2, Spring Data JPA (Hibernate), Spring Security |
| Auth | JWT (jjwt), BCrypt |
| Database | MySQL 8 |
| Frontend | React, Vite, React Router, Recharts ([frontend repo](https://github.com/Karthicit/expense-tracker-ui)) |
| Tests | JUnit 5, MockMvc, H2 (in-memory) |
| Deployment | Docker, Render (backend), Aiven (MySQL), Vercel (frontend) |

## Architecture

```
React (Vercel) -> Controller -> Service -> Repository -> MySQL (Aiven)
                  (HTTP, validation)  (business logic)  (Spring Data JPA, JPQL)
```

A JWT filter authenticates every request before it reaches a controller. Errors share one JSON shape through a global exception handler.

## Design decisions

- **BigDecimal for money**, never double, to avoid floating-point errors.
- **Stateless JWT**: no server sessions; the token carries the user's email.
- **Ownership enforced in queries** (`findByIdAndUserId`), so ids of other users' records cannot be probed.
- **Reports use SQL aggregation** (`SUM`, `GROUP BY`) through JPQL instead of looping in Java.
- **Configuration through environment variables**, so no secrets are in the repository.

## Run locally

1. Install Java 17, Maven and Docker.
2. Start MySQL:
   ```bash
   docker run -d --name mysql-expense -e MYSQL_ROOT_PASSWORD=Root@123 -e MYSQL_DATABASE=expense_tracker -p 3306:3306 mysql:8
   ```
3. Start the backend:
   ```bash
   export DB_PASSWORD='Root@123'
   export JWT_SECRET='any-random-string-at-least-32-characters-long'
   mvn spring-boot:run
   ```
4. Start the frontend from the [frontend repo](https://github.com/Karthicit/expense-tracker-ui) with `npm install` and `npm run dev`, then open http://localhost:5173.

| Variable | Purpose | Default |
|---|---|---|
| `DB_URL` | JDBC URL | `jdbc:mysql://localhost:3306/expense_tracker` |
| `DB_USERNAME` | Database user | `root` |
| `DB_PASSWORD` | Database password | required |
| `JWT_SECRET` | Token signing secret (32+ characters) | required |
| `CORS_ORIGIN` | Allowed frontend address | `http://localhost:5173` |

## Tests

```bash
mvn test
```
Integration tests run on an in-memory H2 database, so no MySQL is needed. They cover register and login, the `401` for missing or fake tokens, and one user not seeing another's data.

## Deployment

The backend is built from the included `Dockerfile` (multi-stage build) and deployed on Render, with `/api/health` as its health check. The database is a free Aiven MySQL service connected over SSL.