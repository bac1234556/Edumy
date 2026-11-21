# -*- coding: utf-8 -*-
"""
Appends Cycle C3 Chrome 157 UI execution results (RUN-W-C3-0128 .. RUN-W-C3-0134)
into EDU-TEL-W-001_Execution_Log.csv and writes structured JSON evidence files
referencing all 16 copied screenshots. Zero secrets or raw passwords stored.
"""
import csv
import json
import os

BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
EV_DIR = os.path.join(BASE_DIR, "evidence")
LOG_CSV = os.path.join(BASE_DIR, "EDU-TEL-W-001_Execution_Log.csv")

BROWSER = "Google Chrome 157.0.0.0 (Windows x64, Chrome DevTools MCP)"
COMMIT = "Edumy-main@a44cb7d"
TS = "2026-11-22T00:37:49+07:00"

UI_RUNS = [
    {
        "runId": "RUN-W-C3-0128",
        "tcId": "TC-W-REG-FN-001",
        "module": "W-REG",
        "type": "FN",
        "priority": "High",
        "status": "Pass",
        "defectId": "",
        "actual": "Tạo khóa 1 trang CourseCreate.jsx thành công (Course ID=1066, 2 chương 1107/1108, 4 bài học gồm video preview + bài đọc, mời đồng GV qa.gvco@edumy.test [200]), 0 lỗi JS console, 0 nút/tab AI/ML; xác minh hiển thị đủ trên CourseCurriculumEditor.",
        "note": "Chrome 157 E2E PASS; có screenshot curriculum.",
        "evidenceJson": {
            "runId": "RUN-W-C3-0128",
            "tcId": "TC-W-REG-FN-001",
            "cycle": "C3",
            "commit": COMMIT,
            "browser": BROWSER,
            "timestamp": TS,
            "status": "Pass",
            "createdCourseId": 1066,
            "sectionsCreated": [1107, 1108],
            "lessonsCreated": 4,
            "coInstructorInvited": "qa.gvco@edumy.test (HTTP 200)",
            "aiMlUiElementsFound": 0,
            "consoleErrors": 0,
            "screenshots": ["evidence/RUN-W-C3-0128_TC-W-REG-FN-001_curriculum.png"]
        }
    },
    {
        "runId": "RUN-W-C3-0129",
        "tcId": "TC-W-REG-FN-002",
        "module": "W-REG",
        "type": "FN",
        "priority": "High",
        "status": "Pass",
        "defectId": "",
        "actual": "Validation inline tiếng Việt rõ từng trường (title, price=-1000, category, section, lesson); price=5000 bị API chặn 400 với banner rõ; partial failure (ghost@edumy.test->404) tạo khóa 1067 và hiện role=alert cảnh báo chi tiết trên curriculum; double-click tạo ĐÚNG 1 khóa 1068 (nút disable khi submit).",
        "note": "Chrome 157 E2E PASS cả 3 kịch bản (validation, partial failure, double-submit).",
        "evidenceJson": {
            "runId": "RUN-W-C3-0129",
            "tcId": "TC-W-REG-FN-002",
            "cycle": "C3",
            "commit": COMMIT,
            "browser": BROWSER,
            "timestamp": TS,
            "status": "Pass",
            "validationChecks": {
                "emptySubmitInline": True,
                "negativePriceInline": True,
                "price5000Api400Alert": True
            },
            "partialFailure": {
                "courseId": 1067,
                "coInstructorHttpStatus": 404,
                "warningAlertDisplayed": True
            },
            "doubleSubmit": {
                "courseId": 1068,
                "postCoursesCalls": 1,
                "duplicateCoursesCreated": 0
            },
            "screenshots": [
                "evidence/RUN-W-C3-0129_TC-W-REG-FN-002_validation.png",
                "evidence/RUN-W-C3-0129_TC-W-REG-FN-002_partial_failure.png",
                "evidence/RUN-W-C3-0129_TC-W-REG-FN-002_double_submit.png"
            ]
        }
    },
    {
        "runId": "RUN-W-C3-0130",
        "tcId": "TC-W-M07-PERF-003",
        "module": "W-M07",
        "type": "PERF",
        "priority": "Medium",
        "status": "Fail",
        "defectId": "DEF-W-031",
        "actual": "Trace Chrome 157: /(LCP=290ms,CLS=0.00,0 err), /courses(LCP=778ms,CLS=0.01,0 err), /courses/49(LCP=903ms,CLS=0.06, 1 console err: GET /api/v1/instructor/courses/49/co-instructors 403 cho Student HV1), /my-courses/47/learn(LCP=616ms,CLS=0.10, BigBuckBunny.mp4 ERR_BLOCKED_BY_ORB). Không đạt tiêu chí 0 lỗi console/4xx.",
        "note": "Mở DEF-W-031 (S3): CourseDetail gọi trái quyền API giảng viên gây 403 & video mẫu bị ORB.",
        "evidenceJson": {
            "runId": "RUN-W-C3-0130",
            "tcId": "TC-W-M07-PERF-003",
            "cycle": "C3",
            "commit": COMMIT,
            "browser": BROWSER,
            "timestamp": TS,
            "status": "Fail",
            "defectId": "DEF-W-031",
            "pages": [
                {"path": "/", "ttfbMs": 15, "lcpMs": 290, "cls": 0.00, "consoleErrors": 0, "failedNetwork": []},
                {"path": "/courses", "ttfbMs": 6, "lcpMs": 778, "cls": 0.01, "consoleErrors": 0, "failedNetwork": []},
                {"path": "/courses/49", "ttfbMs": 15, "lcpMs": 903, "cls": 0.06, "consoleErrors": 1, "failedNetwork": ["GET /api/v1/instructor/courses/49/co-instructors [403]"]},
                {"path": "/my-courses/47/learn", "ttfbMs": 6, "lcpMs": 616, "cls": 0.10, "consoleErrors": 0, "failedNetwork": ["BigBuckBunny.mp4 [net::ERR_BLOCKED_BY_ORB]"]}
            ]
        }
    },
    {
        "runId": "RUN-W-C3-0131",
        "tcId": "TC-W-M07-USA-001",
        "module": "W-M07",
        "type": "USA",
        "priority": "Medium",
        "status": "Fail",
        "defectId": "DEF-W-032",
        "actual": "4 trang (/, /courses, /courses/49, /instructor/courses/new) PASS cả 360/768/1366px; FAIL tràn ngang nghiêm trọng tại /cart (360px cắt giá 300.000₫, nút Áp dụng & Thanh toán) và /my-courses/47/learn (360px sidebar mở đè player cắt tiến độ; 768px cắt tab thứ 4 'Hỏi & Đáp (Q&A)'). Còn nhãn tiếng Anh (Instructor/Student/Draft/Published/NeedsReview).",
        "note": "Mở DEF-W-032 (S2 Usability): lỗi responsive /cart 360px & /learn 360px/768px.",
        "evidenceJson": {
            "runId": "RUN-W-C3-0131",
            "tcId": "TC-W-M07-USA-001",
            "cycle": "C3",
            "commit": COMMIT,
            "browser": BROWSER,
            "timestamp": TS,
            "status": "Fail",
            "defectId": "DEF-W-032",
            "matrix": {
                "/": {"360": "Pass", "768": "Pass", "1366": "Pass"},
                "/courses": {"360": "Pass", "768": "Pass", "1366": "Pass"},
                "/courses/49": {"360": "Pass", "768": "Pass", "1366": "Pass"},
                "/instructor/courses/new": {"360": "Pass", "768": "Pass", "1366": "Pass"},
                "/cart": {"360": "Fail (horizontal overflow clipping price & CTA)", "768": "Pass", "1366": "Pass"},
                "/my-courses/47/learn": {"360": "Fail (sidebar open & horizontal overflow)", "768": "Fail (Q&A tab clipped horizontally)", "1366": "Pass"}
            },
            "untranslatedEnglishLabels": ["Instructor", "Student", "Draft", "PendingApproval", "Published", "Hidden", "NeedsReview", "Development", "Business", "Design", "Marketing", "IT & Software"],
            "screenshots": [
                "evidence/RUN-W-C3-0131_TC-W-M07-USA-001_home_360.png",
                "evidence/RUN-W-C3-0131_TC-W-M07-USA-001_courses_360.png",
                "evidence/RUN-W-C3-0131_TC-W-M07-USA-001_detail_360.png",
                "evidence/RUN-W-C3-0131_TC-W-M07-USA-001_course_create_360.png",
                "evidence/RUN-W-C3-0131_TC-W-M07-USA-001_cart_360_FAIL.png",
                "evidence/RUN-W-C3-0131_TC-W-M07-USA-001_player_360_FAIL.png",
                "evidence/RUN-W-C3-0131_TC-W-M07-USA-001_player_768_FAIL.png"
            ]
        }
    },
    {
        "runId": "RUN-W-C3-0132",
        "tcId": "TC-W-M07-USA-002",
        "module": "W-M07",
        "type": "USA",
        "priority": "Medium",
        "status": "Pass",
        "defectId": "",
        "actual": "/register hiển thị lỗi inline tiếng Việt rõ từng trường (rỗng & sai định dạng); /cart trống có thông báo & nút khám phá; coupon EXPIRED2025 báo inline 'Mã giảm giá không tồn tại.'; HV1 vào /admin bị chặn và redirect về / (góp ý UX: nên hiện thêm toast báo thiếu quyền); /payment-cancel hiển thị tiếng Việt & 2 nút điều hướng.",
        "note": "Chrome 157 PASS (kèm khuyến nghị UX thêm toast khi redirect khỏi /admin).",
        "evidenceJson": {
            "runId": "RUN-W-C3-0132",
            "tcId": "TC-W-M07-USA-002",
            "cycle": "C3",
            "commit": COMMIT,
            "browser": BROWSER,
            "timestamp": TS,
            "status": "Pass",
            "checks": {
                "registerInlineFieldErrors": "Pass",
                "emptyCartFriendlyState": "Pass",
                "invalidCouponInlineError": "Pass",
                "adminBlockedForStudent": "Pass (silent redirect to /)",
                "paymentCancelFriendlyPage": "Pass"
            },
            "screenshots": [
                "evidence/RUN-W-C3-0132_TC-W-M07-USA-002_register_validation.png",
                "evidence/RUN-W-C3-0132_TC-W-M07-USA-002_cart_empty.png",
                "evidence/RUN-W-C3-0132_TC-W-M07-USA-002_cart_bad_coupon.png",
                "evidence/RUN-W-C3-0132_TC-W-M07-USA-002_payment_cancel.png"
            ]
        }
    },
    {
        "runId": "RUN-W-C3-0133",
        "tcId": "TC-W-M07-USA-003",
        "module": "W-M07",
        "type": "USA",
        "priority": "Medium",
        "status": "Pass",
        "defectId": "",
        "actual": "Điều hướng Tab/Enter trên Navbar, /login và /courses hoạt động mượt với focus ring xanh rõ nét; Lighthouse Accessibility đạt >= 90 trên cả 3 trang: /=93, /courses=94, /courses/49=94 (<html lang=vi>, landmark & img alt đạt 100%; ghi nhận điểm tối ưu nhỏ contrast rating-count & heading-order).",
        "note": "Chrome 157 Lighthouse Accessibility PASS (93/94/94 >= 90).",
        "evidenceJson": {
            "runId": "RUN-W-C3-0133",
            "tcId": "TC-W-M07-USA-003",
            "cycle": "C3",
            "commit": COMMIT,
            "browser": BROWSER,
            "timestamp": TS,
            "status": "Pass",
            "keyboardNavigation": {"navbarTabFocusRing": True, "loginTabEnterSubmit": True, "coursesFilterTabEnter": True},
            "lighthouseAccessibility": {
                "/": {"accessibility": 93, "bestPractices": 77, "seo": 83},
                "/courses": {"accessibility": 94, "bestPractices": 77, "seo": 83},
                "/courses/49": {"accessibility": 94, "bestPractices": 73, "seo": 83}
            },
            "screenshots": ["evidence/RUN-W-C3-0133_TC-W-M07-USA-003_keyboard_focus.png"]
        }
    },
    {
        "runId": "RUN-W-C3-0134",
        "tcId": "TC-W-M07-COMP-001",
        "module": "W-M07",
        "type": "COMP",
        "priority": "Medium",
        "status": "Blocked",
        "defectId": "",
        "actual": "Đã chạy đủ 5 luồng High trên Google Chrome 157.0.0.0 (Login/CreateCourse/Detail/Cart/Player hoạt động trên desktop; phát hiện DEF-W-031 403 trên /courses/49 và DEF-W-032 responsive /cart & /learn). Còn BLOCKED phần Microsoft Edge và Mozilla Firefox do phiên DevTools MCP chỉ kết nối Chrome và máy chưa cài Firefox.",
        "note": "Partial Chrome 157 hoàn tất; cần chạy bổ sung Edge & Firefox để đóng TC-W-M07-COMP-001.",
        "evidenceJson": {
            "runId": "RUN-W-C3-0134",
            "tcId": "TC-W-M07-COMP-001",
            "cycle": "C3",
            "commit": COMMIT,
            "browser": BROWSER,
            "timestamp": TS,
            "status": "Blocked",
            "browsers": {
                "Chrome 157.0.0.0": "Executed (5 High flows desktop PASS; DEF-W-031 & DEF-W-032 found)",
                "Microsoft Edge": "Not Executed in Chrome DevTools session",
                "Mozilla Firefox": "Not Installed on host"
            }
        }
    }
]

# Remove any prior C3 UI runs (RUN-W-C3-0128..0134) if re-running idempotently
rows = []
with open(LOG_CSV, encoding="utf-8", newline="") as f:
    reader = csv.DictReader(f)
    fieldnames = reader.fieldnames
    for r in reader:
        if r["Run ID"] not in {u["runId"] for u in UI_RUNS}:
            rows.append(r)

for u in UI_RUNS:
    ev_rel = f"evidence/{u['runId']}_{u['tcId']}.json"
    with open(os.path.join(BASE_DIR, ev_rel), "w", encoding="utf-8") as ef:
        json.dump(u["evidenceJson"], ef, ensure_ascii=False, indent=2)
    rows.append({
        "Run ID": u["runId"],
        "Chu kỳ": "C3",
        "TC ID": u["tcId"],
        "Module": u["module"],
        "Loại": u["type"],
        "Ưu tiên": u["priority"],
        "Trạng thái": u["status"],
        "Kết quả thực tế": u["actual"],
        "Defect ID": u["defectId"],
        "Bằng chứng": ev_rel,
        "Commit Hash": COMMIT,
        "Trình duyệt/Công cụ": BROWSER,
        "Thời điểm (Asia/Ho_Chi_Minh)": TS,
        "Người thực hiện": "QA Lead",
        "Ghi chú": u["note"],
    })

with open(LOG_CSV, "w", encoding="utf-8", newline="") as f:
    w = csv.DictWriter(f, fieldnames=fieldnames)
    w.writeheader()
    w.writerows(rows)

print(f"Wrote 7 C3 UI evidence JSONs and updated {LOG_CSV} (total rows={len(rows)})")
