# Deployment setup for a new project

This file lists the steps to deploy a new project forked from this boilerplate. It covers the secrets to create, the server to prepare, and the workflow and compose files to update so that a push to `master` builds your images and deploys them.
Every step uses the same example: owner `acme` and project `acme-crm`. Replace them with your own GitHub owner and project name.

Renaming `spring.application.name` and the `org.bkd.saas` Java package is optional and has no effect on deployment.

### Update files

Do this first, so the first deploy already uses your own images and directory.

  | File | Before | After |
  |---|---|---|
  | `compose.production.yml` | `image: ghcr.io/rxmi-bkd/saas-backend:${IMAGE_TAG:-latest}` | `image: ghcr.io/acme/acme-crm-backend:${IMAGE_TAG:-latest}` |
  | `compose.production.yml` | `image: ghcr.io/rxmi-bkd/saas-frontend:${IMAGE_TAG:-latest}` | `image: ghcr.io/acme/acme-crm-frontend:${IMAGE_TAG:-latest}` |
  | `build-and-deploy.yml` | `image: ghcr.io/rxmi-bkd/saas-backend` | `image: ghcr.io/acme/acme-crm-backend` |
  | `build-and-deploy.yml` | `image: ghcr.io/rxmi-bkd/saas-frontend` | `image: ghcr.io/acme/acme-crm-frontend` |
  | `build-and-deploy.yml` | `target: /home/github-actions/saas` | `target: /home/github-actions/acme-crm` |
  | `build-and-deploy.yml` | `cd /home/github-actions/saas` | `cd /home/github-actions/acme-crm` |

### Prepare the server

- Install Docker Engine and the Compose plugin.
- Create user and give it access to Docker.
  ```bash
  sudo useradd -m github-actions
  sudo usermod -aG docker github-actions
  ```
- Append public key to `/home/github-actions/.ssh/authorized_keys`.
- Create the project directory `mkdir -p /home/github-actions/acme-crm`.
- Copy `.env.production` into it, next to where `compose.production.yml` will be placed by the deploy.
- Create a DNS A record that maps the `DOMAIN` value to the server's public IP.
- Open ports 80 and 443 in the server's firewall.
- Make sure the server can pull `ghcr.io/acme/acme-crm-*`. The packages are private by default and only exist after the first build, so either run `docker login ghcr.io` as the deploy user, with a personal access token that has the `read:packages` scope, or make the packages public after the first build (GitHub > your profile or org > Packages > package settings > Change visibility).

### Configure secrets

- Create `.env.production` from `.env.production.example` and fill in every value
  - `DOMAIN`: your domain, e.g. `crm.acme.com`.
  - `FRONTEND_BASE_URL`: `https://` followed by the same domain. It must use HTTPS because session cookies are `secure` and `same-site=strict`, so the app does not work over plain HTTP.
  - `POSTGRES_DB` / `POSTGRES_USER` / `DB_URL`: keep them consistent because the database name in `DB_URL` must match `POSTGRES_DB`.
  - `JWT_RESET_PASSWORD_TOKEN_SECRET` and `JWT_ACTIVATION_TOKEN_SECRET`: Generate each one with `openssl rand -base64 48`.
- Generate an SSH key pair for deployments: `ssh-keygen -t ed25519 -f deploy_key -N ""`.
- Add the GitHub repository secrets
  - `SSH_HOST`
  - `SSH_USER`
  - `SSH_KEY`
- Delete the local `deploy_key` once it is stored as a secret. `.env.production` is not committed, keep it out of git.

### First deploy and verification

- Push to `master`, or run the `build & deploy` workflow manually.
- On the server, check that the three containers are healthy
  ```bash
  cd /home/github-actions/acme-crm
  docker compose -f compose.production.yml ps
  docker compose -f compose.production.yml logs --tail=100 backend
  ```
- Open `https://DOMAIN` and check that the page loads over HTTPS.
