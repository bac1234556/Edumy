// Phase 3 Smoke Test: 10 High TCs spanning all 8 modules on isolated edumy-test stack
import { apiReq, loginWeb, mailpitList, loadState, saveEvidence, BASE_WEB, BASE_PROM, BASE_GRAFANA, QA_PASS, SEED_ADMIN_PASS } from './lib_qa_client.mjs';

async function main() {
  const st = loadState();
  const checks = [];

  // 1. Backend /actuator/health (W-M07)
  const health = await apiReq('GET', '/actuator/health');
  checks.push({ id: 'SMK-01', module: 'W-M07', name: 'Backend Actuator Health UP', pass: health.status === 200 && health.raw?.status === 'UP', detail: `HTTP ${health.status} status=${health.raw?.status}` });

  // 2. Frontend Nginx serves SPA (W-M07)
  const web = await apiReq('GET', `${BASE_WEB}/`);
  checks.push({ id: 'SMK-02', module: 'W-M07', name: 'Frontend SPA http://localhost:3002', pass: web.status === 200 && String(web.rawText).includes('<div id="root"'), detail: `HTTP ${web.status} len=${web.byteLength}` });

  // 3. Login HV1 + /api/v1/me (W-M01)
  const hv1 = await loginWeb(st.accounts.HV1, QA_PASS);
  const me = await apiReq('GET', '/api/v1/me', { token: hv1.token });
  checks.push({ id: 'SMK-03', module: 'W-M01', name: 'Login HV1 & GET /api/v1/me', pass: hv1.status === 200 && me.status === 200, detail: `login=${hv1.status} me=${me.status}` });

  // 4. Public catalog returns Published courses (W-M02)
  const cat = await apiReq('GET', '/api/courses');
  const items = cat.data?.items || cat.data?.content || [];
  checks.push({ id: 'SMK-04', module: 'W-M02', name: 'Public Catalog GET /api/courses', pass: cat.status === 200 && items.length > 0, detail: `HTTP ${cat.status} items=${items.length}` });

  // 5. Instructor my-courses (W-M03)
  const gva = await loginWeb(st.accounts.GVA, QA_PASS);
  const myC = await apiReq('GET', '/api/courses/my-courses', { token: gva.token });
  checks.push({ id: 'SMK-05', module: 'W-M03', name: 'Instructor GET /api/courses/my-courses', pass: myC.status === 200 && Array.isArray(myC.data) && myC.data.length >= 6, detail: `HTTP ${myC.status} count=${myC.data?.length}` });

  // 6. Cart GET (W-M04)
  const cart = await apiReq('GET', '/api/v1/cart', { token: hv1.token });
  checks.push({ id: 'SMK-06', module: 'W-M04', name: 'Student GET /api/v1/cart', pass: cart.status === 200, detail: `HTTP ${cart.status}` });

  // 7. Learning enrollments GET (W-M05)
  const enr = await apiReq('GET', '/api/courses/enrolled', { token: hv1.token });
  checks.push({ id: 'SMK-07', module: 'W-M05', name: 'Student GET /api/courses/enrolled', pass: enr.status === 200, detail: `HTTP ${enr.status}` });

  // 8. Payout earnings summary (W-M06)
  const earn = await apiReq('GET', '/api/v1/me/earnings/summary', { token: gva.token });
  checks.push({ id: 'SMK-08', module: 'W-M06', name: 'Instructor GET /api/v1/me/earnings/summary', pass: earn.status === 200 && earn.data?.holdDays === 7, detail: `HTTP ${earn.status} holdDays=${earn.data?.holdDays}` });

  // 9. Admin stats (W-M01/M03)
  const adm = await loginWeb(st.accounts.ADM1, SEED_ADMIN_PASS);
  const stats = await apiReq('GET', '/api/v1/admin/stats', { token: adm.token });
  checks.push({ id: 'SMK-09', module: 'W-M01', name: 'Admin GET /api/v1/admin/stats', pass: stats.status === 200, detail: `HTTP ${stats.status} users=${stats.data?.totalUsers}` });

  // 10. Mailpit + Prometheus + Grafana reachable (W-M07)
  const mp = await mailpitList();
  const prom = await apiReq('GET', `${BASE_PROM}/api/v1/targets`);
  const graf = await apiReq('GET', `${BASE_GRAFANA}/api/health`);
  checks.push({ id: 'SMK-10', module: 'W-M07', name: 'Mailpit + Prometheus + Grafana Health', pass: Array.isArray(mp) && prom.status === 200 && graf.status === 200, detail: `mailpitMsgs=${mp.length} prom=${prom.status} grafana=${graf.status}` });

  const passed = checks.filter(c => c.pass).length;
  console.log(`=== SMOKE TEST: ${passed}/${checks.length} PASSED ===`);
  for (const c of checks) {
    console.log(`  [${c.pass ? 'PASS' : 'FAIL'}] ${c.id} (${c.module}) ${c.name} -> ${c.detail}`);
  }
  saveEvidence('RUN-W-C1-0000', 'smoke_test', { passed, total: checks.length, checks });
  if (passed < checks.length) process.exit(1);
}

main().catch(e => { console.error(e); process.exit(1); });
