# ==============================================================
# Script khởi chạy EduMy trên Docker Desktop
# ==============================================================
$ErrorActionPreference = "Continue"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "         KHOI CHAY EDUMY TREN DOCKER DESKTOP              " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Kiem tra Docker Daemon
docker version >$null 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "[CANH BAO] Docker Desktop chua duoc bat! Vui long mo Docker Desktop truoc." -ForegroundColor Red
    exit 1
}

Write-Host "-> Docker Desktop dang hoat dong tot." -ForegroundColor Green

# 3. Chay docker compose (tu thu muc chua docker-compose.yml)
$composeDir = Join-Path $PSScriptRoot "..\Edumy"
Write-Host "`nDang khoi dong cac container (database, mlservice, backend, frontend, prometheus, grafana)..." -ForegroundColor Cyan
Push-Location $composeDir
docker compose up -d
Pop-Location

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n==========================================================" -ForegroundColor Green
    Write-Host " DA KHOI CHAY THANH CONG CAC DICH VU TREN DOCKER DESKTOP! " -ForegroundColor Green
    Write-Host "==========================================================" -ForegroundColor Green
    Write-Host " - Frontend UI : http://localhost:3000" -ForegroundColor Yellow
    Write-Host " - Backend API : http://localhost:5000" -ForegroundColor Yellow
    Write-Host " - AI Service  : http://localhost:8000/docs" -ForegroundColor Yellow
    Write-Host " - Database    : SQL Server container (localhost:14333)" -ForegroundColor Yellow
    Write-Host " - Prometheus  : http://localhost:9090" -ForegroundColor Yellow
    Write-Host " - Grafana     : http://localhost:3001 (admin/admin)" -ForegroundColor Yellow
    Write-Host "`nBan co the mo Docker Desktop GUI hoac trinh duyet http://localhost:3000 de xem." -ForegroundColor Cyan
} else {
    Write-Host "`n[LOI] Khong the khoi chay Docker Compose. Vui long kiem tra log ben tren." -ForegroundColor Red
}
