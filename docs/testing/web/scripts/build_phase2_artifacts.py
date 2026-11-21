# -*- coding: utf-8 -*-
"""
Build Phase 2 QA Artifacts for Edumy Web:
1. requirements_inventory.csv
2. EDU-TCS-W-001_Test_Cases.xlsx (8 sheets: Cover, TestCases, ExecutionLog, Defects, Summary, RTM, IDOR, NFR)
3. EDU-TEL-W-001_Execution_Log.csv (empty schema-ready log)
Also validates 100% requirement coverage, ID format, NFR >= 2 TCs, and prints summary tables.
"""

import csv
import os
import re
from collections import Counter, defaultdict

import openpyxl
from openpyxl.styles import Alignment, Border, Font, PatternFill, Side
from openpyxl.utils import get_column_letter

from qa_requirements_data import IDOR_MATRIX, IDOR_ROLES, NFR_SPECS, REQUIREMENTS
from qa_testcases_part1 import TEST_CASES_PART1
from qa_testcases_part2 import TEST_CASES_PART2
from qa_testcases_part3 import TEST_CASES_PART3

BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
REQ_CSV = os.path.join(BASE_DIR, "requirements_inventory.csv")
EXEC_CSV = os.path.join(BASE_DIR, "EDU-TEL-W-001_Execution_Log.csv")
XLSX_PATH = os.path.join(BASE_DIR, "EDU-TCS-W-001_Test_Cases.xlsx")

ALL_TCS = sorted(
    TEST_CASES_PART1 + TEST_CASES_PART2 + TEST_CASES_PART3,
    key=lambda x: (MODULES_ORDER.index(x[1]), TYPES_ORDER.index(x[5]), x[0])
) if False else (TEST_CASES_PART1 + TEST_CASES_PART2 + TEST_CASES_PART3)

TC_ID_RE = re.compile(r"^TC-W-(M0[1-7]|REG)-(FN|SEC|PERF|REL|REC|OBS|USA|COMP|MNT|DEP)-\d{3}$")

TC_HEADERS = [
    "TC ID", "Module", "Kịch bản (TS)", "Truy vết (UC/BR/AC/NFR/D)", "Tiêu đề",
    "Loại", "Cấp độ", "Kỹ thuật", "Ưu tiên", "Vai trò thực hiện",
    "Tiền điều kiện", "Dữ liệu test", "Các bước", "Kết quả mong đợi",
    "Kết quả thực tế", "Trạng thái", "Số lần thực thi", "Run ID gần nhất",
    "Defect ID", "Bằng chứng", "Môi trường/Build", "Người thực hiện", "Ngày"
]

EXEC_HEADERS = [
    "Run ID", "Chu kỳ", "TC ID", "Module", "Loại", "Ưu tiên",
    "Trạng thái", "Kết quả thực tế", "Defect ID", "Bằng chứng",
    "Commit Hash", "Trình duyệt/Công cụ", "Thời điểm (Asia/Ho_Chi_Minh)", "Người thực hiện", "Ghi chú"
]

DEFECT_HEADERS = [
    "Defect ID", "Tiêu đề", "Module", "Loại lỗi", "Severity", "Priority",
    "Trạng thái", "TC ID liên quan", "Truy vết yêu cầu", "Run ID phát hiện",
    "Mô tả & Các bước tái hiện", "Kết quả mong đợi", "Kết quả thực tế",
    "Bằng chứng", "Commit phát hiện", "Commit sửa", "Ngày phát hiện", "Ngày đóng/Ghi chú"
]

MODULES_ORDER = ["W-M01", "W-M02", "W-M03", "W-M04", "W-M05", "W-M06", "W-M07", "W-REG"]
TYPES_ORDER = ["FN", "SEC", "PERF", "REL", "REC", "OBS", "USA", "COMP", "MNT", "DEP"]
PRIOS_ORDER = ["High", "Medium", "Low"]

ALL_TCS = sorted(
    ALL_TCS,
    key=lambda x: (MODULES_ORDER.index(x[1]), TYPES_ORDER.index(x[5]), x[0])
)


def expand_trace_tokens(trace_str: str):
    """Expand ranges like UC-A01..A05, UC-I15..I17, AC-14..AC-16, CODE-PAY-01..04 into exact IDs."""
    tokens = set()
    for raw in [t.strip() for t in trace_str.split(",") if t.strip()]:
        if ".." in raw:
            left, right = raw.split("..", 1)
            left, right = left.strip(), right.strip()
            # Match prefix + numeric suffix
            m_l = re.match(r"^(.*?)(\d+)$", left)
            m_r = re.match(r"^(.*?)(\d+)$", right)
            if m_l and m_r:
                pref_l, num_l = m_l.group(1), m_l.group(2)
                num_r = m_r.group(2)
                width = len(num_l)
                for n in range(int(num_l), int(num_r) + 1):
                    tokens.add(f"{pref_l}{str(n).zfill(width)}")
                continue
        tokens.add(raw)
    return tokens


def validate_and_map():
    seen_ids = set()
    req_to_tcs = defaultdict(list)
    req_ids = {r[0] for r in REQUIREMENTS}

    for tc in ALL_TCS:
        tc_id = tc[0]
        assert TC_ID_RE.match(tc_id), f"Invalid TC ID format: {tc_id}"
        assert tc_id not in seen_ids, f"Duplicate TC ID: {tc_id}"
        seen_ids.add(tc_id)
        for token in expand_trace_tokens(tc[3]):
            if token in req_ids:
                req_to_tcs[token].append(tc_id)

    missing_reqs = [r for r in REQUIREMENTS if not req_to_tcs[r[0]]]
    nfr_under_2 = [r[0] for r in REQUIREMENTS if r[1] == "NFR" and len(req_to_tcs[r[0]]) < 2]
    uc_under_2 = [r[0] for r in REQUIREMENTS if r[1] == "UC" and len(req_to_tcs[r[0]]) < 2]
    code_under_2 = [r[0] for r in REQUIREMENTS if r[1] == "CODE" and r[0].startswith("CODE-") and len(req_to_tcs[r[0]]) < 2]
    ac_not_gwt = []
    for r in REQUIREMENTS:
        if r[1] == "AC":
            tcs_for_ac = [tc for tc in ALL_TCS if r[0] in expand_trace_tokens(tc[3])]
            if not any(("Given " in tc[12] and "When " in tc[12] and "Then " in tc[12]) for tc in tcs_for_ac):
                ac_not_gwt.append(r[0])

    # Cross-check IDOR and NFR TC references
    for grp, _, _, _, tc_id in IDOR_MATRIX:
        assert tc_id in seen_ids, f"IDOR references unknown TC: {tc_id} ({grp})"
    for nfr_id, _, _, tc_list in NFR_SPECS:
        for tid in [x.strip() for x in tc_list.split(",") if x.strip()]:
            assert tid in seen_ids, f"NFR {nfr_id} references unknown TC: {tid}"

    return req_to_tcs, missing_reqs, nfr_under_2, uc_under_2, code_under_2, ac_not_gwt


def write_csvs():
    with open(REQ_CSV, "w", newline="", encoding="utf-8-sig") as f:
        w = csv.writer(f)
        w.writerow(["id", "loai", "mo_ta_ngan", "module", "kenh", "uu_tien_moscow", "nguon_test_basis", "trang_thai_dac_ta"])
        for r in REQUIREMENTS:
            w.writerow(r)

    with open(EXEC_CSV, "w", newline="", encoding="utf-8-sig") as f:
        w = csv.writer(f)
        w.writerow(EXEC_HEADERS)


def style_sheet_header(ws, row_idx, ncols, fill_hex="1F4E78"):
    fill = PatternFill("solid", fgColor=fill_hex)
    font = Font(name="Calibri", size=11, bold=True, color="FFFFFF")
    thin = Side(style="thin", color="BFBFBF")
    border = Border(left=thin, right=thin, top=thin, bottom=thin)
    for c in range(1, ncols + 1):
        cell = ws.cell(row=row_idx, column=c)
        cell.fill = fill
        cell.font = font
        cell.alignment = Alignment(horizontal="center", vertical="center", wrap_text=True)
        cell.border = border
    ws.row_dimensions[row_idx].height = 28


def style_data_rows(ws, start_row, end_row, ncols):
    thin = Side(style="thin", color="D9D9D9")
    border = Border(left=thin, right=thin, top=thin, bottom=thin)
    alt_fill = PatternFill("solid", fgColor="F7FAFC")
    font = Font(name="Calibri", size=10)
    for r in range(start_row, end_row + 1):
        for c in range(1, ncols + 1):
            cell = ws.cell(row=r, column=c)
            cell.font = font
            cell.border = border
            cell.alignment = Alignment(vertical="top", wrap_text=True)
            if r % 2 == 1:
                cell.fill = alt_fill


def build_xlsx(req_to_tcs):
    wb = openpyxl.Workbook()

    # -------------------- 1. Cover --------------------
    ws_cov = wb.active
    ws_cov.title = "Cover"
    cover_rows = [
        ("MÃ TÀI LIỆU", "EDU-TCS-W-001"),
        ("TÊN TÀI LIỆU", "Bộ Test Cases, Ma trận Truy vết (RTM), Ma trận IDOR & NFR — Edumy Web"),
        ("PHIÊN BẢN", "1.0 (Pha 2 — Chờ phê duyệt trước khi thực thi C1)"),
        ("NGÀY LẬP", "2026-11-21 (Asia/Ho_Chi_Minh)"),
        ("CHUẨN ÁP DỤNG", "ISO/IEC/IEEE 29119-2, 29119-3, 29119-4 | ISO/IEC 25010 | OWASP Top 10 (2021) & ASVS L1"),
        ("COMMIT CƠ SỞ", "Edumy-Project: f2f5240 | Edumy-main: bf4ac6c"),
        ("PHẠM VI", "Toàn bộ chức năng (W-M01..W-M07, W-REG) & phi chức năng (SEC, PERF, REL, REC, OBS, USA, COMP, MNT, DEP) của Edumy Web"),
        ("TỔNG SỐ YÊU CẦU TRONG INVENTORY", len(REQUIREMENTS)),
        ("TỔNG SỐ TEST CASES", len(ALL_TCS)),
        ("TRẠNG THÁI HIỆN TẠI", "Not Run (100%) — Chưa thực thi, tuân thủ quy tắc không bịa kết quả"),
        ("CẤU TRÚC WORKBOOK", "8 Sheets: Cover, TestCases, ExecutionLog, Defects, Summary, RTM, IDOR, NFR"),
        ("GHI CHÚ TEST BASIS", "EDU-SRS-001_v3.0, EDU-DDD-001, EDU-MTP-001 chưa có bản gốc trong workspace; đã được Stakeholder phê duyệt lập dựa trên tóm tắt prompt, api-contract-v2.md, openapi.json, Edumy/docs/system/*, SQL scripts 06-08 và mã nguồn."),
    ]
    ws_cov.merge_cells("A1:B1")
    ws_cov["A1"] = "EDUMY WEB — SYSTEM TEST CASE SPECIFICATION (EDU-TCS-W-001)"
    ws_cov["A1"].font = Font(name="Calibri", size=15, bold=True, color="FFFFFF")
    ws_cov["A1"].fill = PatternFill("solid", fgColor="1F4E78")
    ws_cov["A1"].alignment = Alignment(horizontal="center", vertical="center")
    ws_cov.row_dimensions[1].height = 36
    for idx, (k, v) in enumerate(cover_rows, start=3):
        ws_cov.cell(row=idx, column=1, value=k).font = Font(name="Calibri", size=11, bold=True)
        ws_cov.cell(row=idx, column=2, value=v).font = Font(name="Calibri", size=11)
        ws_cov.cell(row=idx, column=1).fill = PatternFill("solid", fgColor="EDF2F7")
        ws_cov.cell(row=idx, column=2).alignment = Alignment(wrap_text=True)
    ws_cov.column_dimensions["A"].width = 36
    ws_cov.column_dimensions["B"].width = 95

    # -------------------- 2. TestCases --------------------
    ws_tc = wb.create_sheet("TestCases")
    ws_tc.append(TC_HEADERS)
    style_sheet_header(ws_tc, 1, len(TC_HEADERS))
    for tc in ALL_TCS:
        row = list(tc) + ["", "Not Run", 0, "", "", "", "Edumy-main@bf4ac6c", "QA Lead", ""]
        ws_tc.append(row)
    style_data_rows(ws_tc, 2, len(ALL_TCS) + 1, len(TC_HEADERS))
    ws_tc.freeze_panes = "C2"
    ws_tc.auto_filter.ref = f"A1:{get_column_letter(len(TC_HEADERS))}{len(ALL_TCS)+1}"
    tc_widths = [18, 9, 15, 28, 42, 8, 15, 22, 10, 18, 32, 36, 48, 46, 26, 12, 10, 16, 14, 24, 20, 14, 14]
    for i, w in enumerate(tc_widths, 1):
        ws_tc.column_dimensions[get_column_letter(i)].width = w

    # -------------------- 3. ExecutionLog --------------------
    ws_ex = wb.create_sheet("ExecutionLog")
    ws_ex.append(EXEC_HEADERS)
    style_sheet_header(ws_ex, 1, len(EXEC_HEADERS))
    ws_ex.freeze_panes = "A2"
    for i in range(1, len(EXEC_HEADERS) + 1):
        ws_ex.column_dimensions[get_column_letter(i)].width = 20

    # -------------------- 4. Defects --------------------
    ws_df = wb.create_sheet("Defects")
    ws_df.append(DEFECT_HEADERS)
    style_sheet_header(ws_df, 1, len(DEFECT_HEADERS), fill_hex="9B2C2C")
    ws_df.freeze_panes = "A2"
    for i in range(1, len(DEFECT_HEADERS) + 1):
        ws_df.column_dimensions[get_column_letter(i)].width = 22

    # -------------------- 5. Summary --------------------
    ws_sm = wb.create_sheet("Summary")
    ws_sm["A1"] = "BẢNG TỔNG HỢP TEST CASES THEO MODULE × LOẠI × ƯU TIÊN (Tự động bằng công thức Excel)"
    ws_sm["A1"].font = Font(name="Calibri", size=13, bold=True, color="1F4E78")

    # Table A: Module x Type
    headers_mt = ["Module"] + TYPES_ORDER + ["High", "Medium", "Low", "Tổng TC", "Pass", "Fail", "Blocked", "Not Run"]
    ws_sm.append([])
    ws_sm.append(headers_mt)
    style_sheet_header(ws_sm, 3, len(headers_mt))
    last_tc_row = len(ALL_TCS) + 1

    for idx, mod in enumerate(MODULES_ORDER, start=4):
        row = [mod]
        for t in TYPES_ORDER:
            row.append(f'=COUNTIFS(TestCases!$B$2:$B${last_tc_row},"{mod}",TestCases!$F$2:$F${last_tc_row},"{t}")')
        for p in PRIOS_ORDER:
            row.append(f'=COUNTIFS(TestCases!$B$2:$B${last_tc_row},"{mod}",TestCases!$I$2:$I${last_tc_row},"{p}")')
        row.append(f'=COUNTIF(TestCases!$B$2:$B${last_tc_row},"{mod}")')
        for st in ["Pass", "Fail", "Blocked", "Not Run"]:
            row.append(f'=COUNTIFS(TestCases!$B$2:$B${last_tc_row},"{mod}",TestCases!$P$2:$P${last_tc_row},"{st}")')
        ws_sm.append(row)

    tot_row = 4 + len(MODULES_ORDER)
    tot = ["TỔNG"]
    for c_idx in range(2, len(headers_mt) + 1):
        col_l = get_column_letter(c_idx)
        tot.append(f"=SUM({col_l}4:{col_l}{tot_row-1})")
    ws_sm.append(tot)
    style_data_rows(ws_sm, 4, tot_row, len(headers_mt))
    for c_idx in range(1, len(headers_mt) + 1):
        ws_sm.cell(row=tot_row, column=c_idx).font = Font(name="Calibri", size=10, bold=True)
        ws_sm.cell(row=tot_row, column=c_idx).fill = PatternFill("solid", fgColor="D9E1F2")
    for c_idx in range(1, len(headers_mt) + 1):
        ws_sm.column_dimensions[get_column_letter(c_idx)].width = 11
    ws_sm.column_dimensions["A"].width = 14

    # -------------------- 6. RTM --------------------
    ws_rtm = wb.create_sheet("RTM")
    rtm_headers = [
        "Req ID", "Loại", "Mô tả yêu cầu", "Module", "Kênh", "MoSCoW",
        "Trạng thái đặc tả", "Danh sách TC ID", "Số lượng TC", "Độ phủ", "Trạng thái tổng hợp", "Khoảng trống / Ghi chú"
    ]
    ws_rtm.append(rtm_headers)
    style_sheet_header(ws_rtm, 1, len(rtm_headers))
    for r in REQUIREMENTS:
        tcs = req_to_tcs[r[0]]
        ws_rtm.append([
            r[0], r[1], r[2], r[3], r[4], r[5], r[7],
            ", ".join(tcs), len(tcs), "Covered" if tcs else "GAP",
            "Not Run", "" if tcs else "Chưa có TC"
        ])
    style_data_rows(ws_rtm, 2, len(REQUIREMENTS) + 1, len(rtm_headers))
    ws_rtm.freeze_panes = "B2"
    ws_rtm.auto_filter.ref = f"A1:L{len(REQUIREMENTS)+1}"
    rtm_widths = [15, 8, 52, 9, 8, 10, 26, 38, 12, 12, 18, 24]
    for i, w in enumerate(rtm_widths, 1):
        ws_rtm.column_dimensions[get_column_letter(i)].width = w

    # -------------------- 7. IDOR --------------------
    ws_idor = wb.create_sheet("IDOR")
    idor_headers = ["Nhóm Endpoint", "Endpoint đại diện", "Tài nguyên mục tiêu", "TC ID"] + \
                   [f"Kỳ vọng: {r}" for r in IDOR_ROLES] + \
                   [f"Thực tế: {r}" for r in IDOR_ROLES] + ["Kết luận"]
    ws_idor.append(idor_headers)
    style_sheet_header(ws_idor, 1, len(idor_headers), fill_hex="2C5282")
    for grp, ep, owner, exp_map, tc_id in IDOR_MATRIX:
        row = [grp, ep, owner, tc_id] + [exp_map[r] for r in IDOR_ROLES] + ["Not Run"] * len(IDOR_ROLES) + ["Not Run"]
        ws_idor.append(row)
    style_data_rows(ws_idor, 2, len(IDOR_MATRIX) + 1, len(idor_headers))
    ws_idor.freeze_panes = "C2"
    for i in range(1, len(idor_headers) + 1):
        ws_idor.column_dimensions[get_column_letter(i)].width = 18 if i > 4 else (38 if i in (2, 3) else 16)

    # -------------------- 8. NFR --------------------
    ws_nfr = wb.create_sheet("NFR")
    nfr_headers = ["NFR ID", "Chỉ số đo lường", "Ngưỡng chấp nhận", "TC ID liên quan", "Số đo thực tế", "Đạt / Không đạt", "Run ID", "Bằng chứng"]
    ws_nfr.append(nfr_headers)
    style_sheet_header(ws_nfr, 1, len(nfr_headers), fill_hex="276749")
    for nfr_id, metric, threshold, tc_list in NFR_SPECS:
        ws_nfr.append([nfr_id, metric, threshold, tc_list, "", "Not Run", "", ""])
    style_data_rows(ws_nfr, 2, len(NFR_SPECS) + 1, len(nfr_headers))
    nfr_widths = [10, 44, 34, 42, 24, 16, 16, 26]
    for i, w in enumerate(nfr_widths, 1):
        ws_nfr.column_dimensions[get_column_letter(i)].width = w

    wb.save(XLSX_PATH)


def print_report(req_to_tcs, missing_reqs, nfr_under_2, uc_under_2, code_under_2, ac_not_gwt):
    print(f"Total Requirements in inventory: {len(REQUIREMENTS)}")
    by_req_type = Counter(r[1] for r in REQUIREMENTS)
    print("Requirements by type:", dict(by_req_type))
    print(f"Total Test Cases: {len(ALL_TCS)}")
    print(f"Missing requirements (without TC): {len(missing_reqs)}")
    for m in missing_reqs:
        print("  MISSING:", m[0], m[2])
    print(f"NFRs with < 2 TCs: {nfr_under_2}")
    print(f"UCs with < 2 TCs ({len(uc_under_2)}): {uc_under_2}")
    print(f"CODE-* with < 2 TCs ({len(code_under_2)}): {code_under_2}")
    print(f"ACs without Given/When/Then: {ac_not_gwt}")

    print("\n=== TABLE: MODULE x TYPE x PRIORITY ===")
    header = "| Module | " + " | ".join(TYPES_ORDER) + " | High | Medium | Low | Tổng |"
    sep = "|---|" + "---:|" * (len(TYPES_ORDER) + 4)
    print(header)
    print(sep)
    col_totals = Counter()
    for mod in MODULES_ORDER:
        tcs = [tc for tc in ALL_TCS if tc[1] == mod]
        tc_types = Counter(tc[5] for tc in tcs)
        tc_prios = Counter(tc[8] for tc in tcs)
        cells = [str(tc_types[t]) for t in TYPES_ORDER] + [str(tc_prios[p]) for p in PRIOS_ORDER] + [str(len(tcs))]
        for t in TYPES_ORDER:
            col_totals[t] += tc_types[t]
        for p in PRIOS_ORDER:
            col_totals[p] += tc_prios[p]
        col_totals["ALL"] += len(tcs)
        print(f"| **{mod}** | " + " | ".join(cells) + " |")
    tot_cells = [str(col_totals[t]) for t in TYPES_ORDER] + [str(col_totals[p]) for p in PRIOS_ORDER] + [str(col_totals["ALL"])]
    print(f"| **TỔNG** | " + " | ".join(f"**{c}**" for c in tot_cells) + " |")


if __name__ == "__main__":
    req_to_tcs, missing_reqs, nfr_under_2, uc_under_2, code_under_2, ac_not_gwt = validate_and_map()
    write_csvs()
    build_xlsx(req_to_tcs)
    print_report(req_to_tcs, missing_reqs, nfr_under_2, uc_under_2, code_under_2, ac_not_gwt)
    print(f"\nGenerated:\n- {REQ_CSV}\n- {EXEC_CSV}\n- {XLSX_PATH}")
