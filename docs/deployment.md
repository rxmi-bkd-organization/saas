# Deployment setup for a new project

This file lists the steps to deploy a new project forked from this boilerplate. It covers the secrets to create, the server to prepare, and the workflow and compose files to update so that a push to `master` builds your images and copies the compose files to the server. You then start the containers manually.
Several projects can share one server: a single shared Caddy proxy (`proxy/`) owns ports 80 and 443 and handles HTTPS for every domain, while each project's stack publishes no port.
Every step uses the same example: owner `acme` and project `acme-crm`. Replace them with your own GitHub owner and project name.

Renaming `spring.application.name` and the `org.bkd.saas` Java package is optional and has no effect on deployment.

### Update files

Do this first, so the first deploy already uses your own images and directory.

  | File | Before | After |
  |---|---|---|
  | `compose.production.yml` | `image: ghcr.io/rxmi-bkd-organization/saas-backend:${IMAGE_TAG:-latest}` | `image: ghcr.io/acme/acme-crm-backend:${IMAGE_TAG:-latest}` |
  | `compose.production.yml` | `image: ghcr.io/rxmi-bkd-organization/saas-frontend:${IMAGE_TAG:-latest}` | `image: ghcr.io/acme/acme-crm-frontend:${IMAGE_TAG:-latest}` |
  | `build-and-deploy.yml` | `image: ghcr.io/rxmi-bkd-organization/saas-backend` | `image: ghcr.io/acme/acme-crm-backend` |
  | `build-and-deploy.yml` | `image: ghcr.io/rxmi-bkd-organization/saas-frontend` | `image: ghcr.io/acme/acme-crm-frontend` |
  | `build-and-deploy.yml` | `target: /home/github-actions/saas` | `target: /home/github-actions/acme-crm` |

### Prepare the server

Shared by all projects on the server, do it once.

- Install Docker Engine and the Compose plugin.
- Create user and give it access to Docker.
  ```bash
  sudo useradd -m github-actions
  sudo usermod -aG docker github-actions
  ```
- Append public key to `/home/github-actions/.ssh/authorized_keys`.
- Open ports 80 and 443 in the server's firewall.
- Start the shared proxy, once per server (see [Shared reverse proxy](#shared-reverse-proxy)).

Per project:

- Create the project directory `mkdir -p /home/github-actions/acme-crm`. Use a different directory name for each project, because Compose derives the project name, and so the volumes, from it.
- Copy `.env.production` into it, next to where `compose.production.yml` will be placed by the deploy.
- Create a DNS A record that maps the `DOMAIN` value to the server's public IP.
- Add the project to the proxy (see [Shared reverse proxy](#shared-reverse-proxy)).
- Make sure the server can pull `ghcr.io/acme/acme-crm-*`. The packages are private by default and only exist after the first build, so either run `docker login ghcr.io` as the deploy user, with a personal access token that has the `read:packages` scope, or make the packages public after the first build (GitHub > your profile or org > Packages > package settings > Change visibility).

### Shared reverse proxy

`compose.production.yml` publishes no port. The frontend joins the external Docker network `proxy` under the alias `APP_NAME`, and the shared proxy forwards each domain to that alias over plain HTTP. The proxy is the only container bound to ports 80 and 443, so any number of projects can run on the same server.

- Copy the `proxy/` directory to the server (for example `/home/github-actions/proxy`) and start it once. It creates the `proxy` network.
  ```bash
  cd /home/github-actions/proxy
  docker compose up -d
  ```
- For each project, create `sites/<project>.caddy` in that directory, using `sites/example.caddy.example` as a model: the project's `DOMAIN` as site address and `reverse_proxy <APP_NAME>:80`. Then reload the proxy, which obtains the certificate automatically.
  ```bash
  docker compose exec caddy caddy reload --config /etc/caddy/Caddyfile
  ```
- `APP_NAME` must be unique per project on the server, otherwise two frontends answer to the same name.
- The frontend Caddy trusts the proxy's `X-Forwarded-For`, so the backend rate limiter still sees the real client IP.

### Configure secrets

- Create `.env.production` from `.env.production.example` and fill in every value
  - `APP_NAME`: unique name of the project on the server, e.g. `acme-crm`. It is the hostname the shared proxy uses to reach the frontend.
  - `DOMAIN`: your domain, e.g. `crm.acme.com`. It must match the site address in the proxy's `sites/<project>.caddy`.
  - `FRONTEND_BASE_URL`: `https://` followed by the same domain. It must use HTTPS because session cookies are `secure` and `same-site=strict`, so the app does not work over plain HTTP.
  - `POSTGRES_DB` / `POSTGRES_USER` / `DB_URL`: keep them consistent because the database name in `DB_URL` must match `POSTGRES_DB`.
  - `JWT_RESET_PASSWORD_TOKEN_SECRET` and `JWT_ACTIVATION_TOKEN_SECRET`: Generate each one with `openssl rand -base64 48`.
  - `BREVO_API_KEY`: a Brevo API key (Brevo > SMTP & API > API Keys). Emails are sent from `no-reply@DOMAIN`, so that address (or the `DOMAIN` domain, with SPF/DKIM set up) must be verified as a sender in Brevo, otherwise Brevo rejects or spam-folders the emails.
  - `JWT_RESET_PASSWORD_TOKEN_SECRET`, `JWT_ACTIVATION_TOKEN_SECRET` and `JWT_ACCESS_TOKEN_SECRET` (mobile access tokens): Generate each one with `openssl rand -base64 48`.
  - `JWT_ACCESS_TOKEN_EXPIRATION`: lifetime in seconds of mobile access tokens (default `900`). There is no refresh token and no server-side revocation, so a token stays valid until it expires, even after a password change. Keep it short.
  - `ADMIN_EMAIL` / `ADMIN_PASSWORD`: the admin user, created at every startup if it does not exist, and enabled if it does (password 8 to 64 characters). An existing admin keeps its current password, so changing `ADMIN_PASSWORD` later has no effect. Both are required: if either is missing or invalid, the backend still starts but logs `Admin user seeding failed` and creates no admin.
- Generate an SSH key pair for deployments: `ssh-keygen -t ed25519 -f deploy_key -N ""`.
- Add the GitHub repository secrets
  - `SSH_HOST`
  - `SSH_USER`
  - `SSH_KEY`
- Delete the local `deploy_key` once it is stored as a secret. `.env.production` is not committed, keep it out of git.

### First deploy and verification

- Push to `master`, or run the `build and deploy` workflow manually. It builds the images and copies the compose files and `scripts/deploy.sh`, but does not start anything.
- On the server, make sure the shared proxy is running, then start the containers and check that the three containers are healthy
  ```bash
  cd /home/github-actions/acme-crm
  ./scripts/deploy.sh
  docker compose --env-file .env.production -f compose.production.yml ps
  docker compose --env-file .env.production -f compose.production.yml logs --tail=100 backend
  ```
- Open `https://DOMAIN` and check that the page loads over HTTPS.
