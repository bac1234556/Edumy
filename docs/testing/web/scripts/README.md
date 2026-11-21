# Hướng Dẫn Chạy Bộ Script Kiểm Thử Tự Động Edumy Web (`docs/testing/web/scripts/`)

Bộ script kiểm thử sử dụng **Node.js thuần (`fetch`, Node v24)** và **Python 3** (không cài thêm dependency vào mã nguồn ứng dụng) nhằm đảm bảo mọi kết quả Pass/Fail đều từ lần thực thi thật và lưu đầy đủ bằng chứng tại `../evidence/`.

## 1. Dựng môi trường kiểm thử độc lập (`edumy-test`)
Không đụng tới dữ liệu hay container dev (`edumy_*`):
```powershell
docker compose -p edumy-test -f Edumy-main/docs/testing/web/scripts/docker-compose.test.yml up -d --build
```
Ánh xạ cổng kiểm thử:
- Frontend: `http://localhost:3002`
- Backend: `http://localhost:5001`
- Mailpit UI/API: `http://localhost:8026` (SMTP `1026`)
- SQL Server: `localhost:14334`
- Prometheus: `http://localhost:9091`
- Grafana: `http://localhost:3003`

## 2. Khởi tạo dữ liệu seed, baseline `.bak` & Smoke Test
```powershell
# Áp dụng migration payout
docker cp Edumy-main/Edumy/Database/scripts/08_Fix_Payout_And_Sync.sql edumy-test-database-1:/tmp/08.sql
docker exec edumy-test-database-1 /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "YourPassword123" -d EduMyDb -C -i /tmp/08.sql

# Seed 10 tài khoản + dữ liệu khóa 5 trạng thái + coupon
node Edumy-main/docs/testing/web/scripts/seed_and_baseline.mjs

# Tạo baseline edumy_test_baseline.bak
docker exec edumy-test-database-1 /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "YourPassword123" -C -Q "BACKUP DATABASE EduMyDb TO DISK = N'/var/opt/mssql/edumy_test_baseline.bak' WITH FORMAT, INIT;"

# Chạy Smoke Test 10 TC xuyên 8 module
node Edumy-main/docs/testing/web/scripts/smoke_test.mjs
```

## 3. Chạy các kịch bản kiểm thử theo Module (API, IDOR, Hiệu năng, Phục hồi)
```powershell
node Edumy-main/docs/testing/web/scripts/run_m01.mjs
node Edumy-main/docs/testing/web/scripts/run_m02.mjs
node Edumy-main/docs/testing/web/scripts/run_m03.mjs
node Edumy-main/docs/testing/web/scripts/run_m04.mjs
node Edumy-main/docs/testing/web/scripts/run_m05.mjs
node Edumy-main/docs/testing/web/scripts/run_m06.mjs
node Edumy-main/docs/testing/web/scripts/run_m07_reg.mjs
```
> Mật khẩu test và webhook key đọc qua biến môi trường (`QA_TEST_PASSWORD`, `QA_SEPAY_KEY`), mọi giá trị nhạy cảm đều được che `***` trước khi ghi vào `evidence/` và `EDU-TEL-W-001_Execution_Log.csv`.

## 4. Sinh số liệu thống kê, cập nhật Excel & vẽ 4 biểu đồ PNG
```powershell
python Edumy-main/docs/testing/web/scripts/compute_metrics_and_charts.py
```

## 5. Dọn dẹp môi trường kiểm thử sau khi kết thúc
```powershell
docker compose -p edumy-test -f Edumy-main/docs/testing/web/scripts/docker-compose.test.yml down -v
```
