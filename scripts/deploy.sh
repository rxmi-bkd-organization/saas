#!/usr/bin/env bash
# Usage: ./scripts/deploy.sh [image-tag]   (default: latest)
# Rollback example: ./scripts/deploy.sh sha-xxxxxxx

set -euo pipefail

cd "$(dirname "$0")/.."

export IMAGE_TAG="${1:-latest}"

docker compose -f compose.production.yml pull
docker compose -f compose.production.yml up -d --wait --wait-timeout 300 --remove-orphans
docker image prune -f