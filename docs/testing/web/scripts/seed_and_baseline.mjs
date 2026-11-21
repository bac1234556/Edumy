// Phase 3 Test Data Seeder for isolated edumy-test stack
// Creates 10 standardized accounts, >= 4 categories, courses in all 5 statuses,
// free + paid courses, 3-lesson & 10-lesson courses, quiz, coupons.
import { apiReq, loginWeb, mailpitClear, mailpitList, mailpitGet, QA_PASS, SEED_ADMIN_PASS, saveState, saveEvidence } from './lib_qa_client.mjs';

async function registerUser(fullName, email) {
  const res = await apiReq('POST', '/api/v1/auth/register', {
    body: { fullName, email, password: QA_PASS }
  });
  return res;
}

async function verifyFromMailpit(email) {
  await new Promise(r => setTimeout(r, 800));
  const msgs = await mailpitList();
  const hit = msgs.find(m => (m.To || []).some(t => t.Address?.toLowerCase() === email.toLowerCase()));
  if (!hit) return { found: false };
  const detail = await mailpitGet(hit.ID);
  const text = (detail.HTML || '') + '\n' + (detail.Text || '');
  const m = text.match(/token=([A-Za-z0-9\-_]+)/) || text.match(/code=([A-Za-z0-9\-_]+)/);
  if (!m) return { found: true, tokenFound: false };
  const v = await apiReq('POST', '/api/v1/auth/verify-email', { body: { token: m[1] } });
  return { found: true, tokenFound: true, verifyStatus: v.status };
}

async function main() {
  console.log('=== PHASE 3: SEEDING EDUMY-TEST ENVIRONMENT ===');
  await mailpitClear();

  // 1. Login default seeded admin
  const adm1Login = await loginWeb('admin@edumy.com', SEED_ADMIN_PASS);
  if (!adm1Login.token) {
    throw new Error(`Failed to login seed admin@edumy.com: status=${adm1Login.status} body=${adm1Login.rawText}`);
  }
  const admToken = adm1Login.token;

  // 2. Register remaining accounts (all use QA_PASS in memory only)
  const accounts = {
    ADM1: 'admin@edumy.com',
    ADM2: 'qa.adm2@edumy.test',
    GVA: 'qa.gva@edumy.test',
    GVB: 'qa.gvb@edumy.test',
    GVCO: 'qa.gvco@edumy.test',
    GVREV: 'qa.gvrevoked@edumy.test',
    HV1: 'qa.hv1@edumy.test',
    HV2: 'qa.hv2@edumy.test',
    HVLOCK: 'qa.hvlocked@edumy.test',
    HVUNV: 'qa.hvunverified@edumy.test',
  };

  const createdInfo = {};
  for (const [alias, email] of Object.entries(accounts)) {
    if (alias === 'ADM1') continue;
    const reg = await registerUser(`QA ${alias} Tiếng Việt`, email);
    createdInfo[alias] = { email, registerStatus: reg.status };
    if (alias !== 'HVUNV') {
      createdInfo[alias].verify = await verifyFromMailpit(email);
    }
  }

  // Fetch all users via Admin to map IDs
  const allUsersRes = await apiReq('GET', '/api/v1/admin/users', { token: admToken });
  const usersList = Array.isArray(allUsersRes.data) ? allUsersRes.data : [];
  const idByEmail = {};
  for (const u of usersList) {
    idByEmail[(u.email || '').toLowerCase()] = u.userId || u.id;
  }

  const userIds = {};
  for (const [alias, email] of Object.entries(accounts)) {
    userIds[alias] = idByEmail[email.toLowerCase()];
  }
  console.log('User IDs:', userIds);

  // Assign roles & states
  await apiReq('PUT', `/api/v1/admin/users/${userIds.ADM2}/role`, { token: admToken, rawBody: '"Admin"' });
  await apiReq('PUT', `/api/v1/admin/users/${userIds.GVA}/roles/INSTRUCTOR`, { token: admToken });
  await apiReq('PUT', `/api/v1/admin/users/${userIds.GVB}/roles/INSTRUCTOR`, { token: admToken });
  await apiReq('PUT', `/api/v1/admin/users/${userIds.GVCO}/roles/INSTRUCTOR`, { token: admToken });
  // Grant then revoke GVREV
  await apiReq('PUT', `/api/v1/admin/users/${userIds.GVREV}/roles/INSTRUCTOR`, { token: admToken });
  await apiReq('DELETE', `/api/v1/admin/users/${userIds.GVREV}/roles/INSTRUCTOR`, { token: admToken });
  // Lock HVLOCK
  await apiReq('POST', `/api/v1/admin/users/${userIds.HVLOCK}/lock`, { token: admToken });

  // Login GV-A & HV1 to seed courses
  const gvaLogin = await loginWeb(accounts.GVA, QA_PASS);
  const gvaToken = gvaLogin.token;
  if (!gvaToken) throw new Error('Failed login GVA');

  // 3. Ensure >= 4 categories (including 1 empty category for D-21 delete test)
  const catsRes = await apiReq('GET', '/api/v1/admin/categories/stats', { token: admToken });
  const cats = Array.isArray(catsRes.data) ? catsRes.data : [];
  const activeCatId = cats[0]?.categoryId || cats[0]?.id || 1;
  const secondCatId = cats[1]?.categoryId || cats[1]?.id || activeCatId;

  const emptyCatRes = await apiReq('POST', '/api/v1/admin/categories', {
    token: admToken,
    body: { name: 'QA Danh Mục Trống D21', description: 'Danh mục 0 khóa để kiểm thử xóa D-21' }
  });
  const emptyCatId = emptyCatRes.data?.categoryId || emptyCatRes.data?.id;

  const unusedCatRes = await apiReq('POST', '/api/v1/admin/categories', {
    token: admToken,
    body: { name: 'QA Danh Mục Phụ', description: 'Danh mục phụ trợ kiểm thử' }
  });
  const extraCatId = unusedCatRes.data?.categoryId || unusedCatRes.data?.id;

  async function createFullCourse(title, price, categoryId, numLessons, { addQuiz = false } = {}) {
    const cRes = await apiReq('POST', '/api/courses', {
      token: gvaToken,
      body: {
        title,
        description: `Mô tả chi tiết kiểm thử tự động cho khóa học ${title} đầy đủ trên 20 ký tự.`,
        price,
        level: 'Beginner',
        categoryId,
        thumbnailUrl: 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800&q=80'
      }
    });
    const courseId = cRes.data?.courseId || cRes.data?.id;
    const secRes = await apiReq('POST', `/api/courses/${courseId}/sections`, {
      token: gvaToken,
      body: { title: 'Chương 1: Kiến thức nền tảng', orderIndex: 1 }
    });
    const sectionId = secRes.data?.sectionId || secRes.data?.id;
    const lessonIds = [];
    for (let i = 1; i <= numLessons; i++) {
      const lRes = await apiReq('POST', `/api/sections/${sectionId}/lessons`, {
        token: gvaToken,
        body: {
          title: `Bài ${i}: Nội dung bài học số ${i}`,
          videoUrl: `https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4`,
          contentType: 'Video',
          contentText: `Tài liệu bài giảng số ${i} tiếng Việt có dấu`,
          duration: 120,
          orderIndex: i,
          isPreview: i === 1
        }
      });
      lessonIds.push(lRes.data?.lessonId || lRes.data?.id);
    }
    let quizId = null;
    if (addQuiz) {
      const qRes = await apiReq('POST', '/api/v1/quizzes', {
        token: gvaToken,
        body: {
          courseSectionId: sectionId,
          title: 'Bài kiểm tra Chương 1',
          description: 'Quiz đánh giá kiến thức',
          passingScore: 80,
          timeLimitMinutes: 15,
          questions: [
            {
              content: '2 + 2 bằng mấy?',
              points: 1,
              answers: [
                { content: '4', isCorrect: true },
                { content: '5', isCorrect: false }
              ]
            },
            {
              content: 'Java thuộc hệ sinh thái nào?',
              points: 1,
              answers: [
                { content: 'JVM', isCorrect: true },
                { content: 'RubyVM', isCorrect: false }
              ]
            }
          ]
        }
      });
      quizId = qRes.data?.quizId || qRes.data?.id || null;
    }
    return { courseId, sectionId, lessonIds, quizId, slug: cRes.data?.slug };
  }

  // 4. Seed courses covering all 5 statuses + free + paid + 3 lessons + 10 lessons + split co-instructor
  const cDraft = await createFullCourse('QA Khóa Bản Nháp Draft', 150000, activeCatId, 2);
  const cPend = await createFullCourse('QA Khóa Chờ Duyệt Pending', 200000, activeCatId, 2);
  await apiReq('POST', `/api/v1/instructor/courses/${cPend.courseId}/submit`, { token: gvaToken });

  const cNeedsRev = await createFullCourse('QA Khóa Cần Sửa NeedsReview', 180000, activeCatId, 2);
  await apiReq('POST', `/api/v1/instructor/courses/${cNeedsRev.courseId}/submit`, { token: gvaToken });
  await apiReq('POST', `/api/v1/admin/courses/${cNeedsRev.courseId}/reject`, {
    token: admToken,
    body: { reviewNote: 'Cần bổ sung thêm ví dụ thực tế' }
  });

  const cHidden = await createFullCourse('QA Khóa Đã Ẩn Hidden', 250000, activeCatId, 2);
  await apiReq('POST', `/api/v1/instructor/courses/${cHidden.courseId}/submit`, { token: gvaToken });
  await apiReq('POST', `/api/v1/admin/courses/${cHidden.courseId}/approve`, { token: admToken });
  await apiReq('POST', `/api/v1/admin/courses/${cHidden.courseId}/hide`, {
    token: admToken,
    body: { reason: 'Ẩn để kiểm thử trạng thái Hidden' }
  });

  const cFree3 = await createFullCourse('Lập Trình Java Cơ Bản Miễn Phí', 0, activeCatId, 3);
  await apiReq('POST', `/api/v1/instructor/courses/${cFree3.courseId}/submit`, { token: gvaToken });
  await apiReq('POST', `/api/v1/admin/courses/${cFree3.courseId}/approve`, { token: admToken });

  const cFree10 = await createFullCourse('Khóa Học Chứng Chỉ 10 Bài Tiếng Việt', 0, secondCatId, 10);
  await apiReq('POST', `/api/v1/instructor/courses/${cFree10.courseId}/submit`, { token: gvaToken });
  await apiReq('POST', `/api/v1/admin/courses/${cFree10.courseId}/approve`, { token: admToken });

  const cPaid1 = await createFullCourse('Lập Trình Java Nâng Cao Spring Boot', 300000, activeCatId, 3, { addQuiz: true });
  await apiReq('POST', `/api/v1/instructor/courses/${cPaid1.courseId}/submit`, { token: gvaToken });
  await apiReq('POST', `/api/v1/admin/courses/${cPaid1.courseId}/approve`, { token: admToken });

  const cPaidSplit = await createFullCourse('Kiến Trúc Microservices Chuyên Sâu', 1000000, secondCatId, 2);
  await apiReq('POST', `/api/v1/instructor/courses/${cPaidSplit.courseId}/submit`, { token: gvaToken });
  await apiReq('POST', `/api/v1/admin/courses/${cPaidSplit.courseId}/approve`, { token: admToken });
  // Invite and accept GV-CO with 2000 bps (20%)
  const invRes = await apiReq('POST', `/api/v1/instructor/courses/${cPaidSplit.courseId}/co-instructors`, {
    token: gvaToken,
    body: { email: accounts.GVCO, shareBps: 2000 }
  });
  const invId = invRes.data?.invitationId || invRes.data?.id;
  const gvcoLogin = await loginWeb(accounts.GVCO, QA_PASS);
  if (invId && gvcoLogin.token) {
    await apiReq('POST', `/api/v1/instructor/invitations/${invId}/accept`, { token: gvcoLogin.token });
  }

  // 5. Coupons (valid + expired + inactive)
  const cpnValid = await apiReq('POST', '/api/coupons', {
    token: admToken,
    body: { code: 'QAGIAM20', discountPercentage: 20, expiryDate: '2028-12-31T23:59:59', isActive: true }
  });
  const cpnExpired = await apiReq('POST', '/api/coupons', {
    token: admToken,
    body: { code: 'QAEXPIRED', discountPercentage: 15, expiryDate: '2024-01-01T00:00:00', isActive: true }
  });
  const cpnInactive = await apiReq('POST', '/api/coupons', {
    token: admToken,
    body: { code: 'QAINACTIVE', discountPercentage: 10, expiryDate: '2028-12-31T23:59:59', isActive: false }
  });

  const state = {
    seededAt: new Date().toISOString(),
    accounts,
    userIds,
    categories: { activeCatId, secondCatId, emptyCatId, extraCatId },
    courses: { cDraft, cPend, cNeedsRev, cHidden, cFree3, cFree10, cPaid1, cPaidSplit },
    coupons: {
      valid: cpnValid.data?.code || 'QAGIAM20',
      expired: cpnExpired.data?.code || 'QAEXPIRED',
      inactive: cpnInactive.data?.code || 'QAINACTIVE',
    },
    seedLog: createdInfo,
  };
  saveState(state);
  saveEvidence('RUN-W-C1-0000', 'seed_summary', state);
  console.log('Seed completed cleanly. Saved qa_env_state.json.');
}

main().catch(err => {
  console.error('SEED ERROR:', err);
  process.exit(1);
});
