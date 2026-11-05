#!/usr/bin/env bash
set -euo pipefail
dump="${1:?Укажите файл копии}"
database="${2:-helpdesk_restore}"
if [[ ! "$database" =~ ^[A-Za-z][A-Za-z0-9_]*$ ]]; then
  echo "Имя базы может содержать только буквы, цифры и подчёркивание." >&2
  exit 1
fi
cd "$(dirname "$0")/.."
user="${POSTGRES_USER:-helpdesk}"
docker compose cp "$dump" postgres:/tmp/helpdesk-restore.dump
docker compose exec -T postgres psql -U "$user" -d postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '$database' AND pid <> pg_backend_pid();"
docker compose exec -T postgres psql -U "$user" -d postgres -c "DROP DATABASE IF EXISTS \"$database\";"
docker compose exec -T postgres psql -U "$user" -d postgres -c "CREATE DATABASE \"$database\";"
docker compose exec -T postgres pg_restore -U "$user" -d "$database" --no-owner --no-privileges /tmp/helpdesk-restore.dump
docker compose exec -T postgres psql -U "$user" -d "$database" -c "SELECT 'categories' AS metric, count(*) FROM category UNION ALL SELECT 'tickets', count(*) FROM ticket;"
