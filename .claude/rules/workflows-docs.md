---
paths:
  - ".github/workflows/**"
---

# Workflows: keep `docs/workflows.md` in sync

- Every time a file in `.github/workflows/` is added, changed or removed, update `docs/workflows.md` in the same change.
- Update the part the change affects: the workflow table, triggers, change-detection paths, job chain, deploy behavior, manual run, rollback or permissions.
- If the change affects server setup or secrets, also update `docs/deployment.md`.
- Never leave `docs/workflows.md` describing behavior the workflows no longer have.
