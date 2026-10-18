# ==============================================================
# Script khởi động toàn bộ dịch vụ EduMy trên Localhost
# ==============================================================
$ErrorActionPreference = "Continue"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "             KHỞI ĐỘNG HỆ THỐNG EDUMY LOCALHOST           " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$baseDir = Join-Path $PSScriptRoot "Edumy"
if (-not (Test-Path $baseDir)) {
    $baseDir = $PSScriptRoot
}

$mlDir = Join-Path $baseDir "MLService"
$backendDir = Join-Path $baseDir "Backend"
$frontendDir = Join-Path $baseDir "Frontend"

# 1. Kiểm tra SQL Server
Write-Host "`n[1/4] Kiểm tra kết nối SQL Server (port 1433)..." -ForegroundColor Yellow
$sqlTest = Test-NetConnection -ComputerName 127.0.0.1 -Port 1433 -WarningAction SilentlyContinue
if ($sqlTest.TcpTestSucceeded) {
    Write-Host " -> SQL Server đang hoạt động tốt trên port 1433." -ForegroundColor Green
} else {
    Write-Host " -> CẢNH BÁO: Chưa kết nối được port 1433. Hãy đảm bảo SQL Server đã khởi chạy." -ForegroundColor Red
}

# 2. Khởi chạy ML Service (FastAPI)
Write-Host "`n[2/4] Khởi chạy ML Service (FastAPI - port 8000)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$mlDir'; python -m uvicorn main:app --host 127.0.0.1 --port 8000" -WindowStyle Minimized
Write-Host " -> ML Service đã được bật tại http://127.0.0.1:8000" -ForegroundColor Green

# 3. Khởi chạy Backend (Spring Boot)
Write-Host "`n[3/4] Khởi chạy Backend (Spring Boot 3 - port 5000)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$backendDir'; .\mvnw.cmd spring-boot:run" -WindowStyle Minimized
Write-Host " -> Backend đang khởi chạy tại http://localhost:5000" -ForegroundColor Green

# 4. Khởi chạy Frontend (React Vite)
Write-Host "`n[4/4] Khởi chạy Frontend (React Vite - port 5173)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$frontendDir'; npm run dev" -WindowStyle Minimized
Write-Host " -> Frontend đang khởi chạy tại http://localhost:5173" -ForegroundColor Green

Write-Host "`n==========================================================" -ForegroundColor Cyan
Write-Host " TOÀN BỘ CÁC DỊCH VỤ ĐÃ ĐƯỢC KÍCH HOẠT THÀNH CÔNG!       " -ForegroundColor Cyan
Write-Host " - Giao diện Web Client: http://localhost:5173           " -ForegroundColor Yellow
Write-Host " - API Backend         : http://localhost:5000           " -ForegroundColor Yellow
Write-Host " - ML Microservice     : http://127.0.0.1:8000           " -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Cyan
