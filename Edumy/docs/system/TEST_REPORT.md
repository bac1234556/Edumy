# Test Inventory & Verification Guide — EduMy

**Updated**: 2026-11-17

---

## 1. Backend Unit & Architecture Tests (`Backend/src/test/java/com/edumy/`)

Hệ thống Backend sử dụng **JUnit 5**, **Mockito**, **Spring Modulith Test** và **JaCoCo** để kiểm chứng kiến trúc và nghiệp vụ cốt lõi:

| Test Class | File Path | Các ca kiểm thử (`@Test`) | Mục tiêu |
|---|---|---|---|
| `ModularityTest` | `Backend/src/test/java/com/edumy/ModularityTest.java` | `verifiesModularStructure` | Khởi tạo `ApplicationModules.of(Application.class)` kiểm tra cấu trúc 8 module Spring Modulith |
| `AuthServiceImplTest` | `Backend/src/test/java/com/edumy/identity/auth/service/AuthServiceImplTest.java` | `testMobileRegister_Success`, `testMobileLogin_AccountLocked`, `testMobileRefresh_ReuseDetection`, `testMobileLogout_Success` | Kiểm thử đăng ký mobile, chặn tài khoản bị khóa, phát hiện dùng lại refresh token & đăng xuất |
| `MobileAuthControllerTest` | `Backend/src/test/java/com/edumy/identity/auth/controller/MobileAuthControllerTest.java` | `testMobileRegister`, `testMobileLogin`, `testMobileRefresh`, `testMobileLogout` | Kiểm thử REST contract cho `/api/v1/auth/mobile/*` |
| `EarningServiceImplTest` | `Backend/src/test/java/com/edumy/payout/service/EarningServiceImplTest.java` | `testAcM09RevenueSplit`, `testGetSummary` | Kiểm thử chia tách doanh thu (phí nền tảng + đồng giảng viên + giảng viên chính) và tổng hợp số dư |
| `WithdrawalServiceImplTest` | `Backend/src/test/java/com/edumy/payout/service/WithdrawalServiceImplTest.java` | `testMissingPayoutAccount`, `testBelowMinimum`, `testAlreadyOpen`, `testExceedsBalance`, `testSuccess` | Kiểm thử các ràng buộc nghiệp vụ khi tạo yêu cầu rút tiền |

### Lệnh chạy kiểm thử Backend
```bash
cd Edumy/Edumy/Backend
.\mvnw.cmd test
```

---

## 2. Frontend Checks (`Frontend/`)

```bash
cd Edumy/Edumy/Frontend
npm run lint    # Kiểm tra mã nguồn với Oxlint
npm run build   # Kiểm tra đóng gói production bundle với Vite
```

---

## 3. Integration Verification Script (`scripts/verify_audit.ps1`)

Script kiểm thử tích hợp end-to-end khi Backend và SQL Server đang chạy tại `http://localhost:5000`:
1. Kiểm tra `/actuator/health`
2. Đăng nhập đa vai trò (`Admin`, `Instructor`, `Student`) & Mobile Auth (`/api/v1/auth/mobile/login`)
3. Kiểm tra Catalog, `recommend`, `similar` và ghi log tìm kiếm
4. Kiểm tra Q&A (`/api/v1/courses/{id}/questions`) & Payout (`/api/v1/me/earnings/summary`)
5. Kiểm tra định tuyến payment callback đa kiểu chữ (`/api/payment/callback`, `/api/Payment/callback`)
6. Kiểm tra tạo Section & Quiz cho giảng viên
7. Kiểm tra Audit Trail (`/api/admin/activities`)
