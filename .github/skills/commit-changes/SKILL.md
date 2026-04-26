---
name: commit-changes
description: Full git workflow — creates a branch, stages changes, runs lint analysis, surfaces warnings/errors for user decision, drafts a Conventional Commits message and commits.
argument-hint: [optional: short summary of the change]
disable-model-invocation: true
allowed-tools: Bash, Read, Glob
---

# Commit Changes: $ARGUMENTS

Guide the developer through the complete git workflow.

> **Note:** `$ARGUMENTS` is an optional short summary hint. If omitted, derive context from the diff.

> ⛔ **HARD RULE — confirmed at every step below:** Never run `git commit`, `git push`, `git merge`, `git rebase`, `git tag`, or any other git write operation without explicit user confirmation. Present the exact commands you plan to run and wait for the user to say yes before executing them.

---

## Step 1 — Inspect Current Changes

Run the following to understand what is staged and what is not:

```bash
git status
git diff --stat
git diff --cached --stat
```

From the output:
- List the **affected feature/core modules** (e.g. `feature/login`, `core/firebase`, `navigation/api`)
- Identify the **nature of the change**: new feature, bug fix, refactor, style, chore, docs, test
- If the working tree is clean and nothing is staged, stop and inform the user there is nothing to commit.

---

## Step 2 — Create or Verify Branch

Check the current branch:

```bash
git branch --show-current
```

**If already on a valid branch** — name matches the full pattern `<prefix>/<non-empty-kebab-case-suffix>` where prefix is one of `feature`, `fix`, `refactor`, `style`, `chore`, `docs`, `test`, and the suffix contains only lowercase letters, digits, and hyphens (e.g. `feature/login-component` ✅, `feature/` ❌, `feature/foo_bar` ❌) — skip to Step 3.

**Otherwise**, derive the branch name automatically from the diff:

| Change type | Branch prefix |
|-------------|---------------|
| New feature | `feature/` |
| Bug fix | `fix/` |
| Code refactor (no behaviour change) | `refactor/` |
| Style / formatting | `style/` |
| Build, tooling, CI | `chore/` |
| Documentation | `docs/` |
| Tests | `test/` |

- Infer the **type** from the nature of the changes observed in Step 1.
- Infer the **suffix** from the affected modules and the change description — use **kebab-case** (e.g. `feature/login-component`, `fix/firebase-auth-crash`).
- If `$ARGUMENTS` was provided, use it to inform the suffix.
- **Only ask the user** if the type or description cannot be confidently determined from the diff.
- Propose the derived name to the user before creating it:

  > "I'll create branch `<derived-branch-name>`. Confirm or suggest a different name."

- Create the branch as a **git worktree** (never `git checkout -b`):

```bash
git worktree add ../<repo-name>-<branch-dir> -b <branch-name>
```

Then `cd` into the new worktree directory to continue working there.

---

## Step 3 — Stage Changes

Stage all changed files automatically:

```bash
git add .
```

Confirm what is staged:

```bash
git diff --cached --stat
git diff --cached --name-only
```

Show this output to the user before continuing.

---

## Step 4 — Run Analysis

Determine which command to run:

1. **Try lint first:**
   ```bash
   ./gradlew lint 2>&1
   ```
   - If lint **succeeds or exits non-zero due to findings** → use this output. Do **not** fall back.
   - If lint **fails because the task does not exist** (e.g. `Task 'lint' not found`) or **Gradle configuration fails before lint runs** → fall back to step 2.

2. **Fallback — only when the `lint` task is unavailable:**
   ```bash
   ./gradlew build 2>&1
   ```

> ⚠️ A non-zero exit code from lint caused by actual findings is **not** a reason to fall back. Treat it as real findings and surface them to the user.

**Parse the output** using strict diagnostic patterns to avoid false positives (e.g. summary lines like "0 errors, 0 warnings" must not be counted):

- **Prefer the Gradle task exit code** as the primary signal: a non-zero exit code means real issues exist.
- When available, prefer reading the structured lint report (XML at `app/build/reports/lint-results*.xml` or HTML equivalent) over console parsing.
- If falling back to console parsing, match only genuine diagnostic lines using word-boundary patterns:
  - **Errors** — lines matching `\berror:` (case-insensitive, in lint/compiler diagnostic sections)
  - **Warnings** — lines matching `\bwarning:` (case-insensitive, in lint/compiler diagnostic sections)

**Always show the findings to the user**, even if there are none:

- If there are **no warnings or errors**: inform the user and continue to Step 5.
- If there are **warnings and/or errors**: display them clearly (grouped as Errors / Warnings) and ask:

  > "The analysis found the items above. Do you want to **abort** the commit to fix them, or **proceed** anyway?"

  - If the user chooses **abort**: stop here and remind them to fix the issues, then re-run the skill.
  - If the user chooses **proceed**: continue to Step 5. Do **not** include the findings in the commit message.

---

## Step 5 — Draft Commit Message

Build a **Conventional Commits** message using the context from Steps 1–4.

### Subject line (≤ 72 characters)

```
<type>(<scope>): <short imperative description>
```

| Field | Rules |
|-------|-------|
| `type` | `feature`, `fix`, `refactor`, `style`, `chore`, `docs`, `test` — match the nature of the change |
| `scope` | Affected module(s) as kebab-case or slash-separated module path: e.g. `login`, `core/firebase`, `navigation` — omit if change spans too many |
| `description` | Imperative mood, lowercase, no trailing period (e.g. `add login component`, `remove i18n module`) |

If `$ARGUMENTS` was provided, use it as a starting point for the description.

### Body (optional but recommended)

- Explain **what** changed and **why** (not how — the diff shows that)
- Use plain prose or short bullet points
- Wrap at 72 characters per line

### Footer (optional)

Add only if the project requires specific trailers. This project does not require any standard footer.

### Full message template

```
<type>(<scope>): <description>

<body — what and why>
```

**Show the full drafted message to the user** and ask them to confirm or suggest edits before committing.

---

## Step 6 — Commit

Once the message is confirmed, commit using stdin to avoid temp files:

```bash
git commit -F - <<'EOF'
<full commit message>
EOF
```

After the commit succeeds:
- Print the commit SHA: `git log -1 --oneline`
- Remind the user to push: `git push -u origin <branch-name>`
