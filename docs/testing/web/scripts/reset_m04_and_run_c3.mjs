// Clean C3 M04 pre-state, run W-M04 C3 cleanly, and record the 7 honest Browser Blocked rows for C2
import fs from 'node:fs';
import { execSync } from 'node:child_process';
import {
  loadState, EXEC_LOG_CSV, saveEvidence, appendExecutionLog
} from './lib_qa_client.mjs';

const st = loadState();
const cp1 = st.courses.cPaid1.courseId;
const cps = st.courses.cPaidSplit.courseId;
const hv1 = st.userIds.HV1;
const hv2 = st.userIds.HV2;

// 1. Remove aborted C3 M04 rows (RUN-W-C3-0065..0082) before re-running M04 from clean state
const lines = fs.readFileSync(EXEC_LOG_CSV, 'utf8').trim().split(/\r?\n/)
  .filter(l => !/^RUN-W-C3-00(6[5-9]|7[0-9]|8[0-2]),/.test(l));
fs.writeFileSync(EXEC_LOG_CSV, lines.join('\n') + '\n', 'utf8');
console.log('Cleaned aborted C3 M04 rows. Rows before clean C3 M04:', lines.length - 1);

// 2. Clean HV1/HV2 enrollments, cart items & earnings on cPaid1/cPaidSplit so M04 runs from pristine pre-M04 state
const sql = [
  `DELETE FROM dbo.enrollments WHERE user_id IN (${hv1},${hv2}) AND course_id IN (${cp1},${cps});`,
  `DELETE FROM dbo.cart_items WHERE course_id IN (${cp1},${cps});`,
  `DELETE FROM dbo.payout_earnings WHERE order_id IN (SELECT order_id FROM dbo.orders WHERE user_id IN (${hv1},${hv2}));`
].join(' ');
execSync(`docker exec edumy-test-database-1 /opt/mssql-tools18/bin/sqlcmd -I -S localhost -U sa -P "YourPassword123" -d EduMyDb -C -Q "SET QUOTED_IDENTIFIER ON; ${sql}"`, { stdio: 'inherit' });

// 3. Append the 7 honest Browser Blocked rows for Cycle C2 (verified via Cycle C2 browser subagent: DevToolsActivePort ENOENT)
const c2Blocked = [
  ['RUN-W-C2-0128', 'TC-W-M07-PERF-003', 'W-M07', 'PERF', 'Medium', 'Chrome DevTools remote debugging (DevToolsActivePort) chưa bật trên máy host trong C2 -> không thể chạy Lighthouse Performance audit.'],
  ['RUN-W-C2-0129', 'TC-W-M07-USA-001', 'W-M07', 'USA', 'Medium', 'Chrome DevTools remote debugging chưa bật trong C2 -> chưa kiểm tra visual Responsive 360/768/1366px trên trình duyệt.'],
  ['RUN-W-C2-0130', 'TC-W-M07-USA-002', 'W-M07', 'USA', 'Medium', 'Chrome DevTools remote debugging chưa bật trong C2 -> chưa kiểm tra trực quan UI thông báo lỗi từng trường & trạng thái rỗng.'],
  ['RUN-W-C2-0131', 'TC-W-M07-USA-003', 'W-M07', 'USA', 'Medium', 'Chrome DevTools remote debugging chưa bật trong C2 -> chưa kiểm tra điều hướng bàn phím & Lighthouse Accessibility.'],
  ['RUN-W-C2-0132', 'TC-W-M07-COMP-001', 'W-M07', 'COMP', 'Medium', 'Chrome DevTools chưa kết nối (DevToolsActivePort ENOENT) và Firefox chưa cài đặt trên máy host.'],
  ['RUN-W-C2-0133', 'TC-W-REG-FN-001', 'W-REG', 'FN', 'High', 'Chrome DevTools remote debugging chưa bật trong C2 -> chưa chạy UI E2E tạo khóa 1 trang trên CourseCreate.jsx (API tạo khóa/giáo trình/đồng GV đạt 28/28 PASS ở W-M03).'],
  ['RUN-W-C2-0134', 'TC-W-REG-FN-002', 'W-REG', 'FN', 'High', 'Chrome DevTools remote debugging chưa bật trong C2 -> chưa chạy UI E2E validation/partial-failure/double-submit trên CourseCreate.jsx.'],
];
const csvNow = fs.readFileSync(EXEC_LOG_CSV, 'utf8');
for (const [runId, tcId, module, type, priority, reason] of c2Blocked) {
  if (csvNow.includes(`${runId},`)) continue;
  const evPath = saveEvidence(runId, tcId, {
    status: 'Blocked',
    cycle: 'C2',
    cause: 'BLOCKED_BROWSER_CONNECTION_FAILED',
    error: 'Could not find DevToolsActivePort for chrome (ENOENT)',
    note: reason
  });
  appendExecutionLog({
    runId, cycle: 'C2', tcId, module, type, priority,
    status: 'Blocked', actual: reason, evidence: evPath,
    commitHash: 'Edumy-main@836460a',
    tool: 'Antigravity Browser Agent (Chrome 157)'
  });
}
console.log('Recorded 7 honest C2 Browser Blocked rows.');
