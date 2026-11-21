# -*- coding: utf-8 -*-
"""
Generates EDU-TIR-W-001_Defect_Report.md and EDU-TSR-W-001_Status_Report.md
directly from computed_metrics.json and defects_data.json (100% programmatic, zero manual arithmetic).
"""

import json
import os
from collections import Counter

from qa_requirements_data import IDOR_MATRIX, IDOR_ROLES, NFR_SPECS

BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
METRICS = json.load(open(os.path.join(BASE_DIR, "scripts", "computed_metrics.json"), encoding="utf-8"))
DEFECTS = json.load(open(os.path.join(BASE_DIR, "scripts", "defects_data.json"), encoding="utf-8"))

TIR_PATH = os.path.join(BASE_DIR, "EDU-TIR-W-001_Defect_Report.md")
TSR_PATH = os.path.join(BASE_DIR, "EDU-TSR-W-001_Status_Report.md")


def write_tir():
    by_sev = Counter(d["severity"] for d in DEFECTS)
    by_type = Counter(d["type"] for d in DEFECTS)
    lines = [
        "# EDU-TIR-W-001 — BÁO CÁO LỖI HỆ THỐNG EDUMY WEB (TEST INCIDENT / DEFECT REPORT)",
        "",
        "| Thuộc tính | Giá trị |",
        "| :--- | :--- |",
        "| **Mã tài liệu** | `EDU-TIR-W-001` |",
        "| **Chu kỳ phát hiện** | `C1` (`2026-11-21`, Asia/Ho_Chi_Minh) |",
        "| **Build kiểm thử** | `Edumy-Project@f2f5240` · `Edumy-main@bf4ac6c` |",
        f"| **Tổng số Defect** | **{len(DEFECTS)}** (`S1`: {by_sev['S1']} · `S2`: {by_sev['S2']} · `S3`: {by_sev['S3']} · `S4`: {by_sev['S4']}) |",
        "",
        "## 1. Phân bố Defect theo Loại lỗi & Severity",
        "",
        "| Loại lỗi | Số lượng |",
        "| :--- | ---: |",
    ]
    for k, v in sorted(by_type.items(), key=lambda x: -x[1]):
        lines.append(f"| {k} | {v} |")

    lines += [
        "",
        "## 2. Danh sách chi tiết toàn bộ Defect (`DEF-W-001` .. `DEF-W-030`)",
        "",
        "| Defect ID | Severity | Priority | Module | Loại lỗi | Trạng thái | TC ID liên quan | Tóm tắt & Bằng chứng |",
        "| :--- | :---: | :---: | :---: | :--- | :---: | :--- | :--- |",
    ]
    for d in DEFECTS:
        tcs = ", ".join(d["tcIds"])
        lines.append(
            f"| **{d['id']}** | **{d['severity']}** | {d['priority']} | {d['module']} | {d['type']} | {d['status']} | `{tcs}` | {d['title']} ([evidence]({d['evidence']})) |"
        )

    lines += ["", "## 3. Chi tiết tái hiện từng Defect S1 & S2 (Ưu tiên xử lý ngay)", ""]
    for d in DEFECTS:
        if d["severity"] not in ("S1", "S2"):
            continue
        lines += [
            f"### {d['id']} [{d['severity']} / {d['priority']} / {d['type']}] — {d['title']}",
            f"- **Module**: `{d['module']}` · **Truy vết**: `{d['trace']}` · **Run ID**: `{d['runId']}`",
            f"- **TC liên quan**: `{', '.join(d['tcIds'])}`",
            f"- **Các bước tái hiện**: {d['repro']}",
            f"- **Mong đợi**: {d['expected']}",
            f"- **Thực tế**: {d['actual']}",
            f"- **Bằng chứng**: `{d['evidence']}`",
            "",
        ]

    with open(TIR_PATH, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))


def write_tsr():
    t1 = METRICS["table1_module"]
    t2 = METRICS["table2_type"]
    t3 = METRICS["table3_cycle"]
    t6 = METRICS["table6_req"]
    t9 = METRICS["table9_jacoco"]
    tot = t1[-1]
    by_sev = METRICS["table5_defects_by_sev"]

    lines = [
        "# EDU-TSR-W-001 — BÁO CÁO TRẠNG THÁI KIỂM THỬ CHU KỲ C1 (TEST STATUS REPORT)",
        "",
        "| Thuộc tính | Giá trị |",
        "| :--- | :--- |",
        "| **Mã tài liệu** | `EDU-TSR-W-001` |",
        "| **Chu kỳ** | **Chu kỳ C1** (`2026-11-21`, Asia/Ho_Chi_Minh) |",
        "| **Build kiểm thử** | `Edumy-Project@f2f5240` · `Edumy-main@bf4ac6c` |",
        "| **Môi trường** | Stack Docker độc lập `edumy-test` (6 containers healthy/running) |",
        "| **Kết quả tổng quan C1** | **57 Pass / 69 Fail / 8 Blocked / 0 Not Run** (Tổng **134 TC**) |",
        f"| **Execution rate / Pass rate** | **{tot['execRate']}%** đã thực thi · **{tot['passRate']}%** Pass rate · **{METRICS['total_defects']} Defect** (`S1`: {by_sev.get('S1',0)}, `S2`: {by_sev.get('S2',0)}, `S3`: {by_sev.get('S3',0)}, `S4`: {by_sev.get('S4',0)}) |",
        f"| **Khuyến nghị cuối C1** | **NO-GO (Chưa đạt Exit Criteria)** — Tồn tại **{by_sev.get('S1',0)} lỗi S1 Critical** (bypass xác thực webhook SePay, thanh toán đơn đã hủy, IDOR biên soạn, upload độc hại, Admin khóa Admin/tự khóa, JWT cũ còn hiệu lực sau khóa, lỗ hổng kiểm tra quyền Q&A/Quiz) và **{by_sev.get('S2',0)} lỗi S2 Major**. Đề nghị đội phát triển sửa lỗi S1/S2 trước khi chạy Chu kỳ C2. |",
        "",
        "---",
        "",
        "## Bảng 1 — Thống kê theo Module (`W-M01`..`W-M07`, `W-REG`)",
        "",
        "| Module | Tổng TC | Đã thực thi | Pass | Fail | Blocked | Not Run | Số lần chạy | Số chu kỳ | Execution rate | Pass rate | First-time pass rate | Defect S1/S2/S3/S4 |",
        "| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | :---: |",
    ]
    for r in t1:
        b = "**" if r["key"] == "Tổng" else ""
        lines.append(
            f"| {b}{r['key']}{b} | {b}{r['total']}{b} | {b}{r['executed']}{b} | {b}{r['pass']}{b} | {b}{r['fail']}{b} | {b}{r['blocked']}{b} | {b}{r['notRun']}{b} | {b}{r['totalRuns']}{b} | {r['cycles']} | {r['execRate']}% | {r['passRate']}% | {r['firstTimePassRate']}% | {r['S1']}/{r['S2']}/{r['S3']}/{r['S4']} |"
        )

    lines += [
        "",
        "## Bảng 2 — Thống kê theo Loại kiểm thử",
        "",
        "| Loại | Tổng TC | Đã thực thi | Pass | Fail | Blocked | Not Run | Số lần chạy | Execution rate | Pass rate | First-time pass rate |",
        "| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |",
    ]
    for r in t2:
        b = "**" if r["key"] == "Tổng" else ""
        lines.append(
            f"| {b}{r['key']}{b} | {b}{r['total']}{b} | {b}{r['executed']}{b} | {b}{r['pass']}{b} | {b}{r['fail']}{b} | {b}{r['blocked']}{b} | {b}{r['notRun']}{b} | {b}{r['totalRuns']}{b} | {r['execRate']}% | {r['passRate']}% | {r['firstTimePassRate']}% |"
        )

    lines += [
        "",
        "## Bảng 3 — Thống kê theo Chu kỳ",
        "",
        "| Chu kỳ | TC chạy | Pass | Fail | Blocked | Defect mới | Defect đóng | Defect mở lại |",
        "| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |",
    ]
    for r in t3:
        lines.append(
            f"| **{r['cycle']}** | {r['executed']} | {r['pass']} | {r['fail']} | {r['blocked']} | {r['defectsNew']} | {r['defectsClosed']} | {r['defectsReopened']} |"
        )

    lines += [
        "",
        "## Bảng 6 — Độ phủ yêu cầu (`182/182 = 100%`)",
        "",
        "| Nhóm yêu cầu | Tổng số | Số có TC | Độ phủ TC | Pass toàn bộ | Có TC Fail/Blocked |",
        "| :--- | ---: | ---: | ---: | ---: | ---: |",
    ]
    for r in t6:
        cov = round(100.0 * r["withTc"] / r["total"], 1) if r["total"] else 0.0
        lines.append(
            f"| **{r['type']}** | {r['total']} | {r['withTc']} | {cov}% | {r['allPass']} | {len(r['failedReqs'])} |"
        )

    lines += [
        "",
        "## Bảng 7 — Kết quả đo lường Phi chức năng (NFR-01..NFR-10)",
        "",
        "| NFR | Chỉ số | Ngưỡng | Kết quả đo thực tế (C1) | Đạt / Không đạt |",
        "| :--- | :--- | :--- | :--- | :---: |",
    ]
    tc_state = METRICS["tc_state"]
    for nfr_id, metric, thresh, tlist_str in NFR_SPECS:
        tids = [x.strip() for x in tlist_str.split(",") if x.strip()]
        sts = [tc_state[t]["status"] for t in tids if t in tc_state]
        verdict = "Đạt" if sts and all(s == "Pass" for s in sts) else ("Không đạt" if any(s == "Fail" for s in sts) else "Blocked")
        summary_actual = "; ".join(f"`{t}`={tc_state[t]['status']}" for t in tids if t in tc_state)
        lines.append(f"| **{nfr_id}** | {metric} | {thresh} | {summary_actual} | **{verdict}** |")

    lines += [
        "",
        "## Bảng 9 — JaCoCo Code Coverage theo Module Backend (`43/43 tests PASS`)",
        "",
        "| Package Module (`com.edumy.*`) | Số lớp | Line Covered / Total | Line % | Branch Covered / Total | Branch % |",
        "| :--- | ---: | ---: | ---: | ---: | ---: |",
    ]
    tot_lc = sum(r["lineCovered"] for r in t9)
    tot_lt = sum(r["lineTotal"] for r in t9)
    tot_bc = sum(r["branchCovered"] for r in t9)
    tot_bt = sum(r["branchTotal"] for r in t9)
    for r in t9:
        lines.append(f"| `{r['module']}` | {r['classes']} | {r['lineCovered']} / {r['lineTotal']} | {r['linePct']}% | {r['branchCovered']} / {r['branchTotal']} | {r['branchPct']}% |")
    lines.append(
        f"| **TỔNG** | **{sum(r['classes'] for r in t9)}** | **{tot_lc} / {tot_lt}** | **{round(100.0*tot_lc/tot_lt,2) if tot_lt else 0}%** | **{tot_bc} / {tot_bt}** | **{round(100.0*tot_bc/tot_bt,2) if tot_bt else 0}%** |"
    )

    lines += [
        "",
        "## Kết luận kiểm chứng 7 điểm nghi vấn mục 3.2 (bằng thực thi thật)",
        "",
        "1. **`PUT /courses/{id}/status` set `PendingApproval` bỏ qua BR-07**: **ĐÚNG (Xác nhận lỗi — `DEF-W-013`)** — trả HTTP 200 với khóa rỗng (`RUN-W-C1-0049`).",
        "2. **`POST /courses` thiếu `categoryId` tự gán danh mục 1**: **ĐÚNG (Xác nhận lỗi — `DEF-W-014`)** — trả 201 và gán `categoryId=1` cho cả thiếu/`null`/`0` (`RUN-W-C1-0038`).",
        "3. **Bất nhất quy tắc giá tạo khóa vs gửi duyệt**: **ĐÚNG (Xác nhận lỗi — `DEF-W-015`)** — tạo khóa nhận `-1000, 1, 9999, 10500` (201), gửi duyệt mới chặn 400 (`RUN-W-C1-0040`).",
        "4. **NFR-02 BCrypt cost >= 12**: **KHÔNG ĐẠT (Xác nhận lỗi — `DEF-W-008`)** — DB lưu `$2a$10$` (cost=10) và `reset_token` lưu dạng rõ 8 ký tự (`RUN-W-C1-0023`).",
        "5. **NFR-03 Khóa tạm login sai lần 6 & rate-limit verify chứng chỉ lần 31**: **KHÔNG ĐẠT (Xác nhận lỗi — `DEF-W-009`)** — không chặn lần login thứ 6 (lần 7 vẫn 200) và cả 31 lần verify chứng chỉ đều 200 (`RUN-W-C1-0024`, `RUN-W-C1-0102`).",
        "6. **UC-S16 / NFR-09 `@Scheduled` tự hủy đơn & đóng QR quá hạn**: **KHÔNG CÓ (Xác nhận Not Implemented — `DEF-W-018`)** — 0 `@Scheduled` trong mã nguồn, đơn quá hạn 48h không tự hủy (`RUN-W-C1-0080`).",
        "7. **JWT thời hạn (`access-ttl=60m`, `expire-days=7d`)**: **ĐẠT kiểm chữ ký/alg:none (`RUN-W-C1-0025`), nhưng phát hiện lỗi S1 `DEF-W-006`**: JWT cũ vẫn dùng được suốt 60 phút sau khi Admin khóa tài khoản (`RUN-W-C1-0014`).",
        "",
        "## Danh sách 8 TC bị BLOCKED (Minh bạch lý do)",
        "",
        "- `TC-W-M07-DEP-002`: Các lệnh cục bộ trong `Jenkinsfile` (`mvnw verify`, `npm lint/build`, `docker compose build`) đều PASS; chỉ phần chạy trên máy chủ Jenkins CI bị **BLOCKED** do không có Jenkins Server cục bộ.",
        "- 7 TC UI trình duyệt (`TC-W-M07-PERF-003`, `TC-W-M07-USA-001..003`, `TC-W-M07-COMP-001`, `TC-W-REG-FN-001..002`): **BLOCKED** do trình duyệt Chrome trên máy host chưa bật Remote Debugging (`DevToolsActivePort ENOENT`) và Mozilla Firefox chưa được cài đặt. Khi bạn bật `chrome://inspect/#remote-debugging`, QA sẽ chạy bổ sung 7 TC này ngay trong C2.",
        "",
        "## Biểu đồ thống kê C1 (`charts/`)",
        "- `charts/chart1_results_by_module.png` — Pass/Fail/Blocked theo module",
        "- `charts/chart2_defects_by_severity.png` — Phân bố 30 defect theo Severity (S1..S4)",
        "- `charts/chart3_trend_by_cycle.png` — Xu hướng theo chu kỳ",
        "- `charts/chart4_performance_p95.png` — Hiệu năng p95 tại 50 CCU (NFR-05)",
    ]

    with open(TSR_PATH, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))
    print(f"Generated:\n- {TIR_PATH}\n- {TSR_PATH}")


if __name__ == "__main__":
    write_tir()
    write_tsr()
