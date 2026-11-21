// Cycle C1 Execution Script — Module W-M02 (10 Test Cases)
import {
  apiReq, loginWeb, loadState, saveEvidence, appendExecutionLog, QA_PASS, CURRENT_CYCLE
} from './lib_qa_client.mjs';

let runSeq = 27;
function nextRun() { return `RUN-W-${CURRENT_CYCLE}-${String(runSeq++).padStart(4, '0')}`; }

const results = [];
function record(tcId, type, priority, status, actual, evidenceObj, defectId = '', note = '') {
  const runId = nextRun();
  const evPath = saveEvidence(runId, tcId, evidenceObj);
  appendExecutionLog({ runId, cycle: CURRENT_CYCLE, tcId, module: 'W-M02', type, priority, status, actual, defectId, evidence: evPath, note });
  results.push({ runId, tcId, status, actual });
  console.log(`[${status}] ${runId} ${tcId}: ${actual.slice(0, 140)}`);
}

async function main() {
  const st = loadState();
  const hv2 = await loginWeb(st.accounts.HV2, QA_PASS);

  // --- TC-W-M02-FN-001: Public catalog only Published (D-08) & pageSize=20 ---
  const webCat = await apiReq('GET', '/api/courses');
  const mobCat = await apiReq('GET', '/api/v1/catalog/courses');
  const webItems = webCat.data?.items || [];
  const nonPubWeb = webItems.filter(c => (c.status || '').toLowerCase() !== 'published');
  const webPageSize = webCat.data?.pageSize;
  const mobPageSize = mobCat.data?.page?.size;
  const pass001 = webCat.status === 200 && nonPubWeb.length === 0 && webPageSize === 20;
  record('TC-W-M02-FN-001', 'FN', 'High', pass001 ? 'Pass' : 'Fail',
    `webStatus=${webCat.status}, items=${webItems.length}, nonPublishedLeaked=${nonPubWeb.length} (statuses=${[...new Set(webItems.map(i=>i.status))]}), webPageSize=${webPageSize} (SRS yêu cầu 20), v1CatalogPageSize=${mobPageSize}`,
    { webPageSize, mobPageSize, totalItems: webCat.data?.totalItems, nonPubTitles: nonPubWeb.map(c => ({ id: c.courseId, status: c.status, title: c.title })) });

  // --- TC-W-M02-FN-002: Direct access to Hidden/Draft/Pending/NeedsReview by outsider must return 404 ---
  const hidWeb = await apiReq('GET', `/api/courses/${st.courses.cHidden.courseId}`);
  const hidWebHv2 = await apiReq('GET', `/api/courses/${st.courses.cHidden.courseId}`, { token: hv2.token });
  const hidCatalog = await apiReq('GET', `/api/v1/catalog/courses/${st.courses.cHidden.courseId}`);
  const draftWeb = await apiReq('GET', `/api/courses/${st.courses.cDraft.courseId}`);
  const pass002 = hidWeb.status === 404 && hidWebHv2.status === 404 && hidCatalog.status === 404 && draftWeb.status === 404;
  record('TC-W-M02-FN-002', 'FN', 'High', pass002 ? 'Pass' : 'Fail',
    `GET /api/courses/{hidden}: guest=${hidWeb.status}, HV2=${hidWebHv2.status}; /v1/catalog/{hidden}=${hidCatalog.status}; /api/courses/{draft}=${draftWeb.status}`,
    { hidWeb: hidWeb.status, hidWebHv2: hidWebHv2.status, hidCatalog: hidCatalog.status, draftWeb: draftWeb.status });

  // --- TC-W-M02-FN-003: Vietnamese unaccented + case-insensitive search (D-09) ---
  const q1 = await apiReq('GET', `/api/courses?q=${encodeURIComponent('lap trinh java')}`);
  const q2 = await apiReq('GET', `/api/courses?q=${encodeURIComponent('LẬP TRÌNH JAVA')}`);
  const q3 = await apiReq('GET', `/api/courses?q=${encodeURIComponent('lập trình')}`);
  const q4 = await apiReq('GET', `/api/courses?q=${encodeURIComponent('JaVa NaNg CaO')}`);
  const q1v1 = await apiReq('GET', `/api/v1/catalog/courses?q=${encodeURIComponent('lap trinh java')}`);
  const hit = (r, id) => (r.data?.items || r.data?.content || []).some(c => (c.courseId || c.id) === id);
  const pid = st.courses.cPaid1.courseId;
  const pass003 = hit(q1, pid) && hit(q2, pid) && hit(q3, pid) && hit(q4, pid);
  record('TC-W-M02-FN-003', 'FN', 'High', pass003 ? 'Pass' : 'Fail',
    `/api/courses D-09 hits: unaccented=${hit(q1, pid)}, UPPER=${hit(q2, pid)}, accented=${hit(q3, pid)}, mixedUnaccented=${hit(q4, pid)}; /v1/catalog unaccented=${hit(q1v1, pid)}`,
    { q1Count: q1.data?.items?.length, q2Count: q2.data?.items?.length, q3Count: q3.data?.items?.length, q4Count: q4.data?.items?.length, q1v1Count: q1v1.data?.content?.length });

  // --- TC-W-M02-FN-004: Instant search <2 vs >=2 + empty search suggestions ---
  const ins1 = await apiReq('GET', '/api/courses/instant-search?q=a');
  const ins2 = await apiReq('GET', '/api/courses/instant-search?q=ja');
  const emptyWeb = await apiReq('GET', '/api/courses?q=XYZ_KHONG_TON_TAI_999');
  const emptyV1 = await apiReq('GET', '/api/v1/catalog/courses?q=XYZ_KHONG_TON_TAI_999');
  const pass004 = ins1.status === 200 && (ins1.data?.length === 0) && ins2.status === 200 && (ins2.data?.length > 0 && ins2.data?.length <= 6) && emptyWeb.status === 200 && (emptyWeb.data?.items?.length === 0) && (emptyV1.data?.suggestions?.length > 0);
  record('TC-W-M02-FN-004', 'FN', 'Medium', pass004 ? 'Pass' : 'Fail',
    `instant(1char)=${ins1.data?.length}, instant(2char)=${ins2.data?.length}, emptyWebItems=${emptyWeb.data?.items?.length}, emptyV1Suggestions=${emptyV1.data?.suggestions?.length}`,
    { ins1Len: ins1.data?.length, ins2Len: ins2.data?.length, emptyWebHasSuggestions: !!emptyWeb.data?.suggestions, emptyV1SuggestionsLen: emptyV1.data?.suggestions?.length });

  // --- TC-W-M02-FN-005: Pairwise filter (category x FREE/PAID x 4 sorts) ---
  const fFree = await apiReq('GET', `/api/courses?categoryId=${st.categories.activeCatId}&priceType=FREE&sortBy=newest&sortOrder=desc`);
  const fPaidAsc = await apiReq('GET', `/api/courses?priceType=PAID&sortBy=price&sortOrder=asc`);
  const fPaidDesc = await apiReq('GET', `/api/courses?priceType=PAID&sortBy=price&sortOrder=desc`);
  const fRating = await apiReq('GET', `/api/courses?sortBy=rating&sortOrder=desc`);
  const freeOk = (fFree.data?.items || []).every(c => Number(c.price) === 0 && c.categoryId === st.categories.activeCatId);
  const ascPrices = (fPaidAsc.data?.items || []).map(c => Number(c.price));
  const descPrices = (fPaidDesc.data?.items || []).map(c => Number(c.price));
  const ascOk = ascPrices.every((v, i) => i === 0 || ascPrices[i - 1] <= v) && ascPrices.every(v => v > 0);
  const descOk = descPrices.every((v, i) => i === 0 || descPrices[i - 1] >= v) && descPrices.every(v => v > 0);
  const pass005 = fFree.status === 200 && freeOk && ascOk && descOk && fRating.status === 200;
  record('TC-W-M02-FN-005', 'FN', 'High', pass005 ? 'Pass' : 'Fail',
    `freeCatOk=${freeOk} (n=${fFree.data?.items?.length}), priceAscOk=${ascOk}, priceDescOk=${descOk}, ratingSort=${fRating.status}`,
    { freeCount: fFree.data?.items?.length, ascPrices, descPrices, ratingCount: fRating.data?.items?.length });

  // --- TC-W-M02-FN-006: Course detail + Preview lesson creates 0 enrollment + locked lesson blocked ---
  const cDetail = await apiReq('GET', `/api/courses/${pid}`);
  const prevId = st.courses.cPaid1.lessonIds[0]; // isPreview=true
  const lockedId = st.courses.cPaid1.lessonIds[1]; // isPreview=false
  const prevRes = await apiReq('GET', `/api/v1/lessons/${prevId}/access`, { token: hv2.token });
  const enrCheck = await apiReq('GET', `/api/courses/${pid}/enrollment-status`, { token: hv2.token });
  const lockRes = await apiReq('GET', `/api/v1/lessons/${lockedId}/access`, { token: hv2.token });
  const pass006 = cDetail.status === 200 && prevRes.status === 200 && enrCheck.data?.isEnrolled === false && [401, 403].includes(lockRes.status);
  record('TC-W-M02-FN-006', 'FN', 'High', pass006 ? 'Pass' : 'Fail',
    `detail=${cDetail.status}, previewAccess=${prevRes.status}, enrolledAfterPreview=${enrCheck.data?.isEnrolled}, lockedLessonAccess=${lockRes.status}`,
    { detailStatus: cDetail.status, prevStatus: prevRes.status, enrCheck: enrCheck.data, lockRes });

  // --- TC-W-M02-FN-007: Recommendations (/recommend, /similar, /bundle-recommendation) & Tags ---
  const rec = await apiReq('GET', '/api/v1/courses/recommend');
  const sim = await apiReq('GET', `/api/v1/courses/${pid}/similar`);
  const bun = await apiReq('GET', `/api/v1/courses/${pid}/bundle-recommendation`);
  const tags = await apiReq('GET', '/api/tags');
  const pass007 = rec.status === 200 && Array.isArray(rec.data) && rec.data.length <= 4 && sim.status === 200 && bun.status === 200 && tags.status === 200;
  record('TC-W-M02-FN-007', 'FN', 'Medium', pass007 ? 'Pass' : 'Fail',
    `recommend=${rec.status}(n=${rec.data?.length}), similar=${sim.status}, bundle=${bun.status}, tags=${tags.status}(n=${tags.data?.length})`,
    { recLen: rec.data?.length, simStatus: sim.status, bunStatus: bun.status, tagsLen: tags.data?.length });

  // --- TC-W-M02-FN-008: Preview lesson exceptions (non-preview, hidden course lesson, missing 999999) ---
  const prevLockGuest = await apiReq('GET', `/api/v1/catalog/courses/${pid}/lessons/${lockedId}/preview`);
  const prevHid = await apiReq('GET', `/api/v1/catalog/courses/${st.courses.cHidden.courseId}/lessons/${st.courses.cHidden.lessonIds[0]}/preview`);
  const prevMiss = await apiReq('GET', `/api/v1/lessons/999999/access`);
  const pass008 = [401, 403].includes(prevLockGuest.status) && prevHid.status === 404 && prevMiss.status === 404;
  record('TC-W-M02-FN-008', 'FN', 'High', pass008 ? 'Pass' : 'Fail',
    `nonPreviewGuest=${prevLockGuest.status}, hiddenCoursePreview=${prevHid.status}, missingLesson=${prevMiss.status}`,
    { prevLockGuest, prevHid, prevMiss });

  // --- TC-W-M02-FN-009: Recommendations & Tags edge cases ---
  const recNonPub = (rec.data || []).filter(c => (c.status || '').toLowerCase() !== 'published');
  const sim999 = await apiReq('GET', '/api/v1/courses/999999/similar');
  const tagMiss = await apiReq('GET', '/api/tags/search?q=NONEXISTENT999');
  const pass009 = recNonPub.length === 0 && sim999.status !== 500 && tagMiss.status === 200 && (tagMiss.data?.length === 0);
  record('TC-W-M02-FN-009', 'FN', 'Medium', pass009 ? 'Pass' : 'Fail',
    `recNonPub=${recNonPub.length}, similar999=${sim999.status}, tagMiss=${tagMiss.status}(n=${tagMiss.data?.length})`,
    { recNonPub: recNonPub.length, sim999Status: sim999.status, tagMiss: tagMiss.data });

  // --- TC-W-M02-SEC-001: SQL Injection in search, sort, id ---
  const sq1 = await apiReq('GET', `/api/courses?q=${encodeURIComponent("' OR 1=1--")}`);
  const sq2 = await apiReq('GET', `/api/courses?q=${encodeURIComponent("'; DROP TABLE Courses;--")}`);
  const sq3 = await apiReq('GET', `/api/courses?sortBy=${encodeURIComponent("price; WAITFOR DELAY '0:0:5'--")}`);
  const sq4 = await apiReq('GET', `/api/courses/1%20OR%201=1`);
  const noSqlLeak = ![sq1, sq2, sq3, sq4].some(r => /SQLServerException|SQLException|stackTrace/i.test(r.rawText));
  const fastEnough = sq3.elapsedMs < 4000;
  const passSec = noSqlLeak && fastEnough && sq1.status === 200 && sq2.status === 200 && sq3.status === 200 && sq4.status === 400;
  record('TC-W-M02-SEC-001', 'SEC', 'High', passSec ? 'Pass' : 'Fail',
    `SQLi statuses=[${sq1.status},${sq2.status},${sq3.status},${sq4.status}], waitforMs=${sq3.elapsedMs}ms, noSqlLeak=${noSqlLeak}`,
    { sq1: sq1.status, sq2: sq2.status, sq3: { status: sq3.status, ms: sq3.elapsedMs }, sq4: sq4.status, noSqlLeak });

  const passCnt = results.filter(r => r.status === 'Pass').length;
  const failCnt = results.filter(r => r.status === 'Fail').length;
  console.log(`\n=== W-M02 COMPLETE: ${passCnt} PASS / ${failCnt} FAIL / ${results.length} TOTAL ===`);
}

main().catch(e => { console.error(e); process.exit(1); });
