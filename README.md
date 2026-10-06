# StoreBilling — Configurable Retail POS Platform

A configuration-driven Retail POS/MPOS platform: the same backend and frontend serve Gold/Jewellery, Grocery, and General Retail stores through configuration (business type, store, terminal scoping) rather than separate applications or hardcoded per-vertical branching.

## Current scope: Phases 1–3

This repository implements **Phases 1–3** of the platform's roadmap: foundation (org/store/terminal/security/configuration), catalog master data (category/product/customer/tax/pricing), and POS sales/payment/invoicing/inventory. See [ONBOARDING.md](ONBOARDING.md) for the full status, architecture conventions, and what's still outstanding (returns, shift management, Gold/Grocery specialization, reporting, MPOS).

## Tech stack

- **Backend:** Spring Boot 4.1.x, Java 25 (Amazon Corretto), Maven, Spring Data JPA/Hibernate, Spring Security + JWT, Flyway, OpenAPI/Swagger, Actuator
- **Frontend:** Angular 19, Angular Material, Reactive Forms
- **Database:** MySQL 8.4 LTS
- **Testing:** JUnit 5, Mockito, Testcontainers

## Project layout

```
StoreBilling/
├── storebilling-service/    Spring Boot modular monolith (com.retailpos)
├── frontend/                 Angular 19 workspace
├── docker-compose.yml        MySQL + Adminer for local dev
└── .env.example              Copy to .env and fill in real values (never commit .env)
```

## Local setup

1. Copy `.env.example` to `.env` and fill in real values (DB credentials, a strong `JWT_SECRET`, and a `BOOTSTRAP_ADMIN_USERNAME`/`BOOTSTRAP_ADMIN_PASSWORD` for the initial admin user). **Never commit `.env`.**
2. Start MySQL: `docker-compose up -d mysql`
3. Copy `storebilling-service/src/main/resources/application-local.yml.example` to `storebilling-service/src/main/resources/application-local.yml` (also gitignored) if you prefer file-based config over env vars.
4. Run the backend: `cd storebilling-service && ./mvnw spring-boot:run` (reads `.env` values as environment variables — export them in your shell, or use your IDE's env-file support).
5. Run the frontend: `cd frontend && npm install && ng serve`
6. Backend API docs: `http://localhost:8080/swagger-ui.html`. Frontend: `http://localhost:4200`.

### Bootstrap admin user

On first startup, if no `SUPER_ADMIN` user exists, the backend creates one from `BOOTSTRAP_ADMIN_USERNAME` / `BOOTSTRAP_ADMIN_PASSWORD`. There is **no hardcoded or seeded password** anywhere in the codebase or migrations — you must set these env vars yourself before the first run. Rotate the password (or disable the bootstrap account) once you've created your own admin users.

### Seed / demo data

Local/dev-only Flyway migrations seed a demo organization, three business types (`GOLD`, `GROCERY`, `GENERAL`), one store per type, one terminal per store, and a handful of illustrative `app_configuration` rows that demonstrate the scope-resolution chain (`TERMINAL → STORE → ORGANIZATION → BUSINESS_TYPE → GLOBAL → DEFAULT`). This data is for local development only and is excluded from any non-dev Flyway profile.

## Security notes

- No real secrets are ever committed. `.env`, `application-local.yml`, and anything matching `*credentials*`/`*secret*` are gitignored.
- Passwords are hashed with BCrypt (strength 12).
- All non-public API endpoints require a valid JWT and are authorized by role/permission; store/terminal-scoped requests are additionally checked against the caller's store access to prevent IDOR.
