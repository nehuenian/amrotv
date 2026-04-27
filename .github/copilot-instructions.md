# Copilot Instructions — AMRO

This is an **Android-only** app built with:
**Jetpack Compose + MVI + Dagger Hilt + Navigation Compose + Retrofit (TMDB API)**.

These instructions describe the **target architecture that all new code must follow**.

---

## Skills — always reference these before generating or reviewing code

The project ships Copilot skills in `.github/skills/`. **Always load the relevant skill(s)
before writing or reviewing any code.** They encode all conventions, patterns, and decisions
for this codebase; code that contradicts a skill is wrong by definition.

| Skill | When to use |
|-------|-------------|
| `architecture-reference` | **Auto-load for every task.** Covers the full module structure, MVI pattern, Hilt DI setup, clean architecture layering, naming rules, Compose conventions, and code templates. |
| `create-feature-module` | Scaffolding all 6 sub-modules for a new feature (`data`, `domain:api`, `domain:implementation`, `presentation:api`, `presentation:implementation`, `ui`). |
| `create-screen` | Adding a new MVI screen to an existing feature module (State, Intent, Effect, ViewModel, Screen, Content). |
| `compose-skill` | Reviewing, building, or refactoring any Jetpack Compose code — recomposition, state hoisting, side effects, navigation, DI, testing, accessibility. |
| `commit-changes` | Full git commit workflow — branch creation, staging, lint checks, Conventional Commits message format, and co-author trailer. Use before every commit. |

---

## Documentation

All `interface` declarations and their members **must have KDoc**. Abstract base classes (e.g. `MviViewModel`, `RemoteMovieDataSource`) also require KDoc. Implementation classes and test code do **not** need KDoc.

See the **KDoc Conventions** section in `architecture-reference` for the full format, examples, and anti-patterns.

---

## Tooling

Use the right tool for each domain — never substitute one for another:

| Task | Tool |
|------|------|
| Android development (build, deploy, SDK, AVD) | `android-cli` skill — wraps `sdkmanager`, `avdmanager`, `adb`, and Gradle CLI |
| GitHub operations (PRs, issues, workflows, releases) | `gh` CLI |
| Kotlin code intelligence (go-to-definition, find-references, hover, rename) | LSP — use the `lsp-setup` skill to configure the Kotlin language server if not already active |

---

## Git — hard stops

> **NEVER execute any git write operation without explicit user confirmation.**

This is an absolute rule. No exceptions, no assumptions.

Operations that require explicit confirmation **every time**:
- `git commit` (including `--amend`)
- `git push` (including `--force`)
- `git merge` / `git rebase` / `git squash`
- `git tag`
- Creating a branch

**How to ask:** Stop, state the exact commands you are about to run, and wait for the user to say yes.

---

## Code generation

- **New feature** → use `create-feature-module` skill
- **New screen in existing feature** → use `create-screen` skill
- **Any Kotlin/Compose code** → load `architecture-reference` first

All generated code must be consistent with `architecture-reference`.

---

## Code review

Two focused agents — pick based on what changed:

| Agent | Scope | Focus |
|-------|-------|-------|
| `architecture-reviewer` | `domain/`, `data/`, `presentation/` logic, `core/`, `libraries/`, `build.gradle.kts` | Module boundaries, clean arch layers, Hilt DI, MVI contracts, logging, naming, KDoc, build files, data/networking/testing patterns |
| `compose-reviewer` | `ui/` modules, `*Screen.kt`, `*Content.kt`, theme files | Recomposition, state hoisting, side effects, Screen/Content split, Material3 tokens, accessibility, previews |

**Routing rules:**
- Changed files are **only** in `ui/` / `*Screen.kt` / `*Content.kt` → run `compose-reviewer` only
- Changed files are **only** non-UI (domain, data, presentation logic, build files) → run `architecture-reviewer` only
- Changed files include **both** → run both agents in parallel

### Code review loop

After applying changes:
1. Determine which agent(s) apply based on the routing rules above.
2. Invoke the relevant agent(s) (in parallel if both).
3. Collect all comments.
4. Address every comment.
5. Re-run the same agent(s).
6. Repeat until all relevant agents report no remaining issues.

---

## Workflow

### Branch strategy

Create a new branch for each feature or commit phase. Use worktrees to keep the main tree clean:
```bash
git worktree add ../amrotv-<branch-name> -b <branch-name>
# Example: git worktree add ../amrotv-feature-movies-domain -b feature/movies-domain
```

### Before triggering a code review

Ask the user: *"Should I run the review agents on these changes?"*
Only proceed after explicit confirmation.

### Before committing after review

> **STOP. Do not run any git command yet.**

State exactly what you are about to run and wait for the user to say yes:
```
I'm ready to commit. The following commands will run — should I proceed?
  git add <files>
  git commit -m "feat(movies): add domain layer"
```

### Before merging or pushing

Always ask for explicit confirmation before any `git push` or `git merge`.

---

## Commit conventions

All commit workflow details — branch creation, staging, lint checks, message format, and the Conventional Commits rules for this project — are defined in the `commit-changes` skill:

```
read .github/skills/commit-changes/SKILL.md
```

Use the `commit-changes` skill whenever you are about to commit. Do not bypass the workflow defined there.
