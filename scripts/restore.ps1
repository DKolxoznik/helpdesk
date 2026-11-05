param(
    [Parameter(Mandatory = $true)][string]$Dump,
    [string]$Database = "helpdesk_restore"
)

$ErrorActionPreference = "Stop"
if ($Database -notmatch '^[A-Za-z][A-Za-z0-9_]*$') {
    throw "Имя базы может содержать только буквы, цифры и подчёркивание."
}
if (-not (Test-Path $Dump)) {
    throw "Файл копии не найден: $Dump"
}

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root
$user = if ($env:POSTGRES_USER) { $env:POSTGRES_USER } else { "helpdesk" }

docker compose cp $Dump "postgres:/tmp/helpdesk-restore.dump"
docker compose exec -T postgres psql -U $user -d postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '$Database' AND pid <> pg_backend_pid();"
docker compose exec -T postgres psql -U $user -d postgres -c "DROP DATABASE IF EXISTS $Database;"
docker compose exec -T postgres psql -U $user -d postgres -c "CREATE DATABASE $Database;"
docker compose exec -T postgres pg_restore -U $user -d $Database --no-owner --no-privileges /tmp/helpdesk-restore.dump
docker compose exec -T postgres psql -U $user -d $Database -c "SELECT 'categories' AS metric, count(*) FROM category UNION ALL SELECT 'tickets', count(*) FROM ticket;"
