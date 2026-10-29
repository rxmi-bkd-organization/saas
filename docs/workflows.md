# CI/CD workflows

All workflows live in `.github/workflows/`. For the server and secrets setup, see [deployment.md](deployment.md).

| Workflow | File | Runs on | Purpose |
|---|---|---|---|
| test | `test.yml` | pull request, push to `develop`, called by `build and deploy` | Backend `./mvnw verify`, frontend `npm run build` |
| format and lint | `format-and-lint.yml` | pull request, push to `develop`, called by `build and deploy` | Backend `spotless:check`, frontend `format:check` and `lint:check` |
| changes | `changes.yml` | called by `test` and `format and lint` | Detects which side of the repo changed |
| build and deploy | `build-and-deploy.yml` | push to `master`, manual run | Builds and pushes the images, then deploys to the server |

## Change detection

`changes.yml` uses `dorny/paths-filter` and outputs two booleans:

| Output | `true` when these paths change |
|---|---|
| `backend` | `backend/**`, `.github/workflows/**` |
| `frontend` | `frontend/**`, `.github/workflows/**` |

In `test` and `format and lint`, the `backend` job runs only if `backend` is `true`, and the `frontend` job only if `frontend` is `true`. A change that touches neither (docs, `.claude/**`, root files) skips both jobs, and the run still ends green.

Editing any file in `.github/workflows/` runs both sides.

## Build and deploy

```
test ────────────┐
                 ├─> build (matrix: backend, frontend) ─> deploy
format and lint ─┘
```

- **Trigger:** every push to `master` runs the whole chain, including docs-only pushes. Inside `test` and `format and lint`, the change detection above still applies.
- **Build:** pushes `ghcr.io/<owner>/<project>-backend` and `-frontend`, each tagged `sha-<short sha>` and `latest`.
- **Deploy:** copies `compose.production.yml` to the server, then runs `docker compose pull` and `up -d --wait --wait-timeout 300 --remove-orphans`. `--wait` makes the job fail if a service is not healthy after 5 minutes. A failed deploy does not roll back: the new containers stay up until you fix them.
- **Concurrency:** deploys are queued (`group: deploy`, no cancellation), so two pushes never deploy at the same time.

### Manual run

Run it from the Actions tab (`build and deploy` > Run workflow). It asks for a required reason, so there is a record of why the deploy happened.

`build` and `deploy` only run when the selected ref is `master`. Starting it from another branch runs the tests and lint, then skips the build and deploy, so a branch can never be deployed by accident.

## Rolling back

- **Revert on `master`:** `git revert <commit>` and push. The pipeline tests, builds and deploys the reverted code.
- **Directly on the server:** each build also pushes a `sha-<short sha>` tag, so an earlier image can be started without rebuilding:
  ```bash
  cd /home/github-actions/<project>
  IMAGE_TAG=sha-xxxxxxx docker compose -f compose.production.yml up -d --wait
  ```
  The tag only applies to that command. The next deploy goes back to `latest`.

## Permissions

Workflows request `contents: read`. `build and deploy` also requests `packages: write` to push images to ghcr.io. `changes.yml` needs no extra permission because the repository is public. If it becomes private, add `pull-requests: read` to `changes.yml`, `test.yml`, `format-and-lint.yml` and `build-and-deploy.yml`, because a called workflow cannot hold more permissions than its caller.
