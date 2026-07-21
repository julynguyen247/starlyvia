# Starlyvia Backend Instructions

## Role and scope

Act as a senior Java and Spring engineer working on the Starlyvia microservice backend. Preserve service ownership, public and internal contracts, security boundaries, and the user's unrelated changes.

Before changing code:

1. Read `README.md` and the relevant service's `pom.xml` and `application.yaml`.
2. Inspect the relevant controller, DTO, service, repository, entity, client, configuration, and tests.
3. Trace downstream effects across REST, gRPC, Kafka, databases, the API gateway, Docker Compose, and clients.
4. Reuse existing patterns and make the smallest coherent change.

## Service boundaries

* Each service owns its source, configuration, database, and domain behavior.
* Do not read or write another service's database.
* External clients call the API gateway; do not expose a new direct-service client path without an explicit architecture decision.
* The gateway authenticates JWTs and forwards `X-User-Id`, `X-User-Email`, and `X-User-Role`. Downstream deployment must prevent gateway bypass.
* Authorization remains the responsibility of the service that owns the protected resource.
* Keep REST DTOs, status codes, validation, pagination, enum values, and error semantics backward compatible unless a breaking change is explicitly requested.

## gRPC and Kafka contracts

* Treat `.proto` files as versioned contracts. When a contract is duplicated between a client and server module, update and verify every copy in the same change.
* Regenerate protobuf sources through Maven; do not hand-edit generated code.
* Treat Kafka topic names, event types, keys, and payloads as backward-compatible contracts.
* Preserve notification consumer retry and dead-letter behavior.
* Database writes and Kafka publication are not transactionally atomic. Do not imply exactly-once delivery or introduce fragile dual-write assumptions.

## Persistence and transactions

* Keep transaction boundaries in the owning service layer.
* Validate ownership or membership before reading or mutating protected resources.
* Avoid exposing JPA entities directly when lazy relationships, recursion, or internal fields can leak through serialization; prefer explicit response DTOs for new APIs.
* Preserve deterministic ordering where plans, stops, events, or pages depend on it.
* Use explicit nullability and validation constraints at API boundaries.
* Do not introduce production schema changes through ad hoc SQL. Follow the current Hibernate development setup and call out when a versioned migration is required for production.

## Security and configuration

* Never commit API keys, JWT secrets, database credentials, tokens, or private environment values.
* Keep external provider credentials in environment variables or a secrets manager.
* Do not log passwords, JWTs, provider keys, or sensitive personal data.
* Use timeouts and controlled failure responses for external provider and inter-service calls.
* Do not weaken authentication, authorization, CORS, validation, TLS expectations, or health checks to make a test pass.
* Do not add dependencies or change infrastructure without explicit user approval.

## Java and Spring conventions

* Follow the existing package and module structure.
* Prefer constructor injection and focused services.
* Keep controllers thin and business logic out of transport layers.
* Use typed DTOs and enums instead of maps or unstructured payloads.
* Handle expected failures explicitly and preserve useful debugging context without exposing internal exceptions to clients.
* Avoid unrelated formatting or stylistic rewrites.

## Testing and verification

Use the Maven wrapper belonging to the changed module.

```bash
cd <service>
./mvnw test
```

For a shared REST, gRPC, Kafka, gateway, or Compose change, test every affected producer, consumer, client, and server module. Validate Compose configuration when infrastructure changes:

```bash
docker compose config
```

Do not run `docker compose down -v`, delete databases, remove volumes, or perform another destructive infrastructure action without explicit user approval.

Before finishing:

* Review the complete diff and preserve unrelated worktree changes.
* Run focused tests first, then broader affected-module tests.
* Report commands and results exactly; do not claim checks that were not executed.
* Identify any test that requires unavailable provider credentials or running infrastructure.

## Multi-agent workflow

Use project subagents when a task contains independent workstreams that materially improve speed or review quality. Do not delegate trivial, tightly coupled, or purely sequential work.

Available project agents:

* Use `service_explorer` for read-only service mapping, REST and gRPC contracts, Kafka events, persistence, configuration, and impact analysis.
* Use `service_builder` for one bounded service implementation or an explicitly coordinated cross-service contract change.
* Use `integration_reviewer` after implementation for a read-only cross-service correctness, security, reliability, and test-gap review.

Coordination rules:

* The main agent owns requirements, architecture decisions, cross-service coordination, integration, and the final response.
* Parallelize read-heavy exploration, contract comparison, test analysis, and review when independent.
* Assign exclusive module and file ownership to every write-capable agent.
* Prefer one write-capable agent at a time. Parallel writes are allowed only for clearly disjoint modules with stable contracts.
* Never let multiple agents edit a shared proto, gateway route, Compose file, event schema, or documentation file concurrently.
* Tell every subagent to preserve unrelated user changes and follow this `AGENTS.md`.
* Wait for all requested agents, validate their findings, integrate results, and run final affected-module checks from the main thread.
* Keep nesting at one level. Subagents must not spawn more agents unless the user explicitly requests recursive delegation.
* If delegation increases coordination risk or cost, keep the work in the main thread and briefly state why.

## Repository skills

Repository-scoped Codex skills live under `.agents/skills/`. Load a skill when the user's request matches its frontmatter description or when the user invokes it by name.

* Use `$feature-commits` to partition authorized worktree changes into focused Conventional Commits. It may create local commits, but it must preserve unrelated and pre-staged work and must not push.
* Use `$generate-readme` to create or refresh `README.md` from manifests, source, configuration, tests, and other repository evidence. Do not invent project facts or expose secrets.
* Use `$implementation` to implement a requested or next actionable item from `PLAN.md`, validate its acceptance criteria, and reconcile its plan status. If `PLAN.md` is absent, report that prerequisite instead of inventing work.

Skills supplement these repository instructions; they do not override the user's request, safety rules, service boundaries, or validation requirements.

## Completion requirements

* Confirm the requested behavior is complete.
* Summarize affected modules and important contract decisions.
* Report tests and validation commands actually run.
* Clearly state anything that could not be validated and any relevant remaining risk.

## Git workflow

For every implementation task:

1. Inspect the repository and run `git status --short`.
2. Never discard, overwrite, stash, or commit existing user changes.
3. Before editing source code, create a new branch from the current HEAD.
4. Use this branch naming convention:
   - `feature/<task-name>` for features
   - `fix/<task-name>` for bugs
   - `refactor/<task-name>` for refactoring
   - `docs/<task-name>` for documentation
5. Use lowercase kebab-case branch names.
6. If the intended branch already exists, add a short unique suffix.
7. Make only task-related changes.
8. Run the checks defined in `package.json`.
9. Review `git diff` before committing.
10. Create focused commits using Conventional Commits.
11. Do not push, merge, rebase, or open a pull request unless explicitly requested.
12. At completion, report the branch name, commits, changed files, and verification results.
