$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

$user = if ($env:POSTGRES_USER) { $env:POSTGRES_USER } else { "helpdesk" }
$db = if ($env:POSTGRES_DB) { $env:POSTGRES_DB } else { "helpdesk" }
$outDir = Join-Path $root "backups"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
$stamp = Get-Date -Format "yyyyMMdd-HHmmss"
$out = Join-Path $outDir "helpdesk-$stamp.dump"

docker compose exec -T postgres pg_dump -U $user -d $db -Fc -f /tmp/helpdesk.dump
docker compose cp "postgres:/tmp/helpdesk.dump" $out
Write-Host "Backup: $out"
