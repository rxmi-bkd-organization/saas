---
name: spec-issue
description: Write a lightweight feature specification as a GitHub issue, so the spec and its tracking live in one place on the project board. Use whenever the user wants to spec, plan, scope or write up a feature, change or task ("create a spec", "write an issue for X", "spec this out", "track this on the board", "what should we build for X"), even if they never say "specification" or "issue". Also use to refine or complete an existing spec issue.
---

# spec-issue

Create a specification as a GitHub issue. The issue body is the spec, and the project board (if the repo has one) is the tracker. Do not write spec files into the repo: one source of truth means nothing goes stale in two places.

The audience is a solo developer. Future-them should be able to pick the issue up after a month away and build the right thing. So keep it short (one screen to one page), skip stakeholder sections, approvals, timelines and estimates, and write the *why* for any decision, not just the what.

## Workflow

1. **Understand the request.** Read the relevant code and any project guidance (`CLAUDE.md`, `README`, contributing docs, architecture or lint rules) so the spec fits what exists and respects the repo's conventions. Ask the user only for what you cannot infer, using the `AskUserQuestion` tool with all questions batched into one call (offer concrete options, recommended first). If they gave enough, draft straight away.
2. **Draft the body** using the template below. Show it to the user and wait for approval. Publishing an issue is outward-facing and hard to retract, so confirm before creating.
3. **Create the issue** with `gh issue create --title ... --body-file <temp file> --label ...`. Write the body to a file rather than inline to avoid shell-quoting damage. Run `gh label list` and use only labels that already exist and genuinely fit (a type label such as `enhancement` or `bug`, plus any area labels). Never create labels unprompted.
4. **Add it to the project board, if there is one.** Run `gh project list --owner <repo owner>`. If exactly one project exists, add the issue with `gh project item-add <number> --owner <owner> --url <issue-url>`. If there are several, ask which with `AskUserQuestion`. If there are none, skip this step silently. If `gh` lacks the `project` scope, tell the user to run `gh auth refresh -s project` rather than failing quietly. Leave the board's status at its default (usually the first column).
5. **Report** the issue URL, and whether it landed on a board.

If the user is refining an existing spec, `gh issue view <n>` it, edit with `gh issue edit <n> --body-file`, and keep unchanged sections intact. Close the issue only when the work ships. If a spec has stopped being useful, say so rather than leaving it stale.

## Title

Title must be short, imperative, specific.

## Body template

Use these sections in this order. Omit a section only if it is truly empty, and never pad.

```markdown
## Problem and goal
2–3 sentences: why this exists and what changes for the user.

## Scope
- What will be built.

## Non-goals
- What will not be built. Non-goals protect against scope creep when nobody else will push back.

## Acceptance criteria
- [ ] Observable, testable behavior, e.g. "Requesting a password reset for an unknown email returns the same response as for a known one."
- [ ] Each item should map to a test.

## Design decisions and constraints
- Choices you would otherwise forget the reason for, with the why: data or schema changes, integration boundaries, config, constraints from the existing architecture.

## Open questions
- Unknowns parked here so they don't block the draft.
```

Acceptance criteria are checkboxes so progress shows on the issue and the board. The board's status column carries lifecycle, so the body has no status section.

## Sizing

If the spec needs more than about a page, or has more than roughly ten acceptance criteria, the feature is too big. Propose splitting it into several issues, linked from a parent issue with task-list references (`- [ ] #12`), instead of writing one long spec.

## Quality bar

- Acceptance criteria describe behavior someone can observe, not implementation steps.
- Design decisions follow the conventions you found in the repo; name them so the reason is recorded.
- Don't invent requirements the user didn't ask for. Put guesses under Open questions.
