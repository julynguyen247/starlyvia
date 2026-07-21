---
name: generate-readme
description: Generate or refresh a repository README from evidence in the existing codebase. Use when the user asks to create, regenerate, update, audit, or improve README.md; document an undocumented project; synchronize setup, architecture, configuration, usage, API, or test instructions with code; or replace placeholder documentation with accurate project guidance.
---

# Generate README

Create a useful README whose claims and commands are traceable to the current repository rather than assumptions or generic boilerplate.

## Rules

- Read every applicable `AGENTS.md` before editing documentation.
- Inspect the existing README and preserve accurate, intentional content, user edits, tone, links, and project terminology.
- Derive facts from source code, manifests, lockfiles, build files, entry points, configuration, tests, CI, containers, migrations, API definitions, and existing docs.
- Distinguish verified behavior from inference. Omit details that cannot be supported by repository evidence.
- Never invent badges, URLs, license terms, deployment status, performance claims, compatibility promises, environment defaults, or test results.
- Never expose secret values. Document variable names and safe placeholders only.
- Use commands that match the checked-in toolchain, such as repository wrappers and declared scripts. Do not substitute globally installed tools without evidence.
- Keep the README proportional to the project. Prefer concise task-oriented guidance over exhaustive source listings.
- Use relative links for repository files and verify their targets.
- Preserve unrelated worktree changes and avoid broad documentation rewrites when a focused refresh satisfies the request.

## Workflow

1. Establish scope and audience.
   - Determine whether to create a missing README, refresh the full document, or update only requested sections.
   - Infer the primary reader from the repository: user, integrator, contributor, operator, or library consumer.
2. Inventory the project.
   - Identify languages, frameworks, package/build tools, modules, executables, infrastructure, and top-level documentation.
   - Inspect ignored files so generated output and local-only state are not documented as source artifacts.
3. Trace how the software works.
   - Read entry points and major module boundaries.
   - Trace public APIs, ports, service dependencies, persistence, events, external providers, and authentication only as relevant.
   - Inspect tests and CI to learn supported validation commands.
4. Extract operational facts.
   - Find prerequisites, install/build/run commands, required services, environment variable names, configuration precedence, and safe example values.
   - Confirm whether commands run from the repository root or a module directory.
5. Design the document.
   - Include only useful sections, commonly: overview, status, architecture, project structure, prerequisites, setup, usage, API examples, configuration, testing, deployment, limitations, and contributing guidance.
   - Put the shortest successful path early. Move details after the quick start.
   - Use tables or Mermaid only when they materially clarify mappings or multi-component flows.
6. Write or update `README.md`.
   - Reconcile existing text with current code instead of replacing accurate prose reflexively.
   - Use runnable fenced commands and realistic placeholders.
   - Describe current limitations plainly when the code demonstrates them.
7. Review the diff for factual accuracy, accidental deletions, leaked values, stale counts, and unsupported claims.

## Validation

Validate the finished README by checking:

- every referenced path and relative Markdown link exists;
- every documented script, wrapper, port, endpoint, module, and environment variable matches repository evidence;
- code fences are balanced and Markdown headings form a sensible hierarchy;
- sample commands use the correct working directory and do not perform destructive actions unexpectedly;
- no credentials, tokens, private endpoints, or machine-specific absolute paths were introduced;
- `git diff --check -- README.md` passes;
- available lightweight documentation linting passes when already configured in the repository.

Run application tests or builds only when necessary to verify a material README claim and when doing so stays within the user's requested scope. Report unexecuted commands as unverified; do not present them as successful.

## Output Format

Return:

```text
README
- <created or updated path>
- <major sections added, removed, or reconciled>

Evidence used
- <manifest, entry point, configuration, test, or documentation source>

Validation
- <command or check>: <pass/fail and concise result>

Unverified
- <claim or command not executed, or "None">
```
