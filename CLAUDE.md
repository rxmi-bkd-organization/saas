# Project rules

## Keep `docs/` in sync

- Every time you change code, config or workflows, check whether a file in `docs/` describes it, and update that doc in the same change.
- Update only the part the change affects.
- Never leave a doc describing behavior the project no longer has.
- When you add a new topic that deserves documentation, add a file in `docs/`.

## Branch names

- Format: `<type>/<short-kebab-case-description>`
- `<type>` is one of the commit types listed below (`feat`, `fix`, `refactor`, `perf`, `style`, `test`, `docs`, `build`, `ops`, `chore`), chosen to match the main purpose of the branch.
- Description: lowercase, words separated by hyphens, concise.

## Commit messages

- Format: `<type>: <description>`
- Allowed types:
  - `feat`: add, adjust or remove a feature to/of/from the API or UI
  - `fix`: fix an API or UI bug of a preceded `feat` commit
  - `refactor`: rewrite or restructure code without altering API or UI behavior
  - `perf`: special type of `refactor` that specifically improves performance
  - `style`: code style changes (white-space, formatting, missing semi-colons) that do not affect behavior
  - `test`: add missing tests or correct existing ones
  - `docs`: exclusively affect documentation
  - `build`: affect build-related components (build tools, dependencies, project version, ...)
  - `ops`: affect operational aspects (infrastructure/IaC, deployment scripts, CI/CD pipelines, backups, monitoring, recovery procedures, ...)
  - `chore`: tasks like initial commit, modifying `.gitignore`, ...

## Code style

- Always assign intermediate objects to a named local variable before passing them to a call or returning them; do not nest object creation inline in call arguments.
