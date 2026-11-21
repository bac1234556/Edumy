// Cycle C1 Execution Script — Module W-M06: Payout & Withdrawals (9 Test Cases)
import { execSync } from 'node:child_process';
import {
  apiReq, loginWeb, loadState, saveEvidence, appendExecutionLog, QA_PASS, SEED_ADMIN_PASS, SEPAY_API_KEY, CURRENT_CYCLE
} from './lib_qa_client.mjs';

const DB_CONTAINER = 'edumy-test-database-1';
function sqlQuery(sql) {
  const cmd = `docker exec ${DB_CONTAINER} /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "YourPassword123" -d EduMyDb -C -h -1 -W -Q "SET NOCOUNT ON; ${sql.replaceAll('"', '\\"')}"`;
  return execSync(cmd, { encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'] }).trim().split(/\r?\n/)[0].trim();
}

let runSeq = 104;
function nextRun() { return `RUN-W-${CURRENT_CYCLE}-${String(runSeq++).padStart(4, '0')}`; }

const results = [];
function record(tcId, type, priority, status, actual, evidenceObj, defectId = '', note = '') {
  const runId = nextRun();
  const evPath = saveEvidence(runId, tcId, evidenceObj);
  appendExecutionLog({ runId, cycle: CURRENT_CYCLE, tcId, module: 'W-M06', type, priority, status, actual, defectId, evidence: evPath, note });
  results.push({ runId, tcId, status, actual });
  console.log(`[${status}] ${runId} ${tcId}: ${actual.slice(0, 140)}`);
}

async function main() {
  const st = loadState();
  const adm1 = await loginWeb(st.accounts.ADM1, SEED_ADMIN_PASS);
  const gva = await loginWeb(st.accounts.GVA, QA_PASS);
  const gvb = await loginWeb(st.accounts.GVB, QA_PASS);
  const gvco = await loginWeb(st.accounts.GVCO, QA_PASS);
  const hv1 = await loginWeb(st.accounts.HV1, QA_PASS);

  // Note: In M04, HV1 bought cPaid1 (300,000 -> GVA gets 210,000) and cPaidSplit (1,000,000 -> GVA gets 560,000, GVCO gets 140,000).
  // Total GVA earned so far = 770,000 (all pending for 7 hold-days).
  // Let's also create 3 more paid orders for cPaidSplit (1,000,000 each -> +560,000 each = +1,680,000) using temporary buyers so GVA has >= 2,450,000 total!
  const baseGva = Number((await apiReq('GET', '/api/v1/me/earnings/summary', { token: gva.token })).data?.totalEarned || 0);
  const baseCo = Number((await apiReq('GET', '/api/v1/me/earnings/summary', { token: gvco.token })).data?.totalEarned || 0);
  for (let i = 1; i <= 3; i++) {
    const em = `qa.payout.buyer${i}.${Date.now()}@edumy.test`;
    await apiReq('POST', '/api/v1/auth/register', { body: { fullName: `Buyer ${i}`, email: em, password: QA_PASS } });
    const lg = await loginWeb(em, QA_PASS);
    const ord = await apiReq('POST', '/api/v1/orders/buy-now', { token: lg.token, body: { courseId: st.courses.cPaidSplit.courseId } });
    await apiReq('POST', '/api/v1/payments/webhook/sepay', {
      headers: { Authorization: `Apikey ${SEPAY_API_KEY}` },
      body: { id: 910000 + i, gateway: 'BIDV', transferType: 'in', transferAmount: 1000000, content: `EDUMY${ord.data.orderId}`, referenceCode: `FT-PAY-${ord.data.orderId}` }
    });
  }

  // --- TC-W-M06-FN-001: 30% fee + 20% co-instructor split + hold-days=7 maturity ---
  const sumBefore = await apiReq('GET', '/api/v1/me/earnings/summary', { token: gva.token });
  const sumCoBefore = await apiReq('GET', '/api/v1/me/earnings/summary', { token: gvco.token });
  const monthly = await apiReq('GET', '/api/v1/me/earnings/monthly', { token: gva.token });
  const byCourse = await apiReq('GET', '/api/v1/me/earnings/courses', { token: gva.token });
  const entries = await apiReq('GET', '/api/v1/me/earnings/entries', { token: gva.token });

  // Delta for 3 x 1,000,000 orders: platform 30% -> net 700,000 each -> GVA 80% (+1,680,000), GVCO 20% (+420,000)
  const gvaDelta = Number(sumBefore.data?.totalEarned || 0) - baseGva;
  const coDelta = Number(sumCoBefore.data?.totalEarned || 0) - baseCo;
  const splitOk = gvaDelta === 1680000 && coDelta === 420000 && sumBefore.data?.holdDays === 7 && sumBefore.data?.availableBalance === 0 && sumBefore.data?.pendingBalance === sumBefore.data?.totalEarned;

  // Mature GVA's earnings by moving matures_at into the past on test DB
  sqlQuery(`UPDATE dbo.payout_earnings SET matures_at = DATEADD(DAY, -1, SYSUTCDATETIME()) WHERE instructor_id = ${st.userIds.GVA}`);
  const sumAfterMat = await apiReq('GET', '/api/v1/me/earnings/summary', { token: gva.token });
  const matureOk = sumAfterMat.data?.availableBalance === sumBefore.data?.totalEarned && sumAfterMat.data?.pendingBalance === 0;
  const pass001 = splitOk && matureOk && monthly.status === 200 && byCourse.status === 200 && entries.status === 200;
  record('TC-W-M06-FN-001', 'FN', 'High', pass001 ? 'Pass' : 'Fail',
    `3x1M delta: GVA=+${gvaDelta} (exp +1680000), GVCO=+${coDelta} (exp +420000), holdDays=${sumBefore.data?.holdDays}, beforeMatAvail=${sumBefore.data?.availableBalance}, afterMatAvail=${sumAfterMat.data?.availableBalance}`,
    { baseGva, baseCo, gvaDelta, coDelta, sumBefore: sumBefore.data, sumCoBefore: sumCoBefore.data, sumAfterMat: sumAfterMat.data });

  // --- TC-W-M06-FN-002: Payout account requires currentPassword & masks to last 4 digits ---
  const paWrong = await apiReq('PUT', '/api/v1/me/payout-account', {
    token: gva.token,
    body: { bankName: 'Vietcombank', accountNumber: '123456789012', accountHolder: 'QA GVA', currentPassword: 'WrongPassword@1' }
  });
  const paOk = await apiReq('PUT', '/api/v1/me/payout-account', {
    token: gva.token,
    body: { bankName: 'Vietcombank', accountNumber: '123456789012', accountHolder: 'QA GVA', currentPassword: QA_PASS }
  });
  const paGet = await apiReq('GET', '/api/v1/me/payout-account', { token: gva.token });
  const noFullAcct = !paOk.rawText.includes('123456789012') && !paGet.rawText.includes('123456789012');
  const hasLast4 = paGet.rawText.includes('9012');
  const pass002 = paWrong.status === 422 && paOk.status === 200 && paGet.status === 200 && noFullAcct && hasLast4;
  record('TC-W-M06-FN-002', 'FN', 'High', pass002 ? 'Pass' : 'Fail',
    `wrongPwd=${paWrong.status}, saveOk=${paOk.status}, getOk=${paGet.status}, maskedOnlyLast4=${noFullAcct && hasLast4}`,
    { paWrong, paOk: paOk.data, paGet: paGet.data, noFullAcct, hasLast4 });

  // --- TC-W-M06-FN-003: Withdrawal BVA (no account, 999,999, >balance, 1,000,000 ok) ---
  const gvaAvail = Number(sumAfterMat.data?.availableBalance || 0);
  const wdNoAcc = await apiReq('POST', '/api/v1/me/withdrawals', { token: gvb.token, body: { amount: 1000000 } });
  const wdLow = await apiReq('POST', '/api/v1/me/withdrawals', { token: gva.token, body: { amount: 999999 } });
  const wdHigh = await apiReq('POST', '/api/v1/me/withdrawals', { token: gva.token, body: { amount: gvaAvail + 1 } });
  const wdOk = await apiReq('POST', '/api/v1/me/withdrawals', { token: gva.token, body: { amount: 1000000 } });
  const wd1Id = wdOk.data?.id;
  const sumInWd = await apiReq('GET', '/api/v1/me/earnings/summary', { token: gva.token });
  const pass003 = wdNoAcc.status === 422 && wdLow.status === 422 && wdHigh.status === 422 && [200, 201].includes(wdOk.status) && sumInWd.data?.inWithdrawal === 1000000 && sumInWd.data?.availableBalance === (gvaAvail - 1000000);
  record('TC-W-M06-FN-003', 'FN', 'High', pass003 ? 'Pass' : 'Fail',
    `noAcc=${wdNoAcc.status}, belowMin(999999)=${wdLow.status}, overBal=${wdHigh.status}, valid1M=${wdOk.status}, availAfter=${sumInWd.data?.availableBalance}, inWd=${sumInWd.data?.inWithdrawal}`,
    { wdNoAcc, wdLow, wdHigh, wdOk: wdOk.data, sumInWd: sumInWd.data });

  // --- TC-W-M06-FN-004: Second open withdrawal 409 + cancel restores balance & monthly quota ---
  const wdDupOpen = await apiReq('POST', '/api/v1/me/withdrawals', { token: gva.token, body: { amount: 1000000 } });
  const wdCancel = await apiReq('POST', `/api/v1/me/withdrawals/${wd1Id}/cancel`, { token: gva.token });
  const sumAfterCancel = await apiReq('GET', '/api/v1/me/earnings/summary', { token: gva.token });
  const wd2 = await apiReq('POST', '/api/v1/me/withdrawals', { token: gva.token, body: { amount: 1000000 } });
  const wd2Id = wd2.data?.id;
  const pass004 = wdDupOpen.status === 409 && wdCancel.status === 204 && sumAfterCancel.data?.availableBalance === gvaAvail && [200, 201].includes(wd2.status);
  record('TC-W-M06-FN-004', 'FN', 'High', pass004 ? 'Pass' : 'Fail',
    `openDup=${wdDupOpen.status}, cancel=${wdCancel.status}, availAfterCancel=${sumAfterCancel.data?.availableBalance}, recreateAfterCancel=${wd2.status}`,
    { wdDupOpen, wdCancel: wdCancel.status, sumAfterCancel: sumAfterCancel.data, wd2: wd2.data });

  // --- TC-W-M06-FN-005: Admin reject (reason required, restores quota) & Admin pay (transferRef, locks monthly 2nd withdrawal 409) ---
  const rejEmpty = await apiReq('POST', `/api/v1/admin/withdrawals/${wd2Id}/reject`, { token: adm1.token, body: { reason: '' } });
  const rejOk = await apiReq('POST', `/api/v1/admin/withdrawals/${wd2Id}/reject`, { token: adm1.token, body: { reason: 'Sai tên chủ tài khoản' } });
  const wd3 = await apiReq('POST', '/api/v1/me/withdrawals', { token: gva.token, body: { amount: 1000000 } });
  const wd3Id = wd3.data?.id;
  const payOk = await apiReq('POST', `/api/v1/admin/withdrawals/${wd3Id}/pay`, { token: adm1.token, body: { transferReference: 'FT261121QA001' } });
  const cancelPaid = await apiReq('POST', `/api/v1/me/withdrawals/${wd3Id}/cancel`, { token: gva.token });
  const wdSecondMonth = await apiReq('POST', '/api/v1/me/withdrawals', { token: gva.token, body: { amount: 1000000 } });
  const pass005 = rejEmpty.status === 400 && rejOk.status === 200 && [200, 201].includes(wd3.status) && payOk.status === 200 && cancelPaid.status === 409 && wdSecondMonth.status === 409;
  record('TC-W-M06-FN-005', 'FN', 'High', pass005 ? 'Pass' : 'Fail',
    `rejectEmpty=${rejEmpty.status}, rejectOk=${rejOk.status}, recreateAfterReject=${wd3.status}, pay=${payOk.status}, cancelPaid=${cancelPaid.status}, 2ndThisMonth=${wdSecondMonth.status}`,
    { rejEmpty, rejOk: rejOk.data, payOk: payOk.data, cancelPaid, wdSecondMonth });

  // --- TC-W-M06-FN-006: Payout exceptions (invalid filter, missing payout account fields, re-process paid/999999) ---
  const badEntry = await apiReq('GET', '/api/v1/me/earnings/entries?month=invalid', { token: gva.token });
  const badPa = await apiReq('PUT', '/api/v1/me/payout-account', { token: gva.token, body: { bankName: '', accountNumber: '', accountHolder: '', currentPassword: QA_PASS } });
  const rePay = await apiReq('POST', `/api/v1/admin/withdrawals/${wd3Id}/pay`, { token: adm1.token, body: { transferReference: 'FT2' } });
  const pay404 = await apiReq('POST', '/api/v1/admin/withdrawals/999999/pay', { token: adm1.token, body: { transferReference: 'FT3' } });
  const pass006 = badEntry.status !== 500 && badPa.status === 400 && rePay.status === 409 && pay404.status === 404;
  record('TC-W-M06-FN-006', 'FN', 'High', pass006 ? 'Pass' : 'Fail',
    `badMonthFilter=${badEntry.status}, emptyPayoutAcct=${badPa.status}, repayPaid=${rePay.status}, pay999=${pay404.status}`,
    { badEntry: badEntry.status, badPa: badPa.status, rePay, pay404: pay404.status });

  // --- TC-W-M06-REL-001: Concurrent 2 withdrawals race condition (using GVCO with matured balance boosted on test DB) ---
  // Give GVCO a payout account and 2 matured entries (2,500,000) to test concurrent 2x withdrawal cleanly
  await apiReq('PUT', '/api/v1/me/payout-account', {
    token: gvco.token,
    body: { bankName: 'BIDV', accountNumber: '998877665544', accountHolder: 'QA GVCO', currentPassword: QA_PASS }
  });
  sqlQuery(`UPDATE dbo.payout_earnings SET matures_at = DATEADD(DAY, -1, SYSUTCDATETIME()), amount = 1000000 WHERE instructor_id = ${st.userIds.GVCO}`);
  const raceRes = await Promise.all([
    apiReq('POST', '/api/v1/me/withdrawals', { token: gvco.token, body: { amount: 1000000 } }),
    apiReq('POST', '/api/v1/me/withdrawals', { token: gvco.token, body: { amount: 1000000 } }),
  ]);
  const raceOkCnt = raceRes.filter(r => r.status === 200 || r.status === 201).length;
  const raceOpenDb = Number(sqlQuery(`SELECT COUNT(*) FROM dbo.payout_withdrawals WHERE instructor_id=${st.userIds.GVCO} AND status='requested'`));
  const passRel001 = raceOkCnt === 1 && raceOpenDb === 1;
  record('TC-W-M06-REL-001', 'REL', 'High', passRel001 ? 'Pass' : 'Fail',
    `concurrent 2x withdrawal statuses=${JSON.stringify(raceRes.map(r => r.status))}, okCount=${raceOkCnt}, openInDb=${raceOpenDb}`,
    { raceRes: raceRes.map(r => ({ status: r.status, data: r.data })), raceOpenDb });

  // --- TC-W-M06-SEC-001: IDOR withdrawal & payout-account ---
  const gvcoWdId = raceRes.find(r => r.data?.id)?.data?.id;
  const idorP = {};
  idorP.gvbCancelGvcoWd = (await apiReq('POST', `/api/v1/me/withdrawals/${gvcoWdId}/cancel`, { token: gvb.token })).status;
  idorP.gvaSelfApprove = (await apiReq('POST', `/api/v1/admin/withdrawals/${gvcoWdId}/pay`, { token: gva.token, body: { transferReference: 'HACK' } })).status;
  idorP.hv1AdminWd = (await apiReq('GET', '/api/v1/admin/withdrawals', { token: hv1.token })).status;
  idorP.guestEarnings = (await apiReq('GET', '/api/v1/me/earnings/summary')).status;
  const passSec001 = [403, 404].includes(idorP.gvbCancelGvcoWd) && idorP.gvaSelfApprove === 403 && idorP.hv1AdminWd === 403 && idorP.guestEarnings === 401;
  record('TC-W-M06-SEC-001', 'SEC', 'High', passSec001 ? 'Pass' : 'Fail',
    `IDOR payout: ${JSON.stringify(idorP)}`, idorP);

  // --- TC-W-M06-SEC-002: No full bank account or password leak in responses/logs ---
  const admWdList = await apiReq('GET', '/api/v1/admin/withdrawals', { token: adm1.token });
  const logsTail = execSync('docker logs --tail 500 edumy-test-backend-1', { encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'] });
  const leakResp = admWdList.rawText.includes('123456789012') || admWdList.rawText.includes('998877665544');
  const leakLog = logsTail.includes('123456789012') || logsTail.includes('998877665544') || logsTail.includes(QA_PASS);
  const passSec002 = !leakResp && !leakLog;
  record('TC-W-M06-SEC-002', 'SEC', 'High', passSec002 ? 'Pass' : 'Fail',
    `fullBankAcctLeakInAdminResp=${leakResp}, leakInBackendLogs=${leakLog}`,
    { leakResp, leakLog, adminWdCount: admWdList.data?.content?.length ?? admWdList.data?.length });

  const passCnt = results.filter(r => r.status === 'Pass').length;
  const failCnt = results.filter(r => r.status === 'Fail').length;
  console.log(`\n=== W-M06 COMPLETE: ${passCnt} PASS / ${failCnt} FAIL / ${results.length} TOTAL ===`);
}

main().catch(e => { console.error(e); process.exit(1); });
