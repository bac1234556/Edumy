# ==============================================================
# Script khoi dong toan bo dich vu EduMy tren Localhost
# ==============================================================
$ErrorActionPreference = "Continue"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "             KHOI DONG HE THONG EDUMY LOCALHOST           " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$candidates = @(
    (Join-Path $PSScriptRoot "Edumy\Edumy"),
    (Join-Path $PSScriptRoot "Edumy"),
    $PSScriptRoot
)

$baseDir = $null
foreach ($c in $candidates) {
    if ((Test-Path (Join-Path $c "Backend")) -and (Test-Path (Join-Path $c "Frontend"))) {
        $baseDir = $c
        break
    }
}

if (-not $baseDir) {
    Write-Host "Khong tim thay thu muc chua Backend va Frontend!" -ForegroundColor Red
    exit 1
}

$mlDir = Join-Path $baseDir "MLService"
$backendDir = Join-Path $baseDir "Backend"
$frontendDir = Join-Path $baseDir "Frontend"

# 1. Kiem tra SQL Server
Write-Host "`n[1/4] Kiem tra ket noi SQL Server (port 1433)..." -ForegroundColor Yellow
$sqlTest = Test-NetConnection -ComputerName 127.0.0.1 -Port 1433 -WarningAction SilentlyContinue
if ($sqlTest.TcpTestSucceeded) {
    Write-Host " -> SQL Server dang hoat dong tot tren port 1433." -ForegroundColor Green
} else {
    Write-Host " -> CANH BAO: Chua ket noi duoc port 1433. Hay dam bao SQL Server da duoc bat." -ForegroundColor Red
}

# 2. Khoi chay ML Service (FastAPI)
Write-Host "`n[2/4] Khoi chay ML Service (FastAPI - port 8000)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$mlDir'; python -m uvicorn main:app --host 127.0.0.1 --port 8000" -WindowStyle Normal
Write-Host " -> ML Service dang bat tai http://127.0.0.1:8000" -ForegroundColor Green

# 3. Khoi chay Backend (Spring Boot)
Write-Host "`n[3/4] Khoi chay Backend (Spring Boot 3 - port 5000)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$backendDir'; .\mvnw.cmd spring-boot:run" -WindowStyle Normal
Write-Host " -> Backend dang khoi chay tai http://localhost:5000" -ForegroundColor Green

# 4. Khoi chay Frontend (React Vite)
Write-Host "`n[4/4] Khoi chay Frontend (React Vite - port 5173)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$frontendDir'; npm run dev" -WindowStyle Normal
Write-Host " -> Frontend dang khoi chay tai http://localhost:5173" -ForegroundColor Green

Write-Host "`n==========================================================" -ForegroundColor Cyan
Write-Host " TOAN BO CAC DICH VU DA DUOC KICH HOAT THANH CONG!        " -ForegroundColor Cyan
Write-Host " - Giao dien Web Client: http://localhost:5173            " -ForegroundColor Yellow
Write-Host " - API Backend         : http://localhost:5000            " -ForegroundColor Yellow
Write-Host " - ML Microservice     : http://127.0.0.1:8000            " -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Cyan
