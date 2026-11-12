# ==============================================================
# Script dung toan bo dich vu EduMy tren Docker Desktop
# ==============================================================
Write-Host "Dang dung va go bo cac container EduMy..." -ForegroundColor Cyan

$composeDir = Join-Path $PSScriptRoot "..\Edumy"
Push-Location $composeDir
docker compose down
Pop-Location

Write-Host "Da dung toan bo he thong Edumy tren Docker thanh cong!" -ForegroundColor Green
