---
name: feature-commits
description: Split an existing Git worktree into focused, reviewable commits that follow Conventional Commits. Use when the user asks to organize, split, stage, or commit completed changes; clean up a mixed worktree into logical commits; or prepare local commit history for review. Do not trigger for implementation-only requests that do not authorize commits.
---

# Feature Commits

Turn completed worktree changes into the smallest coherent sequence of Conventional Commits while preserving unrelated user work.

## Rules

- Read every applicable `AGENTS.md` and inspect `git status --short`, the current branch, staged changes, unstaged changes, and untracked files before modifying the index.
- Treat pre-existing changes as user-owned. Include a file or hunk only when its relationship to the requested feature is clear.
- Do not discard, rewrite, stash, reset, amend, rebase, merge, or push unless the user explicitly requests that operation.
- Do not commit secrets, generated build output, IDE state, temporary files, or unrelated formatting.
- Preserve the user's staged state. If already-staged content is unrelated or mixed with the requested work, stop and explain the conflict instead of silently unstaging it.
- Make each commit represent one reason to change and remain understandable on its own. Keep implementation and its focused tests together.
- Order prerequisite commits before dependants, such as contracts before consumers, schema before application use, and refactors before behavior that depends on them.
- Prefer `type(scope): imperative subject`; omit the scope only when no concise scope is useful. Use `!` and a `BREAKING CHANGE:` footer only for genuine breaking changes.
- Select the narrowest Conventional Commit type: `feat`, `fix`, `refactor`, `test`, `docs`, `build`, `ci`, `perf`, `style`, `chore`, or `revert`.
- Keep the subject concise, lowercase unless a proper noun requires otherwise, and free of a trailing period.
- Never claim a commit or validation succeeded without checking the command result.

## Workflow

1. Establish constraints.
   - Read repository instructions and the user's request.
   - Record the branch, HEAD, worktree status, and existing staged content.
   - Identify changes that are clearly in scope and flag ambiguous or unrelated changes.
2. Understand the diff.
   - Review `git diff --stat`, `git diff`, `git diff --cached`, and relevant untracked files.
   - Trace dependencies between production code, tests, contracts, configuration, migrations, and documentation.
3. Design the commit series.
   - Partition changes by behavior and dependency, not merely by file type.
   - Keep mechanical refactors separate from behavioral changes when each can stand alone.
   - Avoid commits that knowingly leave the repository uncompilable unless an unavoidable cross-commit migration is clearly documented.
4. Stage one commit at a time.
   - Stage exact files when the entire file belongs to one commit.
   - For a file containing changes for multiple commits, stage only verified hunks. Reinspect the cached diff before committing.
   - Do not use broad staging commands such as `git add .` when unrelated changes exist.
5. Validate the candidate.
   - Run the cheapest relevant checks for the staged unit when practical.
   - Inspect `git diff --cached --check`, `git diff --cached --stat`, and the complete cached patch.
   - Confirm no secret, unrelated file, or incomplete dependency is staged.
6. Commit.
   - Create the commit with a Conventional Commit message derived from the staged behavior.
   - Verify the new commit with `git show --stat --oneline --decorate --no-renames HEAD` and confirm the remaining worktree state.
7. Repeat until all authorized changes are committed or a safe split is impossible.
8. Review the resulting range from the original HEAD and report any intentionally uncommitted changes.

## Validation

For every proposed commit, verify:

- the cached diff contains one coherent change and no unrelated content;
- whitespace checks pass with `git diff --cached --check`;
- affected tests, linters, builds, or contract checks pass, or their absence/failure is reported exactly;
- the commit subject matches `type(scope): subject` or another valid Conventional Commits form;
- the final `git log` order reflects dependency order;
- `git status --short` contains only expected remaining changes.

If validation fails, do not create that commit. Fix only in-scope issues, restage, and validate again. If fixing would alter user-owned work or expand scope, stop and ask for direction.

## Output Format

Return:

```text
Commits created
- <short-sha> <conventional subject> — <purpose>

Validation
- <command>: <pass/fail and concise result>

Remaining worktree
- <path or "clean"> — <why it remains>

Not performed
- No push, merge, rebase, or amend performed.
```

If no commit is safe or authorized, replace `Commits created` with `No commits created` and state the exact blocker.
