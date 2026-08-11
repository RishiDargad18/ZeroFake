# ZeroFake — Repository Audit Report

> **STATUS: RESOLVED.** This report records the state of the repository *before*
> the remediation pass of 2026-08-10. All CRITICAL and HIGH findings, and the
> large majority of MEDIUM and LOW findings, have since been fixed. The eight
> architectural decisions in §6 were all ruled on and implemented. See the README
> for the current architecture. This document is retained as the audit record.

**Date:** 2026-08-10
**Scope:** Full repository audit (all services, chaincode, frontend, build & deployment config)
**Reference architecture:** Master Planning brief
**Implementation reference:** the repository as committed at `da35237`

> `supply-chain-service` has been dropped from the project by decision of the project owner.
> It is excluded from the functional audit and listed only as a repository-hygiene item.

---

## 1. Overall Project Health

| Dimension | Status | Notes |
|---|---|---|
| Compiles | ✅ Pass | All 4 services compile cleanly (Java 21 target, JDK 22 runtime) |
| Frontend builds | ✅ Pass | `tsc -b && vite build` succeeds, 2335 modules |
| Architecture adherence | 🟡 Mostly | Package structure, DTO pattern, BaseEntity, constructor injection all correct |
| **Security** | 🔴 **Critical** | **3 of 4 services have no authentication at all** |
| **Core feature correctness** | 🔴 **Critical** | **Counterfeit detection returns HTTP 500 instead of "COUNTERFEIT"** |
| Blockchain integrity | 🔴 Critical | Fabricated transaction IDs persisted to the audit trail |
| Deployment | 🔴 Missing | No Docker, no Compose, machine-specific absolute paths |
| Feature completeness | 🟡 Gaps | QR generation absent; 3 of 7 fraud rules unimplemented |
| Test coverage | 🔴 None | 4 generated `contextLoads()` stubs only |

**Verdict:** The codebase is *well-structured but not secure and not demonstrably correct on its headline
use case.* The engineering craft in layering, DTOs, mappers and exception hierarchies is genuinely good —
interview-quality. The problems are concentrated in cross-service integration, security, and the
blockchain trust boundary. These are fixable in days, not weeks.

---

## 2. Architecture Summary (as implemented)

```
React 19 / Vite / TS  ──┬──> auth-service        :8081   Postgres zerofake_auth
                        ├──> product-service     :8082   Postgres zerofake_product
                        ├──> blockchain-service  :8083   Postgres zerofake_blockchain ──> Fabric 2.5
                        └──> fraud-service       :8085   Postgres zerofake_fraud
                                    │
                                    ├── Feign ──> product-service
                                    └── Feign ──> blockchain-service
```

Conforms to the brief: service boundaries are correct (fraud never touches Fabric directly, product
owns no blockchain logic, blockchain owns no product CRUD). Package structure
(`controller/service/service.impl/repository/entity/dto.*/mapper/config/exception/constant`) is applied
consistently. `BaseEntity` uses `@CreationTimestamp`/`@UpdateTimestamp` with no lifecycle callbacks, as
specified. Constructor injection throughout — no field injection found.

**Deviations from the brief:**
- Port 8084 is unallocated (was supply-chain).
- The frontend, not a backend service, orchestrates the *product create → register on chain → update
  blockchain status* workflow.
- Blockchain service reads a static X.509 identity from disk; there is no Fabric CA client, no
  enrollment, and no wallet, contrary to §10 of the brief.

---

## 3. Service-by-Service Status

### 3.1 auth-service (:8081) — 🟡 Functional, insecure defaults

Working: registration, login, JWT issue, refresh tokens, BCrypt, `/me`, logout, role seeding.

| # | Severity | Finding |
|---|---|---|
| A1 | CRITICAL | JWT signing secret and DB password committed in `application.yaml` |
| A2 | HIGH | `/register` accepts a client-supplied `role` — **anyone can self-register as `ROLE_ADMIN`** |
| A3 | HIGH | Bad credentials → **500**, not 401. No handler for `AuthenticationException`/`BadCredentialsException`/`DisabledException` |
| A4 | HIGH | `register()` throws `IllegalArgumentException` on duplicate email → 500 (should be 409) |
| A5 | HIGH | `refreshToken()` prints **both refresh tokens and the user's email to stdout** (debug block, lines 1386–1391) |
| A6 | HIGH | `refreshToken()` throws `IllegalArgumentException("JWT VALIDATION FAILED")` → 500 |
| A7 | HIGH | `getCurrentUser()` throws `IllegalStateException` → 500 (should be 401) |
| A8 | HIGH | `GlobalExceptionHandler` calls `printStackTrace()` and returns raw `ex.getMessage()` to the client — marked `// <-- TEMPORARY` in the source |
| A9 | MEDIUM | JWT carries **no role and no userId claim** — only `sub=email`. Other services cannot authorize from the token without a DB lookup they don't have |
| A10 | LOW | `management.endpoints...` configured but `spring-boot-starter-actuator` is not a dependency — config and the `/actuator/health` permit rule are inert |
| A11 | LOW | `AuthServiceImpl` has 3× duplicate `SecurityContextHolder` import, 2× `Authentication`, 2× `LocalDateTime`; unused imports in `AuthResponse`/`RegisterResponse` |
| A12 | LOW | No validation `message =` on auth DTO constraints; the password `@Pattern` produces an unreadable default error |

### 3.2 product-service (:8082) — 🔴 Unauthenticated

Working: category/product/batch CRUD, soft delete, seeding, conflict + not-found handling.

| # | Severity | Finding |
|---|---|---|
| P1 | CRITICAL | `SecurityConfig` = `anyRequest().permitAll()`. **No JWT filter. Every endpoint is public** — anyone can create, edit or delete any product |
| P2 | HIGH | **QR code generation does not exist.** `qrCodePath` is `@Mapping(ignore)`d in both directions and never written; no ZXing/QR dependency in the pom. The column is permanently `null` |
| P3 | MEDIUM | `ProductServiceApplication` executes raw DDL on **every startup** to drop a Hibernate check constraint (`dropCheckConstraint`) — a workaround for adding `SUCCESS` to the enum under `ddl-auto: update` |
| P4 | MEDIUM | `BlockchainStatus` = `PENDING, REGISTERED, SUCCESS, FAILED`. Brief specifies `PENDING, REGISTERED, FAILED`. Blockchain-service uses `PENDING, SUCCESS, FAILED`. `REGISTERED` is written by nothing |
| P5 | MEDIUM | `updateBlockchainStatus` → `BlockchainStatus.valueOf(input)` on unvalidated input → `IllegalArgumentException` → 500 (should be 400) |
| P6 | MEDIUM | `getProductById` returns soft-deleted products; `getProductsByCategory`/`ByManufacturer` ignore `active` while `getAllProducts` filters on it |
| P7 | MEDIUM | `ProductMapper.updateEntity` and `ProductBatchMapper.updateEntity` are dead — both services use manual setters instead |
| P8 | LOW | `OpenApiConfig` is a copy-paste of auth's — Swagger page is titled *"ZeroFake Authentication Service API"* |

### 3.3 blockchain-service (:8083) — 🔴 Unauthenticated + audit-trail integrity

Working: Fabric Gateway 1.11.0 wiring, gRPC/TLS channel, X.509 identity, all 4 chaincode transactions,
transaction persistence, correct use of `evaluateTransaction` for reads vs `endorse/submit` for writes.

| # | Severity | Finding |
|---|---|---|
| B1 | CRITICAL | `SecurityConfig` = `anyRequest().permitAll()`. **Anyone can register products on the ledger and transfer ownership of any product to themselves** |
| B2 | CRITICAL | Fabric paths are hardcoded to one developer's machine: `\\wsl$\Ubuntu\home\rishi_dargad18\...`. The service **cannot start on any other machine** |
| B3 | HIGH | `registerProduct()` catch block string-matches `"already exists"` across the message chain, suppressed exceptions, `EndorseException.getDetails()`, **and finally the rendered stack trace** — then persists a **fabricated** transaction ID (`ALREADY_REGISTERED_<uuid>`) with status `SUCCESS`. This writes non-existent blockchain transactions into the immutable audit trail |
| B4 | HIGH | `verifyProduct()` fabricates `transactionId = "query-<uuid>"` for a read that produced no transaction |
| B5 | MEDIUM | `blockNumber` and `blockHash` are **always null** — the model exposes them but nothing populates them |
| B6 | MEDIUM | `TransactionType.PRODUCT_VERIFIED` is never persisted; verification produces no audit record |
| B7 | MEDIUM | Gateway/contract lazy init is **not thread-safe** (unsynchronised null checks). Concurrent first requests open multiple gRPC channels; all but the last leak past `@PreDestroy` |
| B8 | MEDIUM | Every Fabric failure is wrapped in a bare `RuntimeException` → 500. `ResourceNotFoundException`/`ConflictException` exist but are never used for chaincode errors |
| B9 | MEDIUM | Contrary to brief §10, there is **no Fabric CA integration, no enrollment, no wallet management**. Identity is a static cert + keystore read from disk |
| B10 | LOW | Unused `PasswordEncoder` bean (this service has no users) |
| B11 | LOW | `BaseEntity` here has no `id` (declared on the subclass), unlike auth/product/fraud where `BaseEntity` owns it |

### 3.4 fraud-detection-service (:8085) — 🔴 Core use case broken

Working: Feign clients, scan history, verification logs, risk thresholds, `GENUINE/SUSPICIOUS/COUNTERFEIT`
classification, fraud report persistence at risk ≥ 80, correct service boundary (never touches Fabric directly).

| # | Severity | Finding |
|---|---|---|
| F1 | CRITICAL | **A product that is not on the blockchain returns HTTP 500, not `COUNTERFEIT`.** Chain: chaincode `VerifyProduct` errors on a missing key → blockchain-service wraps it in `RuntimeException` → 500 → Feign throws `FeignException.InternalServerError` → uncaught → 500. **`BLOCKCHAIN_MISMATCH` can therefore never fire** — the detection path fails at exactly the moment it should detect a fake |
| F2 | HIGH | `POST /api/v1/fraud/reports` is **guaranteed to fail**. `createFraudReport` builds a `FraudReport` with no `fraudType`, no `riskScore`, no `reportedByUserId` — all three are `@NotNull nullable=false` |
| F3 | HIGH | **`MULTIPLE_LOCATION_SCAN` never affects the risk score.** In `verifyProduct` it is added to `triggeredRules` but the `+35` is never applied (it *is* applied in the unused `calculateRiskScore`) |
| F4 | HIGH | `DUPLICATE_QR` scores `30 × otherUserCount` (unbounded), not the specified flat `+30` |
| F5 | HIGH | **Ownership auto-transfer is implemented twice and both fire on the same scan** — once in `VerifyProduct.tsx`, once in `VerificationServiceImpl`. The second always fails. It also means a read-only verification mutates the ledger, and fraud-detection performs supply-chain writes. Failures are swallowed to `System.err` |
| F6 | HIGH | `ProductServiceClient` declares `ProductResponse` but product-service returns `ApiResponse<ProductResponse>`. Jackson silently produces an all-null object. It only "works" because the return value is discarded — which is also why `EXPIRED_PRODUCT` cannot be implemented |
| F7 | MEDIUM | Identity is **client-supplied**: `userId`, `userRole`, `ipAddress`, `location` all arrive in the request body. The frontend sends a hardcoded `"127.0.0.1"` and `"Local Verification Terminal"` — so scan attribution is spoofable *and* the location rule is fed a constant, making `MULTIPLE_LOCATION_SCAN` unfireable in practice |
| F8 | MEDIUM | `verifyProduct` writes ScanHistory + VerificationLog + FraudReport in **three separate transactions** — no `@Transactional` |
| F9 | MEDIUM | `INVALID_OWNER`, `SUSPICIOUS_ACTIVITY`, `EXPIRED_PRODUCT` are enum values with **no implementing logic** (3 of the 7 specified rules) |
| F10 | MEDIUM | `calculateRiskScore` is a near-duplicate risk engine that is never called by any controller — duplicated business logic that has already drifted from the real path (see F3) |
| F11 | MEDIUM | `SecurityConfig` permits `/api/v1/fraud/**` — i.e. the entire service — and enables `httpBasic` with no user, so Boot prints a random generated password at every startup |
| F12 | MEDIUM | pom omits `lombok-mapstruct-binding` from `annotationProcessorPaths` (present in the other three) — latent, currently harmless |
| F13 | LOW | `findTopByProductIdAndSuccessfulTrueOrderByScannedAtDesc` unused — the brief's "latest successful scan retrieval" step loads *all* scans instead |
| F14 | LOW | `ConflictException`, `FraudDetectedException`, `VerificationFailedException`, `BadRequestException` are declared and handled but never thrown |

### 3.5 zerofake-chaincode (Go, Fabric Contract API v2) — 🟢 Good

Clean, idiomatic, correct use of `GetHistoryForKey`, transaction-timestamp-based time (deterministic —
correctly avoids `time.Now()`), ownership validation, role validation, status transitions.

| # | Severity | Finding |
|---|---|---|
| CC1 | MEDIUM | `VerifyProduct` returns an **error** for a non-existent product. An error is the correct chaincode idiom, but combined with B8/F1 it is what turns "counterfeit" into HTTP 500. The Java side must translate it |
| CC2 | LOW | `RegisterProduct` re-implements the existence check inline instead of calling the `productExists` helper — which is therefore dead |
| CC3 | LOW | `IsVerified` is hardcoded `true` at registration and never changes, so `VerifyProduct` is effectively an existence check. Defensible for the demo, but worth stating explicitly in the viva rather than being asked |

### 3.6 Frontend (React 19 / Vite / TS) — 🟡 Good UI, contract drift

Working: glassmorphism UI, theme toggle, protected routes, QR scanner, dashboard charts, product CRUD,
transfer, timeline, toast error handling.

| # | Severity | Finding |
|---|---|---|
| FE1 | HIGH | Duplicates the backend's ownership auto-transfer (see F5) |
| FE2 | MEDIUM | **Type drift with the backend:** `TransactionType` declared `REGISTER_PRODUCT \| TRANSFER_OWNERSHIP`, backend sends `PRODUCT_REGISTERED \| OWNERSHIP_TRANSFERRED \| PRODUCT_VERIFIED`. `FraudStatus` declared `OPEN \| UNDER_INVESTIGATION \| RESOLVED`, backend has `OPEN \| UNDER_REVIEW \| CONFIRMED \| FALSE_POSITIVE \| RESOLVED`. `BlockchainStatus` omits `REGISTERED` |
| FE3 | MEDIUM | `authApi.register` types the response as `ApiResponse<RegisterResponse>` but auth-service returns a bare `RegisterResponse`, so `authService.register()` resolves to `undefined`. Latent — `Register.tsx` discards the value |
| FE4 | MEDIUM | Business workflow lives in the browser: `Products.tsx` calls create → `registerProduct` → `updateBlockchainStatus(id, "SUCCESS")`. This is the root cause of P3 and P4 |
| FE5 | LOW | `refreshToken` is implemented in the API and service layer but **never invoked** — a 401 just clears storage and hard-redirects |
| FE6 | LOW | Single 1.14 MB JS chunk, no code splitting |

---

## 4. Cross-Cutting Status

### 4.1 Build
✅ All four services compile. ✅ Frontend builds. ⚠️ Spring Boot 3.5.16, Java 21 target on a JDK 22
runtime — fine, but pin the toolchain for reproducibility.

### 4.2 Security — 🔴 The dominant issue

**Three of four services accept every request unauthenticated.** The `Authorization: Bearer` header the
frontend attaches is validated by nobody except auth-service. Role-based access control exists only in
`ProtectedRoute.tsx` — i.e. it is a UI affordance, not a security control. `curl` bypasses all of it.

Compounding: secrets committed; self-service admin registration; client-supplied identity in fraud
verification; stack traces and exception messages returned to clients.

### 4.3 Database
Four separate Postgres databases (correct for microservices). `ddl-auto: update` everywhere — acceptable
for a college project, but it is what forced the P3 constraint-dropping hack. No migrations, no indexes
declared on the frequent lookup columns (`product_id`, `transaction_id`, `email`).

### 4.4 Blockchain
Modern Fabric Gateway API used correctly (not the deprecated `fabric-sdk-java`) — good. Undermined by
B2 (unportable paths) and B3/B4 (fabricated transaction records).

### 4.5 Docker / Deployment — 🔴 Absent
No `Dockerfile`, no `docker-compose.yml`, no `.env.example` anywhere in the repository. `deploy-chaincode.sh`
hardcodes `/home/rishi_dargad18/...` and `/mnt/c/Users/RISHI D/...`. The project currently cannot be run by
anyone other than its author.

### 4.6 Repository hygiene
- `supply-chain-service` — dead module (dropped from the project): empty `@SpringBootApplication`, stub
  `application.yaml` with no port or datasource, placeholder pom with `<name/>` and `<licenses><license/></licenses>`.
- `.idea/` and `zerofake-frontend/.env` are **tracked in git** despite being listed in `.gitignore`.
- README documents a structure that does not exist (`frontend/`, `auth-service/` at root, a `docs/` directory),
  states "Java JDK 17+" (project targets 21), and lists Docker Desktop as a prerequisite for a project with no
  Docker artifacts.

---

## 5. Findings by Severity

**CRITICAL (5)** — P1, B1, F11 (no auth on 3 services) · F1 (counterfeit → 500) · A1 (committed secrets) ·
B2 (unportable Fabric config) · No Docker/Compose

**HIGH (12)** — A2, A3, A4, A5, A6, A7, A8 · P2 (no QR generation) · B3, B4 (fabricated tx IDs) ·
F2 (fraud reports 500) · F3, F4 (risk scoring wrong) · F5/FE1 (duplicated ledger writes) · F6 (Feign envelope)

**MEDIUM (24)** — enum drift, response-envelope inconsistency, thread safety, missing `@Transactional`,
unimplemented rules, dead code, DDL hack, client-supplied identity, missing Fabric CA

**LOW (14)** — logging, imports, Swagger titles, tracked IDE files, README accuracy, no tests, bundle size

---

## 6. Architectural Decisions Required

These change contracts or boundaries. **I will not touch them without your approval.**

| # | Decision | Options | My recommendation |
|---|---|---|---|
| **AD-1** | How do product/blockchain/fraud validate JWTs? | (a) Shared-secret JWT filter duplicated per service; (b) shared `common-security` module; (c) API gateway | **(a)** — smallest change, no new module, matches the existing HS256 setup. Requires adding `jwt.secret` config to 3 services |
| **AD-2** | Add `userId` + `role` claims to the JWT? | Required for any service to authorize without calling auth-service | **Yes** — changes the token contract, hence your call. Without it AD-1 can authenticate but not authorize |
| **AD-3** | Who updates `Product.blockchainStatus`? | (a) Frontend (current); (b) blockchain-service calls product-service; (c) product-service calls blockchain-service | **(b)** — removes business workflow from the browser. This is a real cross-service change |
| **AD-4** | Canonical `BlockchainStatus` for products | Brief says `PENDING/REGISTERED/FAILED`; code uses `SUCCESS`; frontend omits `REGISTERED` | Adopt the brief's **`PENDING/REGISTERED/FAILED`**, drop `SUCCESS`, then the P3 DDL hack can be deleted. Requires a one-off DB reset |
| **AD-5** | Ownership auto-transfer on customer verification | (a) Remove entirely; (b) keep, backend only; (c) keep, explicit user action | **(c) or (a)** — a verification scan silently taking ownership is hard to defend in a viva |
| **AD-6** | QR code generation | Add ZXing to product-service, generate on create, store PNG + path | Belongs in product-service per brief §9. Needs a dependency + a file-storage location |
| **AD-7** | Fabric CA / wallet | (a) Implement enrolment + wallet; (b) update docs to match the static-identity approach | **(b)** for the timeline — (a) is a multi-day addition |
| **AD-8** | `FraudReportRequest` needs `reportedByUserId` + `fraudType` | The endpoint cannot work without them | Add both (or derive `reportedByUserId` from the JWT once AD-1 lands) — a small request-DTO addition |

---

## 7. Fix Plan

Batches are ordered by risk reduction per unit of effort. **Batches 1–5 change no architecture and need
no approval.** Batches 6–8 depend on the decisions above.

### Batch 1 — Secrets & information disclosure *(no contract change)*
1. Externalise JWT secret and all DB credentials to env vars with dev defaults (`${JWT_SECRET:...}`); add `.env.example`. **Rotate the committed secret.**
2. Delete the token-printing debug block in `AuthServiceImpl.refreshToken`.
3. Replace `printStackTrace()` + raw `ex.getMessage()` in all three `GlobalExceptionHandler`s with SLF4J logging and a generic client message.
4. `git rm --cached` the tracked `.idea/` and `zerofake-frontend/.env`.
5. Remove `httpBasic` from fraud `SecurityConfig`; remove the unused `PasswordEncoder` bean from blockchain-service.

### Batch 2 — HTTP correctness *(no contract change)*
6. auth: handlers for `BadCredentialsException`/`DisabledException`/`AuthenticationException` → 401; duplicate email → `ConflictException` → 409; refresh failure → `BadRequestException` → 400; `getCurrentUser` → `UnauthorizedException` → 401.
7. product: validate the `updateBlockchainStatus` enum → 400 instead of 500.
8. blockchain: replace bare `RuntimeException` with a mapped exception; translate chaincode *"does not exist"* → `ResourceNotFoundException` → 404, endorsement failure → 502.

### Batch 3 — **Fix counterfeit detection (F1)** *(the single highest-value fix)*
9. In fraud's `verifyProduct`, catch `FeignException` from the blockchain call and translate 404 / *"does not exist"* into `authentic=false` + `BLOCKCHAIN_MISMATCH` + risk 100, instead of letting it become a 500. Depends on step 8.

### Batch 4 — Fraud engine correctness
10. Apply the missing `+35` for `MULTIPLE_LOCATION_SCAN`.
11. Change `DUPLICATE_QR` to the specified flat `+30`.
12. Add `@Transactional` to `verifyProduct`.
13. Remove the duplicated `calculateRiskScore` (internal service interface, not an HTTP contract).
14. Fix `createFraudReport` — **needs AD-8**.

### Batch 5 — Blockchain integrity & hygiene
15. Replace the stack-trace string-sniffing in `registerProduct` with a single typed check; on already-registered, **return the existing persisted transaction instead of fabricating one**.
16. Remove the fabricated `query-<uuid>` transaction ID from `verifyProduct`.
17. Make gateway/contract initialisation thread-safe.
18. Chaincode: use the `productExists` helper in `RegisterProduct`.
19. Delete `supply-chain-service`; remove dead mapper methods, unused exceptions, unused repository method, duplicated imports; fix the product Swagger title; add `lombok-mapstruct-binding` to the fraud pom.
20. Align the frontend `TransactionType` / `FraudStatus` / `BlockchainStatus` types and the `register` envelope type with the backend.

### Batch 6 — **Authentication across services** *(needs AD-1, AD-2)*
21. Add `userId` and `role` claims to the JWT.
22. Add a JWT validation filter + `SecurityConfig` with `@PreAuthorize` role rules to product, blockchain and fraud services.
23. Lock `/register` down so only `ROLE_ADMIN` can create privileged roles (fixes A2).
24. Derive `userId`/`userRole` in fraud verification from the token rather than the request body (fixes F7).

### Batch 7 — Deployment *(needs no decision, but is new work)*
25. `Dockerfile` per service (multi-stage), `docker-compose.yml` with Postgres + all four services + frontend, `.env.example`, health checks, and a documented startup order. Fabric stays on `test-network`; the compose file joins its network.
26. Make Fabric paths env-driven (fixes B2); parameterise `deploy-chaincode.sh`.
27. Rewrite the README to match reality: real structure, real prerequisites, real run instructions.

### Batch 8 — Feature completion *(needs AD-3, AD-4, AD-5, AD-6)*
28. QR code generation in product-service; remove the `dropCheckConstraint` hack; move `blockchainStatus` updates out of the browser; resolve the ownership auto-transfer duplication.

---

## 8. Quick Wins

Highest impact per minute of work, all inside Batches 1–5:

1. **Batch 3 (step 9 + 8)** — makes the core product actually detect counterfeits. *~30 min.*
2. **Steps 1–4** — removes the committed secret and stops leaking stack traces. *~30 min.*
3. **Step 6** — login failures return 401 instead of 500. *~20 min.*
4. **Steps 10–11** — the risk engine matches its own specification. *~15 min.*
5. **Step 19** — deletes the dead module and dead code; the repo stops looking unfinished. *~30 min.*

---

## 9. Final Readiness Assessment

| Purpose | Ready? | Blocker |
|---|---|---|
| Compiles / runs locally for the author | ✅ Yes | — |
| **College viva / demo** | 🟡 Risky | F1 will 500 the moment an examiner scans an unregistered product — the exact demo an examiner will ask for. B3's fabricated transaction IDs are the question you least want asked |
| **Public GitHub repo** | 🔴 No | Committed JWT secret; no Docker; README describes a structure that doesn't exist; dead module |
| **Resume / interview claim** | 🟡 Partly | The architecture genuinely supports the claim. "Secured with JWT + RBAC" does not survive one follow-up question while 3 of 4 services are `permitAll()` |
| Production | 🔴 No | Out of scope for this project — correctly so |

**Bottom line:** Batches 1–5 (roughly one focused day) move this from *"impressive but breaks under
questioning"* to *"solid and defensible."* Batch 6 (authentication) plus Batch 7 (Docker) are what make
the README's own claims true. Batch 8 is polish.
