#!/usr/bin/env bash
set -euo pipefail
base="${BASE_URL:-http://localhost:8080}"

echo "Проверка readiness"
curl -fsS "$base/actuator/health/readiness"
echo

echo "Справочник категорий"
curl -fsS "$base/api/categories"
echo

created="$(curl -fsS -X POST "$base/api/tickets" \
  -H "Content-Type: application/json" \
  -d '{"title":"Проверка стенда","description":"smoke","categoryId":"11111111-1111-1111-1111-111111111111","priority":"HIGH","dueAt":"2020-01-01T00:00:00Z"}')"
echo "$created"
id="$(echo "$created" | jq -r .id)"
test "$id" != "null"

curl -fsS -X POST "$base/api/tickets/$id/status" \
  -H "Content-Type: application/json" \
  -d '{"status":"IN_PROGRESS"}' >/dev/null

code="$(curl -s -o /tmp/helpdesk-forbidden.json -w "%{http_code}" -X POST "$base/api/tickets/$id/status" \
  -H "Content-Type: application/json" \
  -d '{"status":"NEW"}')"
test "$code" = "409"

echo "Сводка"
curl -fsS "$base/api/reports/summary?from=2020-01-01T00:00:00Z&to=2035-01-01T00:00:00Z"
echo
echo "Стенд отвечает."
