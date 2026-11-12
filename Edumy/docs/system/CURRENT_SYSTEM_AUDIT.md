# Current System Audit — EduMy

**Audit Date**: 2026-07-30
**Auditor Role**: Senior Software Architect / ML Engineer / Technical Reviewer
**Audit Status**: Documentation audit complete; build/runtime verification partial.

---

## 1. Executive Summary

### What Exists
EduMy is a functional online learning platform with a **React 19 frontend**, **ASP.NET Core (.NET 10) backend**, **SQL Server database** (LocalDB), a **FastAPI ML inference service** (local only), and a **separate ML training repository** on GitHub.

### Web Completeness: ~75%
The web platform covers user management, course CRUD, enrollment, payment (mock Stripe), reviews, wishlist, cart, quizzes, certificates, and dashboards for Student/Instructor/Admin. Most CRUD features work end-to-end via API.

### ML Completeness: ~10%
- All 3 ML tasks exist as **README plans + baseline heuristics** only.
- The local FastAPI service (`MLService/main.py`) uses **keyword-based fallback logic** — no real ML models are loaded.
- The GitHub repo (`edumy-ml`) has notebook stubs and README descriptions, but notebooks for Sentiment and Recommendation contain **only READMEs** (no `.ipynb` files yet). Classification has one notebook (`course_classification_mlp (1).ipynb`) — needs verification of contents.
- No model artifact files (`.keras`, `.pkl`, `.h5`) are confirmed present in the `saved_models/` directory on GitHub.

### Integration Completeness: ~40%
- ASP.NET Core **does call FastAPI** via `HttpClient` with Polly retry/circuit-breaker.
- React **does call** the recommendation and review endpoints which trigger ML.
- However, all ML calls resolve to **fallback heuristic responses**.

### Top 3 Risks
1. **CRITICAL**: All 3 ML tasks return hardcoded/heuristic results — no trained models exist.
2. **CRITICAL**: Recommendation endpoint returns static `[1, 2, 3]` — completely fake.
3. **HIGH**: Secrets (JWT key, Stripe keys, DB password in docker-compose) are committed in `appsettings.json` and `docker-compose.yml`.

---

## 2. Repository Structure

```
Edumy/
├── Backend/                    # ASP.NET Core Web API (.NET 10)
│   ├── Controllers/            # 21 controllers
│   ├── Data/                   # DbContext + DataSeeder
│   ├── DTOs/                   # 4 DTO files
│   ├── Middlewares/            # ExceptionMiddleware
│   ├── Migrations/             # 8 migration files
│   ├── Models/                 # 30 entity files
│   ├── Services/               # MachineLearningService.cs (1 file)
│   ├── Properties/             # launchSettings.json
│   ├── Program.cs
│   ├── appsettings.json
│   ├── EduMy.Backend.csproj
│   └── Dockerfile
├── Frontend/                   # React 19 + Vite 8
│   ├── src/
│   │   ├── api/                # axiosConfig.js
│   │   ├── components/         # 13 components
│   │   ├── context/            # AuthContext.jsx
│   │   ├── pages/              # 29 page files
│   │   ├── App.jsx
│   │   └── main.jsx
│   ├── .env
│   ├── package.json
│   └── Dockerfile
├── MLService/                  # FastAPI local fallback service
│   ├── main.py                 # Baseline heuristic endpoints
│   ├── requirements.txt
│   ├── Dockerfile
│   └── venv/
├── docker-compose.yml
└── README.md
```

**GitHub ML Repo** (`EduMy-team/edumy-ml`):
```
edumy-ml/
├── app/                        # FastAPI production service (partial)
│   └── main.py
├── data/                       # Data directory (contents unknown)
├── notebooks/
│   ├── 1-sentiment-lstm/       # README only — no .ipynb files
│   ├── 2-classify-mlp/         # README + course_classification_mlp (1).ipynb
│   └── 3-recommend-ncf/        # README only — no .ipynb files
├── saved_models/               # Model artifacts directory (contents unknown)
├── requirements.txt
└── README.md
```

### Component Summary

| Thành phần | Đường dẫn | Công nghệ | Vai trò | Trạng thái |
|---|---|---|---|---|
| Frontend | `Frontend/` | React 19, Vite 8, Axios, Bootstrap 5, Framer Motion | SPA UI | WORKING |
| Backend | `Backend/` | ASP.NET Core (.NET 10), EF Core, JWT, Serilog, Polly | REST API | WORKING |
| Database | LocalDB via EF Core | SQL Server (localdb)\mssqllocaldb | Data persistence | WORKING |
| ML Service (local) | `MLService/` | FastAPI, Python 3.12 | ML inference fallback | MOCK |
| ML Service (GitHub) | `edumy-ml/app/` | FastAPI, TensorFlow, scikit-learn | ML inference production | PARTIAL |
| ML Training | `edumy-ml/notebooks/` | Python, TensorFlow, Keras, scikit-learn | Model training | PARTIAL |
| Deployment | `docker-compose.yml` | Docker Compose | Multi-service deploy | NEEDS_VERIFICATION |
| Tests | — | — | — | MISSING |

---

## 3. Frontend Audit

### Technology Stack
- **React**: 19.2.7
- **Language**: JavaScript (JSX) — NOT TypeScript
- **Build Tool**: Vite 8.1.1
- **State Management**: React Context (`AuthContext`)
- **HTTP Client**: Axios 1.18.1
- **Routing**: react-router-dom 7.18.1
- **UI**: Bootstrap 5.3.8 + custom CSS + Framer Motion
- **Auth Storage**: JWT token in `localStorage`
- **Icons**: lucide-react + react-icons
- **Google OAuth**: @react-oauth/google
- **API Base URL**: `VITE_API_URL=http://localhost:5150/api` (from `.env`)

### Pages & Features

| Chức năng | Component/Page | API được gọi | Dữ liệu thật hay mock | Trạng thái |
|---|---|---|---|---|
| Home | `HomePage.jsx` | GET `/courses`, GET `/categories`, GET `/courses/recommend` | API thật, ML recommend = fallback | PARTIAL |
| Login | `Login.jsx` | POST `/auth/login` | Thật | WORKING |
| Register | `Register.jsx` | POST `/auth/register` | Thật | WORKING |
| Course List | `CourseList.jsx` | GET `/courses?search=&categoryId=` | Thật | WORKING |
| Course Detail | `CourseDetail.jsx` | GET `/courses/{id}`, POST `/courses/{id}/reviews` | Thật + sentiment hardcoded UI values | PARTIAL |
| Course Create | `CourseCreate.jsx` | POST `/courses`, GET `/categories` | Thật, ML classify nếu bỏ trống category | WORKING |
| Instructor Dashboard | `InstructorDashboard.jsx` | GET `/courses/my-courses`, GET `/instructor/stats`, POST `/courses/{id}/status` | Thật + ML sentiment stats from DB | WORKING |
| Admin Dashboard | `AdminDashboard.jsx` | GET `/admin/stats`, GET `/admin/courses`, GET `/admin/ml-monitoring` | Thật | WORKING |
| Cart | `Cart.jsx` | GET/POST `/cart/*` | Thật | WORKING |
| My Learning | `MyLearning.jsx` | GET `/courses/enrolled` | Thật | WORKING |
| Course Player | `CoursePlayer.jsx` | GET `/courses/{id}`, POST `/{id}/lessons/{lid}/complete` | Thật | WORKING |
| Payment | `MockPaymentGateway.jsx` | Mock payment flow | MOCK | MOCK |
| Wishlist | `Wishlist.jsx` | GET/POST/DELETE `/wishlist/*` | Thật | WORKING |
| User Profile | `UserProfile.jsx` | GET/PUT `/users/me` | Thật | WORKING |
| AI Recommendations | Section in `HomePage.jsx` | GET `/courses/recommend` | ML → fallback `[1,2,3]` | HARDCODED |

### Hardcoded UI Values Found in `CourseDetail.jsx`
- `averageRating > 0 ? course.averageRating : '4.7'` — fallback rating (line 180)
- `(245,102 ratings)` — hardcoded count (line 184)
- `course.studentCount || '890,230'` — fallback student count (line 185)
- `course.instructor?.fullName || 'Dr. Angela Yu'` — fallback instructor name (line 189)
- `65 hours on-demand video` — hardcoded (line 362)
- `80 articles` — hardcoded (line 363)
- `₫1,200,000` old price — hardcoded (line 320)
- `82% off` — hardcoded discount (line 321)
- `5 hours left at this price!` — hardcoded urgency (line 325)

These are cosmetic UI fallbacks, not data integrity issues, but they mask missing real data.

---

## 4. Backend Audit

### Technology Stack
- **.NET**: 10.0 (`net10.0`)
- **Architecture**: Layered — Controllers → Services → DbContext (no Repository pattern)
- **ORM**: Entity Framework Core 10.0.10 (SQL Server provider)
- **Auth**: JWT Bearer (symmetric key) + Google OAuth
- **Logging**: Serilog (Console + File)
- **HTTP resilience**: Polly (retry + circuit breaker) for ML calls
- **Swagger**: Swashbuckle.AspNetCore 10.2.3
- **Payment**: Stripe.net 52.1.1
- **CORS**: AllowAll policy (open for development)
- **AutoMapper**: NOT used
- **Background jobs**: NOT used

### Key Controllers & Endpoints

| Method | Route | Controller | Authorization | ML Integration | Trạng thái |
|---|---|---|---|---|---|
| GET | `/api/courses/recommend` | CoursesController | Student | `RecommendCoursesAsync()` | CONNECTED_BUT_FALLBACK |
| POST | `/api/courses` | CoursesController | Instructor,Admin | `ClassifyCourseAsync()` if no category | CONNECTED_BUT_FALLBACK |
| POST | `/api/courses/{id}/status` ("Analyzing") | CoursesController | Instructor,Admin | `ClassifyCourseAsync()` + `AnalyzeContentAsync()` | CONNECTED_BUT_FALLBACK |
| POST | `/api/courses/{id}/reviews` | CoursesController | Student | `AnalyzeSentimentAsync()` | CONNECTED_BUT_FALLBACK |
| POST | `/api/reviews/course/{id}` | ReviewsController | Student | `AnalyzeSentimentAsync()` | CONNECTED_BUT_FALLBACK |
| POST | `/api/mltest/sentiment` | MLTestController | None | `AnalyzeSentimentAsync()` | TEST_ONLY |
| POST | `/api/mltest/classify` | MLTestController | None | `ClassifyCourseAsync()` | TEST_ONLY |
| POST | `/api/mltest/recommend` | MLTestController | None | `RecommendCoursesAsync()` | TEST_ONLY |
| GET | `/api/admin/ml-monitoring` | AdminController | Admin | DB query on CourseMlAnalyses | WORKING |
| POST | `/api/admin/ml-analyses/{id}/approve` | AdminController | Admin | Status update | WORKING |
| POST | `/api/admin/ml-analyses/{id}/override` | AdminController | Admin | Category override | WORKING |
| GET | `/api/instructor/stats` | InstructorController | Instructor,Admin | sentimentStats, qualityScore from DB | WORKING |

### ML Integration Architecture Assessment
**✅ GOOD**: Backend uses `IMachineLearningService` interface with proper DI via `HttpClientFactory` + Polly. This is clean architecture.
**✅ GOOD**: ML results are stored in `CourseMlAnalysis` table with audit trail.
**⚠️ ISSUE**: When ML service fails, methods return `null` and the controller falls back silently. No explicit error logging to UI for recommendation failure.
**⚠️ ISSUE**: `MLTestController` has no authorization — anyone can call test endpoints.

---

## 5. Database Audit

### Entity Summary (30 entities)

| Entity/Table | Primary Key | Foreign Keys | Mục đích | Hỗ trợ ML |
|---|---|---|---|---|
| User | UserId (int) | — | User accounts | ✅ user_id for recommendation |
| Role | Id (int) | — | Role definitions | — |
| UserRole | UserId+RoleId | User, Role | Many-to-many roles | — |
| Course | CourseId (int) | InstructorId, CategoryId | Course data | ✅ course_id for recommendation |
| Category | CategoryId (int) | ParentCategoryId (self-ref) | Course categories | ✅ classification target |
| Enrollment | EnrollmentId (int) | UserId, CourseId | Student enrollments | ✅ implicit feedback |
| Review | ReviewId (int) | UserId, CourseId | Course reviews | ✅ SentimentLabel, SentimentScore |
| LessonProgress | Id (int) | UserId, CourseId, LessonId | Lesson completion | ✅ completion tracking |
| Wishlist | Id (int) | UserId, CourseId | Wishlist items | ✅ implicit interest signal |
| SearchHistory | Id (int) | UserId (nullable) | Search queries | ✅ search behavior |
| UserActivity | Id (int) | UserId | Generic activity log | ✅ ActivityType + ResourceId |
| CourseMlAnalysis | Id (int) | CourseId, ApprovedByUserId | ML prediction storage | ✅ classification results |
| CourseMlAnalysisTag | Id (int) | CourseMlAnalysisId | ML-generated tags | ✅ tag predictions |
| CourseSection | SectionId (int) | CourseId | Course structure | — |
| Lesson | LessonId (int) | SectionId | Lesson content | — |
| Order | OrderId (int) | UserId | Purchase orders | ✅ purchase signal |
| OrderItem | Id (int) | OrderId, CourseId | Order line items | — |
| Cart/CartItem | — | UserId, CourseId | Shopping cart | ✅ cart-add signal |
| Certificate | Id (int) | UserId, CourseId | Completion certs | — |
| Tag/CourseTag | — | CourseId, TagId | Content tags | — |
| Coupon/CourseCoupon | — | — | Discounts | — |
| Quiz/Question/Answer | — | CourseSectionId | Assessments | — |
| QuizAttempt | — | UserId, QuizId | Quiz results | — |
| RefreshToken | Id (int) | UserId | JWT refresh | — |

### User Interaction Tracking

| Interaction | Có lưu | Table/Entity | Timestamp field | Dùng cho Recommendation |
|---|---|---|---|---|
| User đăng ký khóa học | ✅ | Enrollment | EnrolledAt | ✅ Primary signal |
| User hoàn thành bài học | ✅ | LessonProgress | CompletedAt | ✅ Completion signal |
| User đánh giá | ✅ | Review | CreatedAt | ✅ Explicit feedback |
| User thêm wishlist | ✅ | Wishlist | (no timestamp) | ⚠️ No timestamp |
| User tìm kiếm | ✅ | SearchHistory | SearchedAt | ✅ Search behavior |
| User xem khóa học | ⚠️ Partial | UserActivity | Timestamp | ✅ If ActivityType=ViewCourse |
| User click khóa học | ❌ | — | — | ❌ Not tracked |
| User mua khóa học | ✅ | Order | CreatedAt | ✅ Purchase signal |
| User thêm giỏ hàng | ✅ | CartItem | (no timestamp) | ⚠️ No timestamp |

### ML Prediction Storage
- **CourseMlAnalysis**: Stores classification results with `PrimaryCategory`, `Confidence`, `QualityScore`, `RiskLevel`, `Status`, `ModelVersion`, approval workflow.
- **Review**: Has `SentimentLabel` (string) and `SentimentScore` (double?) directly on entity.
- **Missing**: No table for recommendation predictions/scores. No table for model version tracking across all 3 tasks.

---

## 6. FastAPI ML Service Audit

### Local Service (`MLService/main.py`)

| Method | Route | Input | Output | Model thật | Trạng thái |
|---|---|---|---|---|---|
| POST | `/sentiment/analyze` | `{text}` | `{label, score}` | ❌ | HARDCODED |
| POST | `/classify/course` | `{title, description}` | `{category, confidence, is_fallback}` | ❌ | HARDCODED |
| POST | `/recommend/courses` | `{user_id}` | `{recommendedCourseIds, scores}` | ❌ | HARDCODED |
| POST | `/course/analyze-content` | `{title, description}` | `{tags, is_toxic, toxicity_score, quality_score, popularity_score, is_fallback}` | ❌ | HARDCODED |
| GET | `/health` | — | `{status, model_loaded: false, using_fallback: true}` | — | WORKING |

### Dangerous Patterns Found
1. **Sentiment**: `BaselineSentimentAnalyzer` — counts intersections with 6 positive words and 5 negative words. Returns fixed scores `0.85` or `0.50`. **NOT a real model.**
2. **Classification**: `BaselineClassifier` — dictionary lookup with 3 categories and ~15 keywords. Sigmoid normalization of sum. **NOT a real model.**
3. **Recommendation**: Returns hardcoded `[1, 2, 3]` with scores `[0.9, 0.8, 0.7]`. **Completely static.**
4. **Content Analysis**: Keyword matching for toxicity (8 toxic words), tag extraction from 12 common tags, quality = word_count/200. **NOT a real model.**
5. **All endpoints return `is_fallback: true`** — this is honest but confirms no real models are deployed.

### GitHub ML Service (`edumy-ml/app/main.py`)
- References `app.api.sentiment`, `app.api.classify`, `app.api.recommend`, `app.api.success_prediction` modules
- References `app.core.config.settings`
- Has proper CORS middleware
- **File appears truncated on GitHub** — content cut off mid-line
- Has `tensorflow`, `scikit-learn` in `requirements.txt` — suggests intention for real models

---

## 7. Sentiment Analysis Audit (Bài toán 1)

| Hạng mục | Hiện trạng | Bằng chứng | Vấn đề | Hướng xử lý |
|---|---|---|---|---|
| Dataset nguồn | README claims "Udemy reviews + Vietnamese" | `notebooks/1-sentiment-lstm/README.md` | No link, no license, "thu thap them tieng Viet" suggests hand-collected | Cần dataset công khai có nguồn rõ |
| Dataset số lượng | README claims ~50,000 | README | Chưa xác minh — no notebook exists | NEEDS_VERIFICATION |
| Notebook files | README lists 5 notebooks (01-05) | GitHub listing | **Only README.md exists — NO .ipynb files found** | CRITICAL: Cần tạo notebooks |
| LSTM model | README claims LSTM | README | No implementation exists | Cần implement |
| Model export | README claims saved_models/ | README | No evidence of actual exports | Cần implement |
| FastAPI endpoint | `/sentiment/analyze` exists | `MLService/main.py:76-91` | Keyword counting, NOT LSTM | HARDCODED |
| Backend integration | `AnalyzeSentimentAsync()` called in 2 controllers | `CoursesController.cs:561`, `ReviewsController.cs:47` | Works but receives fallback results | CONNECTED_BUT_FALLBACK |
| DB storage | `Review.SentimentLabel`, `Review.SentimentScore` | `Review.cs:12-13` | Fields exist and are populated | WORKING |
| Seed data | Seed reviews have hardcoded sentiment labels | `DataSeeder.cs:396-414` | Labels assigned from array, not from ML | HARDCODED |
| React display | Sentiment badge shown in reviews | `CourseDetail.jsx:261-265` | Displays badge with color coding | WORKING |

---

## 8. Classification Audit (Bài toán 2)

| Hạng mục | Hiện trạng | Bằng chứng | Vấn đề | Hướng xử lý |
|---|---|---|---|---|
| Dataset | Kaggle Udemy Courses | README: `kaggle.com/datasets/andrewmvd/udemy-courses` | ✅ Source xác định, 3,682 courses, 4 categories | OK |
| Notebook | `course_classification_mlp (1).ipynb` | GitHub file listing | Exists nhưng chưa xác minh nội dung | NEEDS_VERIFICATION |
| Categories | 4: Development, Business, Design, Music | README | Backend seed có 8 categories (không khớp) | Mismatch cần xử lý |
| MLP Architecture | Input(5000)→512→256→128→4(softmax) | README | Cần xác minh trong notebook | NEEDS_VERIFICATION |
| TF-IDF | max_features=5000 | README | Cần kiểm tra fit on train only | NEEDS_VERIFICATION |
| Accuracy | README claims 94.20% | README | Chưa xác minh — có thể từ output cũ | NEEDS_VERIFICATION |
| SVM model | Chưa có | — | Yêu cầu bắt buộc: MLP + Linear SVM | MISSING |
| Model export | README lists 3 files | README | `mlp_course_classifier.keras`, `tfidf_vectorizer.pkl`, `label_encoder.pkl` | NEEDS_VERIFICATION |
| FastAPI endpoint | `/classify/course` exists | `MLService/main.py:46-74` | Keyword dictionary, NOT MLP | HARDCODED |
| Backend integration | `ClassifyCourseAsync()` called | `CoursesController.cs:202,306` | Works but receives fallback results | CONNECTED_BUT_FALLBACK |
| DB storage | `CourseMlAnalysis.PrimaryCategory`, `Confidence` | `CourseMlAnalysis.cs` | Fields exist | WORKING |
| Confidence | Sigmoid of keyword score sum | `MLService/main.py:69` | NOT a real probability | FAKE |

---

## 9. Recommendation Audit (Bài toán 3)

| Hạng mục | Hiện trạng | Bằng chứng | Vấn đề | Hướng xử lý |
|---|---|---|---|---|
| Dataset | README claims ~5,000 users, ~500 courses, ~50,000 interactions | `notebooks/3-recommend-ncf/README.md` | Chưa rõ nguồn — likely intended to be synthetic | CRITICAL |
| Notebook files | README lists 5 notebooks (01-05) | GitHub listing | **Only README.md exists — NO .ipynb files** | CRITICAL |
| GMF | Described in README | README | No implementation | MISSING |
| MLP branch | Described in README | README | No implementation | MISSING |
| NeuMF | Described in README | README | No implementation | MISSING |
| Popularity baseline | Not mentioned | — | Required for comparison | MISSING |
| HR@10 | Mentioned in README | README | No implementation | MISSING |
| NDCG@10 | Mentioned in README | README | No implementation | MISSING |
| FastAPI endpoint | `/recommend/courses` exists | `MLService/main.py:147-151` | **Returns hardcoded `[1, 2, 3]`** | HARDCODED |
| Backend integration | `RecommendCoursesAsync()` called | `CoursesController.cs:31` | Works but receives `[1,2,3]` | CONNECTED_BUT_FALLBACK |
| React display | "AI Recommended For You" section | `HomePage.jsx:72-90` | Displays courses with "AI Match" badge | WORKING (with fake data) |
| Cold-start | Not implemented | — | No fallback for new users | MISSING |
| ID mapping | Course IDs from ML = Course IDs in DB? | See Section 13 | CRITICAL risk | CRITICAL |

---

## 10. Web–ML Integration Audit

### Connection Flow
```
React ──HTTP──> ASP.NET Core ──HTTP──> FastAPI (port 8001)
                     │                      │
                     │                      └── Fallback heuristics (no real model)
                     │
                     └── Stores result in DB (CourseMlAnalysis, Review.Sentiment*)
```

### Integration Status
- **HttpClient configured**: ✅ `MachineLearning:BaseUrl = http://localhost:8001` in `appsettings.json`
- **Polly resilience**: ✅ Retry (3 attempts, exponential backoff) + Circuit Breaker (5 failures, 30s break)
- **Interface abstraction**: ✅ `IMachineLearningService` with proper DI
- **Error handling**: ✅ try/catch with logging, returns `null` on failure
- **Graceful degradation**: ⚠️ Partial — When ML returns null:
  - Sentiment: defaults to "Neutral" / 0.5
  - Classification: skips category assignment
  - Recommendation: returns empty list
  - Content analysis: sets course to "NeedsReview"
- **Model loaded check**: ❌ Backend does not verify `/health` endpoint before calling

---

## 11. Build/Test Status

| Thành phần | Lệnh | Kết quả | Lỗi chính | Ghi chú |
|---|---|---|---|---|
| Frontend dev | `npm run dev` | ✅ WORKING | PowerShell script policy (workaround: `cmd /c`) | Runs on port 5173 |
| Frontend build | `npm run build` | NEEDS_VERIFICATION | — | Not tested in this audit |
| Backend restore | `dotnet restore` | NEEDS_VERIFICATION | — | Requires .NET 10 SDK |
| Backend build | `dotnet build` | NEEDS_VERIFICATION | — | Requires .NET 10 SDK |
| ML Service | `uvicorn` | NEEDS_VERIFICATION | — | Python 3.12 venv exists |
| Docker Compose | `docker compose config` | NEEDS_VERIFICATION | Port mismatch: MLService Docker=8000 vs appsettings=8001 | See note below |

**⚠️ Port Mismatch**: `docker-compose.yml` exposes MLService on port 8000, but `appsettings.json` has `MachineLearning:BaseUrl = http://localhost:8001`. Docker-compose internally uses `http://mlservice:8000` (correct for container), but local dev expects 8001.

---

## 12. Security and Secrets

| Loại Secret | File | Có tracked | Mức nguy hiểm | Cần xử lý |
|---|---|---|---|---|
| JWT Secret Key | `appsettings.json:6` | ✅ Yes | HIGH | Move to User Secrets / env var |
| Stripe SecretKey | `appsettings.json:12` | ✅ Yes (placeholder `sk_test_51...`) | MEDIUM | Placeholder, but pattern is dangerous |
| Stripe WebhookSecret | `appsettings.json:14` | ✅ Yes (placeholder `whsec_...`) | MEDIUM | Move to env var |
| SQL SA Password | `docker-compose.yml:7` | ✅ Yes | HIGH | `EduMySuperSecurePassword123!` exposed |
| DB Connection String | `docker-compose.yml:32` | ✅ Yes | HIGH | Contains SA password |
| LocalDB Connection | `appsettings.json:3` | ✅ Yes | LOW | Trusted_Connection only |

### .gitignore Assessment
- Frontend `.gitignore`: ✅ Covers `node_modules`, `dist`, editor files
- Backend: ❌ **No .gitignore found** — `bin/`, `obj/`, `appsettings.*.json` may be tracked
- MLService: ❌ **No .gitignore found** — `venv/`, `__pycache__/` are present
- Root: ❌ **No .gitignore found** for `*.env`, model artifacts, large datasets

---

## 13. Critical Risks

### RISK-001: ML Models Do Not Exist (CRITICAL)
All three ML tasks have NO trained models. FastAPI returns hardcoded/heuristic results. This means:
- Sentiment labels on reviews are fake
- Category predictions are keyword-based
- Recommendations are static `[1, 2, 3]`

### RISK-002: Recommendation ID Domain Mismatch (CRITICAL)
The recommendation endpoint returns `[1, 2, 3]` which maps to Course IDs 1, 2, 3 in the EduMy database. When real ML models are trained:
- **OULAD dataset** contains student-module interactions with OULAD-specific IDs
- These IDs have NO mapping to EduMy Course IDs
- Directly using OULAD item IDs to query EduMy courses would return wrong/no results
- **Resolution needed**: Use OULAD for model validation only; use EduMy interaction data for production inference, or use popularity fallback

### RISK-003: Secrets in Source Control (HIGH)
JWT keys, Stripe keys, and database passwords are committed in configuration files. Even if they are test/placeholder values, this establishes a dangerous pattern.

### RISK-004: No Automated Tests (HIGH)
Zero unit tests, integration tests, or API tests exist for any component. Changes to ML integration could break existing functionality silently.

### RISK-005: Category Mismatch Between ML and Web (HIGH)
- ML notebook (classify) trains on 4 categories: Development, Business, Design, Music
- Backend DataSeeder creates 8 categories: Development, Business, Design, Marketing, IT & Software, Office Productivity, Personal Development, Photography
- ML predictions for "Music" would not match any backend category

---

## 14. Recommended Next Steps

1. **Phase 1 (Current)**: ✅ Audit complete. Review findings with team.
2. **Phase 2**: Fix category alignment between ML and Web (must agree on category taxonomy).
3. **Phase 3**: Implement Classification (MLP + SVM) using Kaggle Udemy dataset — most mature starting point.
4. **Phase 4**: Implement Sentiment (LSTM/BiLSTM) with public education review dataset.
5. **Phase 5**: Implement Recommendation (Popularity + GMF + NeuMF) with OULAD for validation.
6. **Phase 6**: Deploy real models to FastAPI, replacing fallback heuristics.
7. **Phase 7**: Connect ASP.NET Core to production FastAPI with model artifacts.
8. **Phase 8**: Update React UI to display ML results with confidence indicators.
9. **Phase 9**: Persist ML predictions and add monitoring tables.
10. **Phase 10**: Testing, documentation, and presentation preparation.

---

## Documentation Mapping

| Tài liệu | Mục đích |
|----------|----------|
| CURRENT_SYSTEM_AUDIT.md | Tổng quan audit hệ thống |
| IMPLEMENTATION_CHECKLIST.md | Checklist trạng thái |
| WEB_ML_BOUNDARY.md | Phân biệt Web, ML và Integration |
| ML_GAP_ANALYSIS.md | Khoảng cách giữa hiện trạng và mục tiêu |
| IMPLEMENTATION_PLAN.md | Kế hoạch triển khai theo phase |
| DATA_FLOW_API_MAP.md | Luồng dữ liệu và API chi tiết |
| ML_MODEL_STATUS.md | Trạng thái model hiện tại |
| RISK_ANALYSIS.md | Rủi ro kỹ thuật và học thuật |
