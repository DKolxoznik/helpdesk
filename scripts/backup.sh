#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
user="${POSTGRES_USER:-helpdesk}"
db="${POSTGRES_DB:-helpdesk}"
mkdir -p backups
out="backups/helpdesk-$(date +%Y%m%d-%H%M%S).dump"
docker compose exec -T postgres pg_dump -U "$user" -d "$db" -Fc -f /tmp/helpdesk.dump
docker compose cp postgres:/tmp/helpdesk.dump "$out"
echo "Backup: $out"
