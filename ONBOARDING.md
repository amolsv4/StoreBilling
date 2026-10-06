# StoreBilling — Onboarding & Development Status

_Last updated: 2026-09-19, after Phase 3._

This document is for whoever picks up development on this project next. It covers what's been built, the conventions the codebase follows (important to keep consistent), what's known to be missing, and where to go next.

## 1. What this is

A **configuration-driven Retail POS/MPOS platform**: the same backend and frontend serve Gold/Jewellery, Grocery, and General Retail stores through configuration (business type, store, terminal scoping) rather than separate applications or hardcoded per-vertical branching. Functionally inspired by enterprise POS configuration models (e.g. SAP Customer Checkout's approach to configurable sales/hardware/currency/users) — no SAP code, schema, or UI was copied.

The build follows a 9-phase roadmap; **Phases 1–3 are done**, Phases 4–9 are not started. See §6.

## 2. Tech stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 4.1.x, Java 25 (Amazon Corretto), Maven, Spring Data JPA/Hibernate, Spring Security + JWT, Flyway, OpenAPI/Swagger, Actuator |
| Frontend | Angular 19, standalone components, Angular Material, Reactive Forms |
| Database | MySQL 8.4 LTS |
| Testing | JUnit 5, Mockito, Testcontainers |

## 3. Repository layout

```
StoreBilling/
├── storebilling-service/   Spring Boot modular monolith (package com.retailpos)
├── frontend/                Angular 19 workspace
├── docker-compose.yml       MySQL + Adminer for local dev
├── .env.example              Copy to .env yourself, fill in real values (never commit)
├── README.md                 Quick setup instructions
└── ONBOARDING.md              This file
```

Backend is a **modular monolith**, not microservices — one Spring Boot app, one package per business domain under `com.retailpos`. Frontend is a single Angular workspace with two distinct areas: the admin `ShellComponent` (sidenav, all master-data CRUD screens) and the full-screen `/pos` area (cashier-facing, no sidenav).

## 4. What's built

### Phase 1 — Foundation
Organization → Store → Terminal hierarchy, User/Role/Permission (RBAC), JWT auth, and the **multi-scope configuration engine** (`app_configuration`, resolution priority `TERMINAL → STORE → ORGANIZATION → BUSINESS_TYPE → GLOBAL → DEFAULT`) — the mechanism that will eventually let Gold/Grocery/General stores diverge in behavior purely through config, not code branching. Bootstrap admin is created at first startup from `BOOTSTRAP_ADMIN_USERNAME`/`BOOTSTRAP_ADMIN_PASSWORD` env vars — no seeded password anywhere.

### Phase 2 — Catalog master data
Category (hierarchical, self-referencing parent with cycle prevention), Product (generic — no Gold/Grocery-specific fields yet, those are Phase 4/5), Customer, Tax (`TaxComponent` + `TaxGroup` composite bundling, e.g. CGST+SGST), Pricing (`PriceList` + `ProductPrice`).

### Phase 3 — POS Sales, Payment, Invoice, Inventory
- **Sales**: cart-style line-by-line building (`sale`/`sale_line`), hold/resume, bill- and line-level discounts, price override (permission-gated), tax inclusive/exclusive calculation, snapshotted tax/uom/product-name on each line (so a later product rename or tax-rate change never retroactively alters a completed invoice).
- **Invoice numbering**: `{storeCode}-{yyyyMMdd}-{sequence:06d}`, concurrency-safe via a `PESSIMISTIC_WRITE`-locked `invoice_sequence` row per store, assigned only at completion.
- **Payment**: split/multi-tender (`payment_transaction`), overpayment allowed with change-due calculation.
- **Inventory**: a single choke-point service (`InventoryService.applyStockChange`) that every stock-mutating operation goes through — Sales, Purchase Receipts, Stock Transfers, Stock Adjustments, Stock Takes. Negative stock is **allowed** (logged, not blocked) — a deliberate choice so a stock discrepancy never blocks a sale at the register; Stock Take is the reconciliation mechanism.
- **Customer receipt notification**: Email/SMS/WhatsApp — currently a **logging-only stub** (`NoOpNotificationServiceImpl`), not wired to a real provider. See §7.
- **POS frontend**: `/pos` full-screen area outside the admin shell — session picker (pick store/terminal, persisted per-tab via `sessionStorage`), the sale screen (product/customer search-as-you-type, editable cart, `CurrencyPipe` bound to the sale's resolved currency), payment dialog (incremental tender entry, safe retry on failure), receipt dialog with print + notification buttons, held bills.

## 5. Conventions — keep these consistent in future work

These aren't arbitrary — they were established deliberately and enforced across three phases. Deviating breaks consistency for whoever comes after you.

**Backend, every module:**
- Package layout: `entity/`, `repository/`, `dto/`, `service/` (interface + `*Impl`), `mapper/` (MapStruct), `controller/` — flat, no `impl` subpackage.
- Entities extend `common.entity.BaseAuditableEntity` (id, createdBy, createdDate, updatedBy, updatedDate, `@Version`) **unless** they're genuinely append-only (`AuditLog`, `InventoryTransaction`) or a pure balance/lookup row with no meaningful "creation" event (`Inventory`) — those get a documented, deliberate custom shape instead.
- **Soft delete only for master data** — `Update*Request` has a trailing nullable `Boolean active`; the mapper ignores it; the service conditionally sets it. **No DELETE endpoint** for master data, ever. Transactional records (Sales lines pre-completion; DRAFT purchase/transfer/stock-take headers and lines) are the one documented exception — real hard deletes, but only before the record is finalized.
- URLs are plural kebab-case (`/api/v1/organizations`, `/api/v1/tax-groups`). Only `/api/v1/config` is deliberately singular (a resolve-engine, not CRUD).
- Permission codes: `<MODULE>_<ACTION>` uppercase (`PRODUCT_VIEW`, `SALES_COMPLETE`, `PURCHASE_RECEIVE`). New permission rows always go in a **new** Flyway migration — never edit an already-created one.
- **Flyway version numbering gotcha**: `db/migration/` (always-applied) and `db/seed-dev/` (dev-profile-only demo/seed data) are merged into **one shared version sequence** by the `local` profile. Before adding a migration, check both folders' current max version — don't just look at one.
- Standard response envelope everywhere: `{success,message,data}` / `{success:false,message,errorCode,errors}` via `common.dto.ApiResponse<T>` + `common.exception.GlobalExceptionHandler`.
- `AccessValidator.assertStoreAccess`/`assertTerminalAccess` re-checked on every mutating endpoint that touches store/terminal-scoped data — keyed off the **loaded entity's** store/terminal, not a URL param, so revoked access takes effect immediately even on sub-resource calls.

**Frontend, every admin screen:**
- `core/models/<entity>.model.ts` (`XBaseFields`/`XCreateRequest`/`XUpdateRequest`/`X` shape) + `core/services/<entity>.service.ts` (`list`/`listAll`/`getById`/`create`/`update`, envelope-unwrapping) + `features/admin/<entity>/<entity>-list.component` + `-form-dialog.component`.
- `MAT_DIALOG_DATA` is injected as a plain field (`inject(MAT_DIALOG_DATA)`) declared **before** any other field whose initializer reads it — never as a constructor parameter property (field-initializer ordering bug, hit and fixed multiple times across phases).
- No delete UI anywhere except the few places the backend genuinely allows it (see above) — deactivate via the `DataTableComponent`'s built-in toggle instead.
- New route → add to `app.routes.ts` as a child of `ShellComponent` (or the `/pos` tree for cashier-facing screens) with `canActivate:[permissionGuard]`/`data:{permission}`; new permission module → add to `core/constants/permission-codes.ts`; new nav entry → `layout/shell/shell.component.ts`'s `NAV_ITEMS`.

## 6. Roadmap — what's next

Per the original phase plan, in order:

- **Phase 4** — Gold/Jewellery specialization: gold rate, purity, weight, making charges, wastage, old-gold exchange. Builds on Phase 2's generic `Product` via a new `GoldProductDetail` extension table (1:1), and the Phase 1 configuration engine's `BUSINESS_TYPE=GOLD` scope.
- **Phase 5** — Grocery specialization: batch/lot tracking, expiry dates, MRP, weight-based items. Same pattern, `GroceryProductDetail`.
- **Phase 6** — Returns, cancellation, refunds, shift management. Sale cancellation/void and sale/purchase returns were deliberately **not** built in Phase 3 — `SaleStatus` has no `CANCELLED` value yet, `InventoryTransactionType` reserves `SALE_RETURN`/`PURCHASE_RETURN` but nothing posts them. Shift management (open/close, cash reconciliation) doesn't exist at all yet — Phase 3's POS session picker is a lightweight store/terminal selector, not a shift.
- **Phase 7** — Reporting/dashboards.
- **Phase 8** — MPOS (mobile POS) — same backend APIs, different client; offline sync architecture.
- **Phase 9** — Hardening: expanded test coverage, security review, performance testing.

## 7. Known gaps / accepted limitations

- **Never been booted against a live database.** This machine has no Docker installed, so nothing beyond unit tests (mocked repositories) has ever run — no Testcontainers integration tests, no real `mvnw spring-boot:run`, no real `ng serve` against a live backend. Everything has been verified by independent recompilation + re-running the unit suite, and by direct code review, not by execution.
- **No git repository yet.** Nothing has been committed. Per organizational policy, the AI building this never runs `git init`/`commit`/`push` — that's a human step.
- **Notification channels (Email/SMS/WhatsApp) are a stub.** `NoOpNotificationServiceImpl` logs a masked contact detail and records a `notification_log` row, but never actually sends anything. Wiring a real provider (SendGrid, Twilio, WhatsApp Business API, etc.) needs a vendor decision and real credentials, which must be set up directly in `.env` — never pasted into an AI chat session.
- **No customer consent tracking for notifications.** Before real sends go live, add per-channel opt-in tracking on `Customer` — flagged in the Phase 3 plan, not built.
- **Purchase receipts have no structured Supplier entity** — just a free-text `supplier_name` field. Add a real `Supplier` master (with its own permission module) if/when purchasing scope grows (payables, supplier-wise reporting).
- **Price resolution is single-price-list-per-store.** `Store.defaultPriceListId` is one FK, not a priority chain — if multi-tier pricing (e.g. per customer group) becomes a requirement, this needs real design work, not just a schema tweak.
- **Negative stock is allowed by design** (see §4) but the "allow negative stock" behavior isn't yet a configurable per-org flag, even though the Configuration engine from Phase 1 could support it. Flagged as a natural small addition.

## 8. Local setup

See [README.md](README.md) for the step-by-step. Summary: copy `.env.example` → `.env`, fill in real values yourself (rotate any credential that was ever typed into a chat session), get MySQL running (§8.1), `cd storebilling-service && ./mvnw spring-boot:run`, `cd frontend && npm install && ng serve`.

### 8.1 Connecting to MySQL

The backend expects a MySQL 8.4 server reachable at whatever `DB_USERNAME`/`DB_PASSWORD` in `.env` point to, on the database named `storebilling` (see `storebilling-service/src/main/resources/application.yml`'s `spring.datasource.url: jdbc:mysql://localhost:3306/storebilling...`). Two ways to get that server running:

**Option A — Docker (recommended if you don't already have MySQL installed):**
1. Fill in `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`, `MYSQL_ROOT_PASSWORD` in `.env` (these feed `docker-compose.yml`'s `mysql` service).
2. `docker-compose up -d mysql` from the repo root — this creates a container with the `storebilling` database already present (via `MYSQL_DATABASE`) and exposes it on `localhost:3306`.
3. Set `DB_USERNAME`/`DB_PASSWORD` in `.env` to match whatever you set `MYSQL_USER`/`MYSQL_PASSWORD` to.
4. Optional: `Adminer` (a lightweight DB admin UI) is also started, at `http://localhost:8081` — log in with the same credentials, server `mysql`.

**Option B — An existing local MySQL server (e.g. one you already run via MySQL Workbench/Server on Windows):**
1. Make sure it's listening on port 3306 (or update `spring.datasource.url` in `application.yml`/`application-local.yml` if it's on a different port).
2. Create the database once, using whatever client you already have (MySQL Shell/`mysqlsh`, MySQL Workbench, or the `mysql` CLI if installed):
   ```sql
   CREATE DATABASE IF NOT EXISTS storebilling CHARACTER SET utf8mb4;
   ```
3. Create (or reuse) a MySQL user with privileges on that database, and put its username/password into `DB_USERNAME`/`DB_PASSWORD` in `.env`. **Type real credentials directly into `.env` in your editor — never paste them into an AI chat session** (if a credential is ever pasted into a chat, treat it as compromised and rotate it immediately).
4. Skip `docker-compose up -d mysql` (you already have a server); `docker-compose.yml`'s `adminer` service can still optionally be started on its own if you want a web DB browser: `docker-compose up -d adminer`.

**Either way, you only need to create the empty `storebilling` database once** — you never hand-create tables. Every table is created automatically by Flyway the first time the backend boots (`./mvnw spring-boot:run`), applying `V1`…`V27` in order (see `storebilling-service/src/main/resources/db/migration/` and `db/seed-dev/`). If migrations fail partway through on a non-empty database, the safest fix is usually to drop and recreate the `storebilling` database and let Flyway start clean, rather than hand-editing tables.

**Verifying the connection worked:**
- Watch the backend's startup log for Flyway lines (`Migrating schema "storebilling" to version "27"...`) and no connection errors.
- Once it's up, `http://localhost:8080/actuator/health` should return `{"status":"UP"}` (this also confirms the DB connection, since Spring Boot's health check includes a DB ping).
- `http://localhost:8080/swagger-ui.html` lists all the REST endpoints if you want to try one directly (e.g. `POST /api/v1/auth/login` with your `BOOTSTRAP_ADMIN_USERNAME`/`PASSWORD`).

## 9. Where to look first

- `storebilling-service/src/main/java/com/retailpos/sale/service/SaleServiceImpl.java` — the most complete example of the backend conventions in one place (FK resolution, snapshot-on-write, idempotent completion, tax branching).
- `storebilling-service/src/main/java/com/retailpos/inventory/service/InventoryService.java` (+`InventoryServiceImpl`) — the single choke-point pattern for stock mutation; anything that touches stock should call this, never write to `inventory`/`inventory_transaction` directly.
- `storebilling-service/src/main/java/com/retailpos/configuration/service/ConfigurationServiceImpl.java` — the scope-resolution engine everything Gold/Grocery-specific (Phase 4/5) will eventually hook into.
- `frontend/src/app/features/pos/sale/pos-sale.component.ts` — the most complex frontend screen; a good reference for the cart/search/payment interaction pattern.
- `frontend/src/app/shared/components/line-items-editor/` — the reusable editable-grid component shared by the POS cart and the inventory header+lines screens.
