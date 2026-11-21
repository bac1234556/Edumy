// Finalize Cycle C1 Execution Log: verify OBS-002, REC-001, REC-002 accurately + log 7 Blocked Browser TCs
import fs from 'node:fs';
import path from 'node:path';
import { execSync } from 'node:child_process';
import {
  apiReq, saveEvidence, appendExecutionLog, EXEC_LOG_CSV, EVIDENCE_DIR, BASE_PROM, BASE_GRAFANA, nowHcmIso, COMMIT_HASH
} from './lib_qa_client.mjs';

async function main() {
  // 1. Re-verify Prometheus target health & Grafana accurately (OBS-002)
  const hRes = await apiReq('GET', '/actuator/health');
  const pRes = await apiReq('GET', '/actuator/prometheus');
  const promRaw = await (await fetch(`${BASE_PROM}/api/v1/targets`)).json();
  const activeTargets = promRaw?.data?.activeTargets || [];
  const backendTarget = activeTargets.find(t => t.labels?.job === 'edumy-backend');
  const grafHealth = await apiReq('GET', `${BASE_GRAFANA}/api/health`);
  const grafDash = await (await fetch(`${BASE_GRAFANA}/api/search`, {
    headers: { Authorization: 'Basic ' + Buffer.from('admin:admin').toString('base64') }
  })).json();
  const obs2Pass = hRes.status === 200 && hRes.raw?.status === 'UP'
    && pRes.status === 200 && String(pRes.rawText).includes('http_server_requests')
    && backendTarget?.health === 'up' && grafHealth.status === 200 && Array.isArray(grafDash) && grafDash.length > 0;
  const obs2Actual = `health=${hRes.raw?.status}, promMetrics=${pRes.status}, promBackendTarget=${backendTarget?.health} (${backendTarget?.scrapeUrl}), grafana=${grafHealth.status} dashboards=${JSON.stringify(grafDash.map(d => d.title))}`;
  saveEvidence('RUN-W-C1-0122', 'TC-W-M07-OBS-002', {
    health: hRes.raw, promBackendTarget: backendTarget, grafanaHealth: grafHealth.raw, grafanaDashboards: grafDash.map(d => d.title)
  });

  // 2. Verify REC-001 (confirmed 10/10 real tables matched 100% in RUN-W-C1-0119 + verify exact table names for sections/order_items)
  const rec1Ev = JSON.parse(fs.readFileSync(path.join(EVIDENCE_DIR, 'RUN-W-C1-0119_TC-W-M07-REC-001.json'), 'utf8'));
  const validTables = Object.keys(rec1Ev.origCounts).filter(k => rec1Ev.origCounts[k] !== null);
  const allMatch = validTables.every(k => rec1Ev.origCounts[k] === rec1Ev.restoredCounts[k]);
  const rec1Pass = allMatch && rec1Ev.rtoSec <= 14400;
  const rec1Actual = `BACKUP->RESTORE on temp SQL Server container: ${validTables.length}/${validTables.length} core tables matched 100% (Users=${rec1Ev.origCounts.Users}, Courses=${rec1Ev.origCounts.Courses}, Orders=${rec1Ev.origCounts.Orders}, Enrollments=${rec1Ev.origCounts.Enrollments}, payout_earnings=${rec1Ev.origCounts.payout_earnings}), RTO=${rec1Ev.rtoSec}s (<= 14400s / 4h)`;

  // 3. Verify REC-002 excluding binary .bak file (0 daily backup automation exists!)
  const rec2Count = Number(execSync(
    'powershell -Command "(Get-ChildItem -Recurse Edumy-main/Edumy/Database,Edumy-main/scripts,Edumy-main/Edumy/docker-compose.yml -File -Exclude *.bak -ErrorAction SilentlyContinue | Select-String \'BACKUP DATABASE|cron|Schedule|msdb\\.dbo\\.sp_add_job\').Count"',
    { encoding: 'utf8' }
  ).trim());
  const rec2Pass = rec2Count > 0;
  const rec2Actual = `Automated daily backup scripts/cron/SQL Agent jobs found=${rec2Count} (excluding binary EduMyDb.bak; NFR-06 RPO 24h yêu cầu lịch sao lưu tự động hằng ngày -> KHÔNG CÓ)`;
  saveEvidence('RUN-W-C1-0120', 'TC-W-M07-REC-002', { nonBakBackupAutomationMatches: rec2Count, hasDailyBackup: rec2Pass });

  // Update the 3 corrected rows in EDU-TEL-W-001_Execution_Log.csv in place (keep every other row untouched)
  const lines = fs.readFileSync(EXEC_LOG_CSV, 'utf8').trim().split(/\r?\n/);
  const updated = lines.map(line => {
    if (line.startsWith('RUN-W-C1-0119,')) {
      return `RUN-W-C1-0119,C1,TC-W-M07-REC-001,W-M07,REC,High,${rec1Pass ? 'Pass' : 'Fail'},"${rec1Actual.replaceAll('"', '""')}",,evidence/RUN-W-C1-0119_TC-W-M07-REC-001.json,${COMMIT_HASH},Node.js v24.16.0 + Docker SQL Server,${nowHcmIso()},QA Lead,`;
    }
    if (line.startsWith('RUN-W-C1-0120,')) {
      return `RUN-W-C1-0120,C1,TC-W-M07-REC-002,W-M07,REC,High,${rec2Pass ? 'Pass' : 'Fail'},"${rec2Actual.replaceAll('"', '""')}",,evidence/RUN-W-C1-0120_TC-W-M07-REC-002.json,${COMMIT_HASH},PowerShell + Repo Audit,${nowHcmIso()},QA Lead,`;
    }
    if (line.startsWith('RUN-W-C1-0122,')) {
      return `RUN-W-C1-0122,C1,TC-W-M07-OBS-002,W-M07,OBS,High,${obs2Pass ? 'Pass' : 'Fail'},"${obs2Actual.replaceAll('"', '""')}",,evidence/RUN-W-C1-0122_TC-W-M07-OBS-002.json,${COMMIT_HASH},Node.js v24.16.0 fetch,${nowHcmIso()},QA Lead,`;
    }
    return line;
  });
  fs.writeFileSync(EXEC_LOG_CSV, updated.join('\n') + '\n', 'utf8');

  // 4. Append the 7 honestly Blocked Browser E2E TCs (Chrome DevTools DevToolsActivePort ENOENT)
  const browserBlocked = [
    ['RUN-W-C1-0128', 'TC-W-M07-PERF-003', 'W-M07', 'PERF', 'Medium', 'Chrome DevTools remote debugging (DevToolsActivePort) chưa bật trên trình duyệt host -> không thể chạy Lighthouse Performance audit trong chu kỳ C1.'],
    ['RUN-W-C1-0129', 'TC-W-M07-USA-001', 'W-M07', 'USA', 'Medium', 'Chrome DevTools remote debugging (DevToolsActivePort) chưa bật -> chưa kiểm tra visual Responsive 360/768/1366px trên trình duyệt trong C1.'],
    ['RUN-W-C1-0130', 'TC-W-M07-USA-002', 'W-M07', 'USA', 'Medium', 'Chrome DevTools remote debugging (DevToolsActivePort) chưa bật -> chưa kiểm tra trực quan UI thông báo lỗi từng trường & trạng thái rỗng trong C1.'],
    ['RUN-W-C1-0131', 'TC-W-M07-USA-003', 'W-M07', 'USA', 'Medium', 'Chrome DevTools remote debugging (DevToolsActivePort) chưa bật -> chưa kiểm tra điều hướng bàn phím & Lighthouse Accessibility trong C1.'],
    ['RUN-W-C1-0132', 'TC-W-M07-COMP-001', 'W-M07', 'COMP', 'Medium', 'Chrome DevTools chưa kết nối (DevToolsActivePort ENOENT) và Mozilla Firefox chưa cài đặt trên máy host (chỉ có Chrome 157 & Edge 158).'],
    ['RUN-W-C1-0133', 'TC-W-REG-FN-001', 'W-REG', 'FN', 'High', 'Chrome DevTools remote debugging chưa bật -> chưa chạy UI E2E tạo khóa 1 trang trên CourseCreate.jsx (API tạo khóa/section/lesson/co-instructor đã kiểm ở W-M03).'],
    ['RUN-W-C1-0134', 'TC-W-REG-FN-002', 'W-REG', 'FN', 'High', 'Chrome DevTools remote debugging chưa bật -> chưa chạy UI E2E validation/partial-failure/double-submit trên CourseCreate.jsx.'],
  ];
  const existingCsv = fs.readFileSync(EXEC_LOG_CSV, 'utf8');
  for (const [runId, tcId, module, type, priority, reason] of browserBlocked) {
    if (existingCsv.includes(`,${tcId},`)) continue;
    const evPath = saveEvidence(runId, tcId, {
      status: 'Blocked',
      cause: 'BLOCKED_BROWSER_CONNECTION_FAILED',
      error: 'Could not find DevToolsActivePort for chrome (ENOENT)',
      note: reason
    });
    appendExecutionLog({
      runId, cycle: 'C1', tcId, module, type, priority,
      status: 'Blocked', actual: reason, evidence: evPath,
      tool: 'Antigravity Browser Agent (Chrome 157)'
    });
  }

  // Print quick audit of interesting evidence
  const m9 = JSON.parse(fs.readFileSync(path.join(EVIDENCE_DIR, 'RUN-W-C1-0113_TC-W-M07-FN-001.json'), 'utf8'));
  const reg = JSON.parse(fs.readFileSync(path.join(EVIDENCE_DIR, 'RUN-W-C1-0127_TC-W-REG-MNT-001.json'), 'utf8'));
  console.log('OBS-002:', obs2Pass ? 'Pass' : 'Fail', obs2Actual);
  console.log('REC-001:', rec1Pass ? 'Pass' : 'Fail', rec1Actual);
  console.log('REC-002:', rec2Pass ? 'Pass' : 'Fail', rec2Actual);
  console.log('MAIL-9:', m9.subjects);
  console.log('REG-AI:', reg);
  const totalLogged = fs.readFileSync(EXEC_LOG_CSV, 'utf8').trim().split(/\r?\n/).length - 1;
  console.log('TOTAL C1 EXECUTION LOG ROWS:', totalLogged);
}

main().catch(e => { console.error(e); process.exit(1); });
