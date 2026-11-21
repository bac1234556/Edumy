// Cycle C1 Execution Script — Module W-M04 (18 Test Cases)
import { execSync } from 'node:child_process';
import {
  apiReq, loginWeb, mailpitClear, mailpitList, mailpitGet,
  loadState, saveEvidence, appendExecutionLog, QA_PASS, SEED_ADMIN_PASS, SEPAY_API_KEY, CURRENT_CYCLE
} from './lib_qa_client.mjs';

const DB_CONTAINER = 'edumy-test-database-1';
function sqlQuery(sql) {
  const cmd = `docker exec ${DB_CONTAINER} /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "YourPassword123" -d EduMyDb -C -h -1 -W -Q "SET NOCOUNT ON; ${sql.replaceAll('"', '\\"')}"`;
  return execSync(cmd, { encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'] }).trim().split(/\r?\n/)[0].trim();
}

let runSeq = 65;
function nextRun() { return `RUN-W-${CURRENT_CYCLE}-${String(runSeq++).padStart(4, '0')}`; }

const results = [];
function record(tcId, type, priority, status, actual, evidenceObj, defectId = '', note = '') {
  const runId = nextRun();
  const evPath = saveEvidence(runId, tcId, evidenceObj);
  appendExecutionLog({ runId, cycle: CURRENT_CYCLE, tcId, module: 'W-M04', type, priority, status, actual, defectId, evidence: evPath, note });
  results.push({ runId, tcId, status, actual });
  console.log(`[${status}] ${runId} ${tcId}: ${actual.slice(0, 140)}`);
}

async function waitMail(email, timeoutMs = 3500) {
  const end = Date.now() + timeoutMs;
  while (Date.now() < end) {
    const msgs = await mailpitList();
    const m = msgs.find(x => (x.To || []).some(t => t.Address?.toLowerCase() === email.toLowerCase()));
    if (m) return await mailpitGet(m.ID);
    await new Promise(r => setTimeout(r, 300));
  }
  return null;
}

async function main() {
  const st = loadState();
  const adm1 = await loginWeb(st.accounts.ADM1, SEED_ADMIN_PASS);
  const gva = await loginWeb(st.accounts.GVA, QA_PASS);
  const hv1 = await loginWeb(st.accounts.HV1, QA_PASS);
  const hv2 = await loginWeb(st.accounts.HV2, QA_PASS);

  const cPaid1 = st.courses.cPaid1.courseId;
  const cPaidSplit = st.courses.cPaidSplit.courseId;
  const cFree3 = st.courses.cFree3.courseId; // HV1 already enrolled in M01

  // --- TC-W-M04-FN-001: Cart add/remove + 4 AC-29 blocks ---
  const addOk = await apiReq('POST', `/api/v1/cart/add/${cPaid1}`, { token: hv1.token });
  const addDup = await apiReq('POST', `/api/v1/cart/add/${cPaid1}`, { token: hv1.token });
  const addOwned = await apiReq('POST', `/api/v1/cart/add/${cFree3}`, { token: hv1.token });
  const addFree = await apiReq('POST', `/api/v1/cart/add/${st.courses.cFree10.courseId}`, { token: hv1.token });
  const addOwnCourse = await apiReq('POST', `/api/v1/cart/add/${cPaid1}`, { token: gva.token });
  const delOk = await apiReq('DELETE', `/api/v1/cart/remove/${cPaid1}`, { token: hv1.token });
  const cartAfter = await apiReq('GET', '/api/v1/cart', { token: hv1.token });
  const pass001 = addOk.status === 200 && [400, 409].includes(addDup.status) && [400, 409, 422].includes(addOwned.status) && [400, 422].includes(addFree.status) && [400, 403, 422].includes(addOwnCourse.status) && delOk.status === 200;
  record('TC-W-M04-FN-001', 'FN', 'High', pass001 ? 'Pass' : 'Fail',
    `addOk=${addOk.status}, dup=${addDup.status}, owned=${addOwned.status}, free=${addFree.status}, ownCourseGVA=${addOwnCourse.status}, remove=${delOk.status}`,
    { addOk, addDup, addOwned, addFree, addOwnCourse, delOk, cartAfter });

  // --- TC-W-M04-FN-002: Coupon validate valid/expired/inactive/missing + apply checkout ---
  const cpOk = await apiReq('POST', '/api/coupons/validate', { body: { code: st.coupons.valid } });
  const cpExp = await apiReq('POST', '/api/coupons/validate', { body: { code: st.coupons.expired } });
  const cpInact = await apiReq('POST', '/api/coupons/validate', { body: { code: st.coupons.inactive } });
  const cpMiss = await apiReq('POST', '/api/coupons/validate', { body: { code: 'KHONGTONTAI999' } });
  await apiReq('POST', `/api/v1/cart/add/${cPaid1}`, { token: hv2.token });
  const coWithCpn = await apiReq('POST', '/api/v1/orders/checkout', { token: hv2.token, body: { couponCode: st.coupons.valid } });
  const discounted = Number(coWithCpn.data?.totalAmount);
  const expectedDisc = 300000 * 0.8; // 20% off
  if (coWithCpn.data?.orderId) await apiReq('POST', `/api/v1/orders/${coWithCpn.data.orderId}/cancel`, { token: hv2.token });
  const pass002 = cpOk.status === 200 && [400, 404].includes(cpExp.status) && [400, 404].includes(cpInact.status) && [400, 404].includes(cpMiss.status) && coWithCpn.status === 200 && discounted === expectedDisc;
  record('TC-W-M04-FN-002', 'FN', 'Medium', pass002 ? 'Pass' : 'Fail',
    `valid=${cpOk.status}, expired=${cpExp.status}, inactive=${cpInact.status}, missing=${cpMiss.status}, checkoutWith20%=${coWithCpn.status} total=${discounted} (exp=${expectedDisc})`,
    { cpOk, cpExp, cpInact, cpMiss, coWithCpn });

  // --- TC-W-M04-FN-003: Order price snapshot + BR-29 max 1 pending order replaces old ---
  const o1Res = await apiReq('POST', '/api/v1/orders/buy-now', { token: hv2.token, body: { courseId: cPaid1 } });
  const o1Id = o1Res.data?.orderId;
  // GV-A changes cPaid1 price from 300k to 450k
  const cObj = (await apiReq('GET', `/api/courses/${cPaid1}`, { token: gva.token })).data;
  cObj.price = 450000;
  await apiReq('PUT', `/api/courses/${cPaid1}`, { token: gva.token, body: cObj });
  const o1Check = await apiReq('GET', `/api/v1/orders/${o1Id}`, { token: hv2.token });
  // Restore price to 300k
  cObj.price = 300000;
  await apiReq('PUT', `/api/courses/${cPaid1}`, { token: gva.token, body: cObj });
  // HV2 creates second pending order O2 -> check BR-29 max 1 pending
  const o2Res = await apiReq('POST', '/api/v1/orders/buy-now', { token: hv2.token, body: { courseId: cPaidSplit } });
  const hv2Orders = await apiReq('GET', '/api/v1/orders', { token: hv2.token });
  const pendingOrders = (hv2Orders.data || []).filter(o => o.status === 'PENDING_PAYMENT' || o.status === 'PENDING');
  const priceUnchanged = Number(o1Check.data?.totalAmount) === 300000;
  const pass003 = o1Res.status === 200 && priceUnchanged && o2Res.status === 200 && pendingOrders.length <= 1;
  record('TC-W-M04-FN-003', 'FN', 'High', pass003 ? 'Pass' : 'Fail',
    `o1TotalAfterCoursePriceChange=${o1Check.data?.totalAmount} (snapshotOk=${priceUnchanged}), pendingCountAfterO2=${pendingOrders.length} (BR-29 yêu cầu <= 1)`,
    { o1Id, o1Total: o1Check.data?.totalAmount, o2Id: o2Res.data?.orderId, pendingCount: pendingOrders.length, pendingIds: pendingOrders.map(x => x.orderId) });

  // --- TC-W-M04-FN-004: Order history, cancel pending, block cancel paid, empty cart checkout ---
  const cancelO2 = await apiReq('POST', `/api/v1/orders/${o2Res.data?.orderId}/cancel`, { token: hv2.token });
  const emptyCheckout = await apiReq('POST', '/api/v1/orders/checkout', { token: hv2.token, body: {} });
  // Create & pay an order for HV1 (also needed for TC-005 & TC-006)
  const oHv1Res = await apiReq('POST', '/api/v1/orders/buy-now', { token: hv1.token, body: { courseId: cPaid1 } });
  const oHv1Id = oHv1Res.data?.orderId;

  // --- TC-W-M04-FN-005: VietQR + VNPay URL + Polling status ---
  const qrRes = await apiReq('POST', '/api/v1/payments/create-qr', { token: hv1.token, body: { orderId: oHv1Id } });
  const vqrGet = await apiReq('GET', `/api/v1/payments/vietqr/${oHv1Id}`, { token: hv1.token });
  const vnpUrl = await apiReq('POST', '/api/v1/payments/create-url', { token: hv1.token, body: { orderId: oHv1Id, amount: 300000 } });
  const stPoll = await apiReq('GET', `/api/v1/orders/${oHv1Id}/status`);
  const pass005 = qrRes.status === 200 && vqrGet.status === 200 && vnpUrl.status === 200 && String(vnpUrl.data?.paymentUrl || '').includes('vnp_SecureHash') && stPoll.status === 200;
  record('TC-W-M04-FN-005', 'FN', 'High', pass005 ? 'Pass' : 'Fail',
    `createQR=${qrRes.status}, getVietQR=${vqrGet.status}, vnpayUrl=${vnpUrl.status}, statusPoll=${stPoll.status}(${stPoll.data?.status})`,
    { qrRes, vnpUrlStatus: vnpUrl.status, hasHash: String(vnpUrl.data?.paymentUrl || '').includes('vnp_SecureHash'), stPoll });

  // --- TC-W-M04-FN-006: Valid SePay webhook -> PAID + Enrollment + Earning + Email ---
  await mailpitClear();
  const whOk = await apiReq('POST', '/api/v1/payments/webhook/sepay', {
    headers: { Authorization: `Apikey ${SEPAY_API_KEY}` },
    body: { id: 900001, gateway: 'BIDV', transferType: 'in', transferAmount: 300000, content: `EDUMY${oHv1Id}`, referenceCode: `FT-QA-${oHv1Id}` }
  });
  const stAfterPay = await apiReq('GET', `/api/v1/orders/${oHv1Id}/status`);
  const enrHv1 = await apiReq('GET', `/api/courses/${cPaid1}/enrollment-status`, { token: hv1.token });
  const buyMail = await waitMail(st.accounts.HV1);
  const earnCnt = sqlQuery(`SELECT COUNT(*) FROM dbo.payout_earnings WHERE order_id=${oHv1Id}`);
  const pass006 = whOk.status === 200 && whOk.raw?.success === true && enrHv1.data?.isEnrolled === true && Number(earnCnt) >= 1 && !!buyMail;
  record('TC-W-M04-FN-006', 'FN', 'High', pass006 ? 'Pass' : 'Fail',
    `sepayWebhook=${whOk.status}(success=${whOk.raw?.success}), orderStatus=${stAfterPay.data?.status}, enrolled=${enrHv1.data?.isEnrolled}, earningRows=${earnCnt}, mail=${!!buyMail}`,
    { whOk, stAfterPay, enrHv1, earnCnt, mailSubject: buyMail?.Subject });

  // Finish TC-W-M04-FN-004 now that oHv1Id is PAID
  const cancelPaid = await apiReq('POST', `/api/v1/orders/${oHv1Id}/cancel`, { token: hv1.token });
  const pass004 = cancelO2.status === 200 && cancelPaid.status === 409 && emptyCheckout.status === 400;
  record('TC-W-M04-FN-004', 'FN', 'High', pass004 ? 'Pass' : 'Fail',
    `cancelPending=${cancelO2.status}, cancelPaid=${cancelPaid.status}, emptyCartCheckout=${emptyCheckout.status}`,
    { cancelO2, cancelPaid, emptyCheckout });

  // --- TC-W-M04-FN-007: Invalid/missing webhook signature (SePay + VNPay IPN) rejected ---
  // Use a dedicated temp student so if missing-auth webhook bypasses and enrolls, HV2 stays cleanly unenrolled!
  const sigStuEmail = `qa.sigtest.${Date.now()}@edumy.test`;
  await apiReq('POST', '/api/v1/auth/register', { body: { fullName: 'Sig Test', email: sigStuEmail, password: QA_PASS } });
  const sigStu = await loginWeb(sigStuEmail, QA_PASS);
  const oSigRes = await apiReq('POST', '/api/v1/orders/buy-now', { token: sigStu.token, body: { courseId: cPaid1 } });
  const oSigId = oSigRes.data?.orderId;
  const whNoAuth = await apiReq('POST', '/api/v1/payments/webhook/sepay', {
    body: { id: 900002, transferType: 'in', transferAmount: 300000, content: `EDUMY${oSigId}` }
  });
  const oSig2Res = await apiReq('POST', '/api/v1/orders/buy-now', { token: hv2.token, body: { courseId: cPaid1 } });
  const oSig2Id = oSig2Res.data?.orderId;
  const whBadAuth = await apiReq('POST', '/api/v1/payments/webhook/sepay', {
    headers: { Authorization: 'Apikey WRONG_KEY_999' },
    body: { id: 900003, transferType: 'in', transferAmount: 300000, content: `EDUMY${oSig2Id}` }
  });
  const vnpBad = await apiReq('GET', `/api/v1/payments/vnpay-ipn?vnp_TxnRef=${oSig2Id}&vnp_Amount=30000000&vnp_ResponseCode=00&vnp_SecureHash=BADHASH`);
  const enrSigCheck = await apiReq('GET', `/api/courses/${cPaid1}/enrollment-status`, { token: sigStu.token });
  const pass007 = whNoAuth.status === 401 && whBadAuth.status === 401 && vnpBad.raw?.RspCode === '97' && enrSigCheck.data?.isEnrolled === false;
  record('TC-W-M04-FN-007', 'FN', 'High', pass007 ? 'Pass' : 'Fail',
    `sepayNoAuth=${whNoAuth.status}(sigStuEnrolled=${enrSigCheck.data?.isEnrolled}), sepayBadAuth=${whBadAuth.status}, vnpBadHash=${vnpBad.raw?.RspCode}`,
    { whNoAuth, whBadAuth, vnpBad, enrSigCheck });

  // --- TC-W-M04-FN-008: Wrong amount & late webhook on cancelled order -> needs_review, no auto-enroll ---
  const whUnder = await apiReq('POST', '/api/v1/payments/webhook/sepay', {
    headers: { Authorization: `Apikey ${SEPAY_API_KEY}` },
    body: { id: 900004, gateway: 'BIDV', transferType: 'in', transferAmount: 150000, content: `EDUMY${oSig2Id}` }
  });
  await apiReq('POST', `/api/v1/orders/${oSig2Id}/cancel`, { token: hv2.token });
  const whLate = await apiReq('POST', '/api/v1/payments/webhook/sepay', {
    headers: { Authorization: `Apikey ${SEPAY_API_KEY}` },
    body: { id: 900005, gateway: 'BIDV', transferType: 'in', transferAmount: 300000, content: `EDUMY${oSig2Id}` }
  });
  const enrAfterLate = await apiReq('GET', `/api/courses/${cPaid1}/enrollment-status`, { token: hv2.token });
  const admPayRec = await apiReq('GET', '/api/v1/admin/payments?needsReconciliation=true', { token: adm1.token });
  const markedReconcile = (admPayRec.data || []).some(t => t.orderId === oSig2Id);
  const pass008 = enrAfterLate.data?.isEnrolled === false && markedReconcile;
  record('TC-W-M04-FN-008', 'FN', 'High', pass008 ? 'Pass' : 'Fail',
    `underpaid=${whUnder.status}(${whUnder.raw?.message}), lateOnCancelled=${whLate.status}(${whLate.raw?.message}), hv2Enrolled=${enrAfterLate.data?.isEnrolled}, markedNeedsReconcile=${markedReconcile}`,
    { whUnder, whLate, enrAfterLate, markedReconcile });

  // --- TC-W-M04-FN-009: Admin orders, payments & manual reconciliation UC-A18 ---
  // Put oSigId into NEEDS_REVIEW via underpaid webhook, then test UC-A18: missing note -> 400, valid ACTIVATE -> 200 + enroll + email, 2nd reconcile -> 409!
  await mailpitClear();
  await apiReq('POST', '/api/v1/payments/webhook/sepay', {
    headers: { Authorization: `Apikey ${SEPAY_API_KEY}` },
    body: { id: 900009, gateway: 'BIDV', transferType: 'in', transferAmount: 100000, content: `EDUMY${oSigId}` }
  });
  const admOrders = await apiReq('GET', '/api/v1/admin/orders', { token: adm1.token });
  const admPays = await apiReq('GET', '/api/v1/admin/payments', { token: adm1.token });
  const reconNoNote = await apiReq('POST', `/api/v1/admin/payments/${oSigId}/reconcile`, { token: adm1.token, body: { action: 'ACTIVATE', note: '   ' } });
  const reconTry = await apiReq('POST', `/api/v1/admin/payments/${oSigId}/reconcile`, { token: adm1.token, body: { action: 'ACTIVATE', note: 'Đã đối soát sao kê ngân hàng' } });
  const reconDup = await apiReq('POST', `/api/v1/admin/payments/${oSigId}/reconcile`, { token: adm1.token, body: { action: 'REFUND_EXTERNAL', note: 'Thử đối soát lại lần 2' } });
  const enrRecon = await apiReq('GET', `/api/courses/${cPaid1}/enrollment-status`, { token: sigStu.token });
  const reconMail = await waitMail(sigStuEmail);
  const pass009 = admOrders.status === 200 && admPays.status === 200 && reconNoNote.status === 400 && reconTry.status === 200 && reconDup.status === 409 && enrRecon.data?.isEnrolled === true && !!reconMail;
  record('TC-W-M04-FN-009', 'FN', 'High', pass009 ? 'Pass' : 'Fail',
    `adminOrders=${admOrders.status}(n=${admOrders.data?.length}), adminPayments=${admPays.status}, reconNoNote=${reconNoNote.status}, reconActivate=${reconTry.status}(enrolled=${enrRecon.data?.isEnrolled},mail=${!!reconMail}), reconAgain=${reconDup.status}`,
    { admOrdersLen: admOrders.data?.length, admPaysLen: admPays.data?.length, reconNoNote, reconTry, reconDup, enrRecon, mailSubject: reconMail?.Subject });

  // --- TC-W-M04-FN-010: QR expiration check ---
  const qrHasExpire = 'expiresAt' in (qrRes.data || {}) || 'expiredAt' in (qrRes.data || {}) || 'expireSeconds' in (qrRes.data || {});
  record('TC-W-M04-FN-010', 'FN', 'High', qrHasExpire ? 'Pass' : 'Fail',
    `create-qr response fields=${JSON.stringify(Object.keys(qrRes.data || {}))}, hasExpirationField=${qrHasExpire}`,
    { qrData: qrRes.data, qrHasExpire });

  // --- TC-W-M04-FN-011: Cart exceptions (999999, Draft, Hidden, guest 401) ---
  const cGuest = await apiReq('GET', '/api/v1/cart');
  const cAdd999 = await apiReq('POST', '/api/v1/cart/add/999999', { token: hv2.token });
  const cAddDraft = await apiReq('POST', `/api/v1/cart/add/${st.courses.cDraft.courseId}`, { token: hv2.token });
  const cAddHid = await apiReq('POST', `/api/v1/cart/add/${st.courses.cHidden.courseId}`, { token: hv2.token });
  const cDel999 = await apiReq('DELETE', '/api/v1/cart/items/999999', { token: hv2.token });
  const pass011 = cGuest.status === 401 && cAdd999.status === 404 && [400, 404].includes(cAddDraft.status) && [400, 404].includes(cAddHid.status) && cDel999.status !== 500;
  record('TC-W-M04-FN-011', 'FN', 'High', pass011 ? 'Pass' : 'Fail',
    `guest=${cGuest.status}, add999=${cAdd999.status}, addDraft=${cAddDraft.status}, addHidden=${cAddHid.status}, delItem999=${cDel999.status}`,
    { cGuest: cGuest.status, cAdd999: cAdd999.status, cAddDraft: cAddDraft.status, cAddHid: cAddHid.status, cDel999: cDel999.status });

  // --- TC-W-M04-FN-012: Admin Coupon CRUD & boundaries ---
  const cpnDup = await apiReq('POST', '/api/coupons', { token: adm1.token, body: { code: st.coupons.valid, discountPercentage: 10, expiryDate: '2028-01-01T00:00:00', isActive: true } });
  const cpnZero = await apiReq('POST', '/api/coupons', { token: adm1.token, body: { code: 'QAZERO0', discountPercentage: 0, expiryDate: '2028-01-01T00:00:00', isActive: true } });
  const cpn101 = await apiReq('POST', '/api/coupons', { token: adm1.token, body: { code: 'QAOVER101', discountPercentage: 101, expiryDate: '2028-01-01T00:00:00', isActive: true } });
  const cpnStu = await apiReq('POST', '/api/coupons', { token: hv1.token, body: { code: 'STUHACK', discountPercentage: 50, expiryDate: '2028-01-01T00:00:00', isActive: true } });
  const pass012 = [400, 409].includes(cpnDup.status) && cpnZero.status === 400 && cpn101.status === 400 && cpnStu.status === 403;
  record('TC-W-M04-FN-012', 'FN', 'Medium', pass012 ? 'Pass' : 'Fail',
    `dupCode=${cpnDup.status}, pct0=${cpnZero.status}, pct101=${cpn101.status}, studentCreate=${cpnStu.status}`,
    { cpnDup: cpnDup.status, cpnZero: cpnZero.status, cpn101: cpn101.status, cpnStu: cpnStu.status });

  // --- TC-W-M04-FN-013: Admin payments filter & 404 reconcile ---
  const pTrue = await apiReq('GET', '/api/v1/admin/payments?needsReconciliation=true', { token: adm1.token });
  const pFalse = await apiReq('GET', '/api/v1/admin/payments?needsReconciliation=false', { token: adm1.token });
  const rec404 = await apiReq('POST', '/api/v1/admin/payments/999999/reconcile', { token: adm1.token, body: { action: 'ACTIVATE', note: 'test' } });
  const pass013 = pTrue.status === 200 && pFalse.status === 200 && rec404.status === 404;
  record('TC-W-M04-FN-013', 'FN', 'High', pass013 ? 'Pass' : 'Fail',
    `filterTrue=${pTrue.status}(n=${pTrue.data?.length}), filterFalse=${pFalse.status}(n=${pFalse.data?.length}), reconcile999=${rec404.status}`,
    { pTrueLen: pTrue.data?.length, pFalseLen: pFalse.data?.length, rec404: rec404.status });

  // --- TC-W-M04-REL-001: Webhook Idempotency 10x (5 concurrent + 5 sequential) ---
  await mailpitClear();
  const oIdemRes = await apiReq('POST', '/api/v1/orders/buy-now', { token: hv1.token, body: { courseId: cPaidSplit } });
  const oIdemId = oIdemRes.data?.orderId;
  const payload10 = { id: 900100, gateway: 'BIDV', transferType: 'in', transferAmount: 1000000, content: `EDUMY${oIdemId}`, referenceCode: `FT-IDEM-${oIdemId}` };
  const batch1 = await Promise.all(Array.from({ length: 5 }, () =>
    apiReq('POST', '/api/v1/payments/webhook/sepay', { headers: { Authorization: `Apikey ${SEPAY_API_KEY}` }, body: payload10 })
  ));
  const batch2 = [];
  for (let i = 0; i < 5; i++) {
    batch2.push(await apiReq('POST', '/api/v1/payments/webhook/sepay', { headers: { Authorization: `Apikey ${SEPAY_API_KEY}` }, body: payload10 }));
  }
  await new Promise(r => setTimeout(r, 1200));
  const enrCnt10 = Number(sqlQuery(`SELECT COUNT(*) FROM dbo.enrollments WHERE user_id=${st.userIds.HV1} AND course_id=${cPaidSplit}`));
  const earnSum10 = Number(sqlQuery(`SELECT ISNULL(SUM(amount),0) FROM dbo.payout_earnings WHERE order_id=${oIdemId}`));
  const earnRows10 = Number(sqlQuery(`SELECT COUNT(*) FROM dbo.payout_earnings WHERE order_id=${oIdemId}`));
  const mails10 = (await mailpitList()).filter(m => (m.To || []).some(t => t.Address?.toLowerCase() === st.accounts.HV1.toLowerCase()));
  // Expected: 1 enrollment, net earning = 700,000 (split into 2 rows: GVA 560k + GVCO 140k = 700k total), 1 email
  const any500 = [...batch1, ...batch2].some(r => r.status >= 500);
  const passRel001 = !any500 && enrCnt10 === 1 && earnSum10 === 700000 && earnRows10 === 2 && mails10.length === 1;
  record('TC-W-M04-REL-001', 'REL', 'High', passRel001 ? 'Pass' : 'Fail',
    `10x webhook: any500=${any500}, enrollments=${enrCnt10} (exp 1), earningRows=${earnRows10} sum=${earnSum10} (exp 2 rows = 700000), emails=${mails10.length} (exp 1)`,
    { statuses: [...batch1, ...batch2].map(r => r.status), enrCnt10, earnRows10, earnSum10, emailsCount: mails10.length });

  // --- TC-W-M04-REL-002: Double-click checkout concurrency & consistency check ---
  await apiReq('POST', `/api/v1/cart/add/${cPaidSplit}`, { token: hv2.token });
  const dblRes = await Promise.all([
    apiReq('POST', '/api/v1/orders/checkout', { token: hv2.token, body: {} }),
    apiReq('POST', '/api/v1/orders/checkout', { token: hv2.token, body: {} }),
  ]);
  const dblOkCount = dblRes.filter(r => r.status === 200).length;
  const orphanPaid = Number(sqlQuery(`SELECT COUNT(*) FROM dbo.orders o WHERE o.order_id >= ${oHv1Id} AND o.status IN ('PAID','Completed') AND NOT EXISTS (SELECT 1 FROM dbo.enrollments e WHERE e.user_id=o.user_id)`));
  const passRel002 = dblOkCount === 1 && orphanPaid === 0;
  record('TC-W-M04-REL-002', 'REL', 'High', passRel002 ? 'Pass' : 'Fail',
    `doubleClickCheckout statuses=${JSON.stringify(dblRes.map(r => r.status))} (200 count=${dblOkCount}, exp 1), orphanPaidOrders=${orphanPaid}`,
    { dblRes: dblRes.map(r => ({ status: r.status, data: r.data })), orphanPaid });

  // --- TC-W-M04-REL-003: @Scheduled auto-expire QR / pending order (Suspicious Point 3.2) ---
  // Create a pending order, backdate CreatedAt by 48 hours in DB, wait 7s for 5s scheduler tick, check status
  const oExp = await apiReq('POST', '/api/v1/orders/buy-now', { token: hv2.token, body: { courseId: cPaid1 } });
  const oExpId = oExp.data?.orderId;
  sqlQuery(`UPDATE dbo.orders SET created_at = DATEADD(HOUR, -48, SYSUTCDATETIME()) WHERE order_id=${oExpId}`);
  await new Promise(r => setTimeout(r, 7000));
  const oExpStatus = await apiReq('GET', `/api/v1/orders/${oExpId}/status`);
  const hasScheduled = execSync(`powershell -Command "(Get-ChildItem -Recurse Edumy-main/Edumy/Backend/src/main/java -Filter *.java | Select-String '@Scheduled|@EnableScheduling').Count"`, { encoding: 'utf8' }).trim();
  const isExpired = /EXPIRED|CANCELLED/i.test(oExpStatus.data?.status || '');
  record('TC-W-M04-REL-003', 'REL', 'High', (Number(hasScheduled) > 0 && isExpired) ? 'Pass' : 'Fail',
    `@Scheduled occurrences in codebase=${hasScheduled}; 48h-old pending order status=${oExpStatus.data?.status} (NFR-09 yêu cầu tự hủy/đóng QR quá hạn)`,
    { scheduledCount: Number(hasScheduled), oExpId, oExpStatus: oExpStatus.data });

  // --- TC-W-M04-SEC-001: IDOR Order & Payment between HV1 and HV2 ---
  const idorO = {};
  idorO.hv2GetHv1Order = (await apiReq('GET', `/api/v1/orders/${oHv1Id}`, { token: hv2.token })).status;
  idorO.hv2CancelHv1Order = (await apiReq('POST', `/api/v1/orders/${oHv1Id}/cancel`, { token: hv2.token })).status;
  idorO.hv2CreateQrHv1 = (await apiReq('POST', '/api/v1/payments/create-qr', { token: hv2.token, body: { orderId: oHv1Id } })).status;
  idorO.hv2GetVietQrHv1 = (await apiReq('GET', `/api/v1/payments/vietqr/${oHv1Id}`, { token: hv2.token })).status;
  idorO.guestGetOrder = (await apiReq('GET', `/api/v1/orders/${oHv1Id}`)).status;
  const passSec001 = idorO.hv2GetHv1Order === 403 && idorO.hv2CancelHv1Order === 403 && idorO.hv2CreateQrHv1 === 403 && idorO.hv2GetVietQrHv1 === 403 && idorO.guestGetOrder === 401;
  record('TC-W-M04-SEC-001', 'SEC', 'High', passSec001 ? 'Pass' : 'Fail',
    `IDOR order/payment: ${JSON.stringify(idorO)}`, idorO);

  // --- TC-W-M04-SEC-002: Client price tampering + unsigned /api/payment/callback bypass (OWASP A04/A08) ---
  const oTamp = await apiReq('POST', '/api/v1/orders/buy-now', {
    token: hv2.token,
    body: { courseId: cPaid1, price: 1000, totalAmount: 0, discount: 100 }
  });
  const tampTotal = Number(oTamp.data?.totalAmount);
  const oTampId = oTamp.data?.orderId;
  // Attempt unsigned /api/payment/callback bypass!
  const cbBypass = await apiReq('POST', '/api/payment/callback', {
    body: { orderId: oTampId, success: true }
  });
  const enrBypass = await apiReq('GET', `/api/courses/${cPaid1}/enrollment-status`, { token: hv2.token });
  const passSec002 = tampTotal === 300000 && enrBypass.data?.isEnrolled === false && cbBypass.raw?.rspCode !== '00';
  record('TC-W-M04-SEC-002', 'SEC', 'High', passSec002 ? 'Pass' : 'Fail',
    `clientPriceTamperTotal=${tampTotal} (exp 300000); unsigned /api/payment/callback=${cbBypass.status}(rsp=${cbBypass.raw?.rspCode}), hv2EnrolledAfterUnsignedCallback=${enrBypass.data?.isEnrolled}`,
    { tampTotal, cbBypass, enrBypass });

  const passCnt = results.filter(r => r.status === 'Pass').length;
  const failCnt = results.filter(r => r.status === 'Fail').length;
  console.log(`\n=== W-M04 COMPLETE: ${passCnt} PASS / ${failCnt} FAIL / ${results.length} TOTAL ===`);
}

main().catch(e => { console.error(e); process.exit(1); });
