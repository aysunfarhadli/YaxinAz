# Yaxın.az

AI-powered smart community & local services platform. Residents report and track building/neighborhood
issues, run community feeds/polls/events/lost-and-found, and book vetted local service providers — with
Claude used for issue triage, duplicate detection, announcement drafting, and natural-language search
query interpretation (never for autonomous actions: AI proposes, a human always confirms before
anything is posted or changed).

Modular monolith: **Spring Boot 4.1.1**, **Java 17**, **Gradle**, **PostgreSQL** (Flyway-migrated),
**JWT auth**, vanilla-JS frontend served as static resources — no separate frontend build step.
Full UI is available in **English, Azerbaijani, and Russian** (switchable at any time, including
pre-login, from the language selector in the top-right corner).

## Tech stack

| Concern | Choice |
|---|---|
| Language / runtime | Java 17, Spring Boot 4.1.1 |
| Persistence | PostgreSQL 16 + Flyway migrations (`src/main/resources/db/migration`); H2 in-memory for tests |
| Auth | Stateless JWT (jjwt), BCrypt password hashing, role-based access control |
| AI | Spring AI + Anthropic (Claude), with a mock provider fallback when no API key is configured |
| Realtime | STOMP over WebSocket (live notifications) |
| Caching / rate limiting | Caffeine (in-memory; no Redis dependency by design) |
| Mapping | MapStruct (entity ↔ DTO), Lombok |
| API docs | springdoc-openapi / Swagger UI (dev only) |
| Frontend | Static HTML/CSS/vanilla JS under `src/main/resources/static` — no build step, no framework |
| CI | GitHub Actions (`.github/workflows/ci.yml`) — `./gradlew clean build` on every push/PR to `main` |

## Domains

`auth`, `user`, `community`, `issue`, `feed` (posts/comments/likes), `poll`, `event`, `lostfound`,
`provider` (service-provider marketplace), `servicerequest`, `review`, `notification`, `moderation`,
`audit`, `ai`, `analytics`, `search`, `health`, `storage` (file uploads), `seed` (dev demo data).

Four roles: `RESIDENT`, `COMMUNITY_ADMIN`, `SERVICE_PROVIDER`, `PLATFORM_ADMIN`.

## Running locally

**Requirements**: JDK 17, Docker Desktop (for Postgres — or point `DB_URL` at your own instance).

```bash
# 1. Start Postgres
docker compose up db -d

# 2. Copy the env template and fill in real values (JWT_SECRET, ANTHROPIC_API_KEY if you have one)
cp .env.example .env

# 3. Run the app against it (dev profile: Postgres, Flyway on, Swagger on, seeds demo data)
./gradlew bootRun --args='--spring.profiles.active=dev'
```

The app comes up on `http://localhost:8080`. On first boot against an empty database, `DemoDataSeeder`
(dev profile only) seeds a realistic community with issues, posts, polls, events, providers, and reviews,
plus these accounts (password `Demo1234!` unless noted):

| Email | Role |
|---|---|
| `platform@yaxinaz.az` | PLATFORM_ADMIN |
| `admin@yaxinaz.az` | COMMUNITY_ADMIN |
| `resident@yaxinaz.az` | RESIDENT |
| `provider@yaxinaz.az` | SERVICE_PROVIDER |
| `admin@test.com` (password `1234`) | PLATFORM_ADMIN, minimal quick-login account |
| `user@test.com` (password `1234`) | RESIDENT, minimal quick-login account |

Swagger UI: `http://localhost:8080/swagger-ui.html`. Actuator health: `/actuator/health`.

### Full stack via Docker Compose

```bash
docker compose up --build
```

Builds the app image (multi-stage `Dockerfile`), starts Postgres with a healthcheck gate, and runs the
app in `prod` profile against it.

## Testing

```bash
./gradlew clean build
```

Runs the full backend suite (JUnit 5 + Spring Boot Test + MockMvc, H2 in-memory, Flyway disabled,
`test` profile) — no network calls, no real Postgres or Claude API required. Frontend has no automated
test suite (plain HTML/JS, no build tooling in the stack); it's been verified with scripted Playwright
runs against a live instance + seeded demo data during development, not checked into the repo.

## Configuration

All runtime config is environment-variable driven — see `.env.example` for the full list (database,
JWT, AI provider, storage, server port). Profile-specific defaults live in
`src/main/resources/application-{dev,prod,test}.yml`; shared defaults in `application.yml`.

Notable feature flags (`yaxinaz.features.*` / `yaxinaz.rate-limit.*` in `application.yml`): AI can be
fully disabled (`AI_ENABLED=false`) or run with a mock fallback (`MOCK_AI_FALLBACK=true`, the default)
so the app works with zero Claude API cost; rate limits apply per-user per-minute to login, AI requests,
issue creation, announcement rewrites, and moderation reports.

## Project state

See `progress.md` for the full build history, architecture decisions, and what's been verified in each
phase — it's the working log this project was built from and is more detailed than this README.
