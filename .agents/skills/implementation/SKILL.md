---
name: implementation
description: Implement repository features defined in PLAN.md, including selecting the next actionable item, tracing its impact, changing code and tests, validating acceptance criteria, and updating plan status. Use when the user asks to execute, continue, or complete a PLAN.md; implement a named plan item or milestone; take the next task from the plan; or resume partially completed planned work.
---

# Implementation

Use `PLAN.md` as the execution backlog, then implement one coherent plan item at a time against the repository's actual architecture and constraints.

## Rules

- Read every applicable `AGENTS.md`, the root README, the complete `PLAN.md`, and relevant module instructions before editing.
- Search from the repository root for `PLAN.md`. If none exists, stop and report that implementation cannot be selected. Do not invent a plan.
- Treat the user's current request as highest priority, repository instructions as operating constraints, and `PLAN.md` as scoped requirements. Surface conflicts instead of silently choosing one.
- Do not assume a checkbox reflects reality. Inspect code, tests, configuration, and history needed to establish the current implementation state.
- Select a user-named item when provided. Otherwise choose the first incomplete, unblocked item whose dependencies are complete; use plan ordering as the tie-breaker.
- Implement the smallest coherent slice that satisfies the selected item's acceptance criteria.
- Preserve service boundaries, public contracts, security controls, data ownership, and unrelated user changes.
- Do not add dependencies, change infrastructure, alter public contracts, or expand scope unless the plan and user authorization support it.
- Never commit, push, merge, rebase, deploy, or perform destructive operations unless explicitly authorized.
- Add or update focused tests for behavior changes. Do not weaken tests or production safeguards merely to obtain a passing result.
- Update `PLAN.md` only after implementation and validation. Follow its existing status notation and preserve planning detail that remains useful.
- Mark an item complete only when all stated acceptance criteria are met. Otherwise record or report the precise partial state and blocker without overstating completion.

## Workflow

1. Load execution context.
   - Inspect repository status and preserve existing staged, unstaged, and untracked work.
   - Read the plan fully, including goals, non-goals, dependencies, acceptance criteria, validation commands, and completion notation.
2. Select the work item.
   - Honor a specifically requested item.
   - Otherwise identify incomplete items, resolve dependencies, and select the next unblocked item in document order.
   - If every remaining item is blocked, report the blockers and make no speculative changes.
3. Verify the baseline.
   - Inspect relevant source, tests, build/configuration files, contracts, consumers, and documentation.
   - Compare the repository with the selected item to identify already-complete work, missing behavior, and affected boundaries.
4. Plan the slice.
   - Translate acceptance criteria into concrete code changes and focused checks.
   - Identify contract, migration, configuration, security, or cross-module implications before editing.
5. Implement.
   - Follow existing patterns and keep the diff focused.
   - Update every required copy of shared contracts and every affected producer, consumer, client, or server.
   - Add tests close to the changed behavior and documentation only where the feature changes user or operator workflows.
6. Validate incrementally.
   - Run targeted tests first, then the broader affected-module checks required by repository instructions.
   - Run format, lint, type, build, contract, migration, or configuration checks relevant to the change.
   - Review the complete diff and worktree status.
7. Reconcile the plan.
   - Check each acceptance criterion against code and validation evidence.
   - Mark the item complete using the plan's existing convention only when all criteria pass.
   - Leave incomplete items open and record a concise blocker or follow-up when the plan format supports it.
8. Hand off the exact result without committing unless the user requested commits.

## Validation

Before declaring an item complete, verify:

- every acceptance criterion maps to an implemented behavior or artifact;
- focused tests cover the new success path and meaningful failure or boundary cases;
- all affected modules, contracts, clients, configurations, and docs remain synchronized;
- repository-prescribed checks pass, with exact commands and outcomes recorded;
- `git diff --check` passes for task files;
- the final diff contains no unrelated change, secret, generated output, or accidental deletion;
- the plan status matches the evidence and does not hide remaining work.

If a check cannot run because credentials, infrastructure, permissions, or tooling are unavailable, perform the strongest safe alternative and report the limitation. Do not mark the item complete when the missing check is itself an explicit acceptance criterion.

## Output Format

Return:

```text
Plan item
- <identifier and title>
- Status: <completed, partial, or blocked>

Implemented
- <behavior and affected module/file>

Plan update
- <status or notes changed in PLAN.md, or "No update">

Validation
- <command>: <pass/fail/not run and concise result>

Remaining
- <follow-up, blocker, risk, or "None">

Git
- No commit or push performed unless explicitly requested.
```
