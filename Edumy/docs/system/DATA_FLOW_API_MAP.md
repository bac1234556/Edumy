# Data Flow & API Map — EduMy

**Date**: 2026-07-30

---

## 1. System Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────┐
│                         FRONTEND (React 19)                       │
│                         localhost:5173                             │
│                                                                   │
│  ┌─────────┐ ┌──────────┐ ┌───────────┐ ┌──────────┐            │
│  │HomePage │ │CourseList │ │CourseDetail│ │Instructor│            │
│  │ .jsx    │ │ .jsx     │ │ .jsx      │ │Dashboard │            │
│  └────┬────┘ └─────┬────┘ └─────┬─────┘ └─────┬────┘            │
│       │            │            │              │                  │
│       └────────────┼────────────┼──────────────┘                  │
│                    │       Axios (JWT)                             │
└────────────────────┼──────────────────────────────────────────────┘
                     │
                     ▼  HTTP (localhost:5150/api)
┌──────────────────────────────────────────────────────────────────┐
│                    BACKEND (ASP.NET Core .NET 10)                  │
│                    localhost:5150                                  │
│                                                                   │
│  ┌─────────────┐  ┌──────────────┐  ┌──────────────┐            │
│  │ Controllers │  │   Services    │  │  Middleware   │            │
│  │ (21 files)  │→ │ML Service.cs │→ │  JWT Auth     │            │
│  └──────┬──────┘  └───────┬──────┘  │  CORS         │            │
│         │                 │         │  Exception     │            │
│         │                 │         └──────────────┘             │
│         ▼                 ▼                                       │
│  ┌─────────────┐  ┌─────────────┐                                │
│  │  EF Core    │  │ HttpClient  │                                │
│  │  DbContext  │  │ + Polly     │                                │
│  └──────┬──────┘  └──────┬──────┘                                │
└─────────┼────────────────┼───────────────────────────────────────┘
          │                │
          ▼                ▼  HTTP (localhost:8001)
┌────────────────┐  ┌──────────────────────────────────────────┐
│   SQL Server   │  │        ML SERVICE (FastAPI)               │
│   (LocalDB)    │  │        localhost:8001                     │
│   EduMyDb      │  │                                          │
│                │  │  ┌─────────────────┐                     │
│  Tables:       │  │  │ BaselineAnalyzer │ ←─ NO real models  │
│  - Users       │  │  │ (keyword match) │                     │
│  - Courses     │  │  └─────────────────┘                     │
│  - Reviews     │  │                                          │
│  - Enrollments │  │  Endpoints:                              │
│  - MlAnalyses  │  │  POST /sentiment/analyze                 │
│  - ...30 more  │  │  POST /classify/course                   │
│                │  │  POST /recommend/courses                  │
│                │  │  POST /course/analyze-content             │
│                │  │  GET  /health                             │
└────────────────┘  └──────────────────────────────────────────┘
```

---

## 2. Complete API Endpoint Map

### Auth (AuthController)
| Method | Route | Auth Required | Body | Response | ML |
|---|---|---|---|---|---|
| POST | `/api/auth/register` | ❌ | `{email, password, fullName, role}` | `{message, token, fullName, role}` | — |
| POST | `/api/auth/login` | ❌ | `{email, password}` | `{message, token, fullName, role}` | — |
| POST | `/api/auth/google-login` | ❌ | `{token}` | `{message, token, fullName, role}` | — |
| POST | `/api/auth/refresh` | ❌ | `{refreshToken}` | `{token, refreshToken}` | — |
| POST | `/api/auth/revoke` | ✅ Any | — | `{message}` | — |

### Courses (CoursesController)
| Method | Route | Auth Required | Body/Params | Response | ML Integration |
|---|---|---|---|---|---|
| GET | `/api/courses` | ❌ | `?search=&categoryId=&minPrice=&maxPrice=&minRating=&level=&sortBy=&sortOrder=&page=&pageSize=` | `{items, totalItems, page, pageSize, totalPages}` | — |
| GET | `/api/courses/recommend` | ✅ Student | — | `[Course]` | ✅ `RecommendCoursesAsync()` → FALLBACK |
| GET | `/api/courses/my-courses` | ✅ Instructor,Admin | — | `[Course]` | — |
| GET | `/api/courses/enrolled` | ✅ Student | — | `[Course]` | — |
| GET | `/api/courses/{id}` | ❌ | — | `Course` (with sections, lessons, reviews, tags) | — |
| POST | `/api/courses` | ✅ Instructor,Admin | `Course` | `Course` | ✅ `ClassifyCourseAsync()` if no category |
| PUT | `/api/courses/{id}` | ✅ Instructor,Admin | `Course` | `204` | ✅ Sets `NeedsReanalysis=true` |
| POST | `/api/courses/{id}/status` | ✅ Instructor,Admin | `"StatusString"` | `200` | ✅ `ClassifyCourseAsync()` + `AnalyzeContentAsync()` on "Analyzing" |
| POST | `/api/courses/{id}/reviews` | ✅ Student | `Review` | `201` | ✅ `AnalyzeSentimentAsync()` |
| POST | `/api/courses/{id}/lessons/{lid}/complete` | ✅ Any | — | `{message, progressPercentage}` | — |

### Reviews (ReviewsController)
| Method | Route | Auth Required | Body | Response | ML Integration |
|---|---|---|---|---|---|
| GET | `/api/reviews/course/{courseId}` | ❌ | — | `[Review]` | — |
| POST | `/api/reviews/course/{courseId}` | ✅ Student | `Review` | `201 Review` | ✅ `AnalyzeSentimentAsync()` |
| PUT | `/api/reviews/{id}` | ✅ Owner/Admin | `Review` | `204` | — |
| DELETE | `/api/reviews/{id}` | ✅ Owner/Admin | — | `204` | — |

### Instructor (InstructorController)
| Method | Route | Auth Required | Body | Response | ML Integration |
|---|---|---|---|---|---|
| GET | `/api/instructor/stats` | ✅ Instructor,Admin | — | `{totalCourses, totalStudents, totalRevenue, averageRating, recentReviews, revenueByDate, enrollmentByDate, sentimentStats, averageQualityScore, recommendations}` | ✅ Reads ML data from DB |

### Admin (AdminController)
| Method | Route | Auth Required | Body | Response | ML Integration |
|---|---|---|---|---|---|
| GET | `/api/admin/stats` | ✅ Admin | — | `{TotalUsers, TotalCourses, TotalRevenue}` | — |
| GET | `/api/admin/users` | ✅ Admin | — | `[{UserId, FullName, Email, Role, CreatedAt}]` | — |
| GET | `/api/admin/courses` | ✅ Admin | — | `[Course with MlAnalyses, Tags]` | — |
| PUT | `/api/admin/courses/{id}/status` | ✅ Admin | `"StatusString"` | `{message}` | — |
| PUT | `/api/admin/users/{id}/toggle-status` | ✅ Admin | — | `{message, isActive}` | — |
| PUT | `/api/admin/users/{id}/role` | ✅ Admin | `"RoleName"` | `{message}` | — |
| GET | `/api/admin/ml-monitoring` | ✅ Admin | — | `{TotalAnalyses, HighRiskCount, PendingReviews, TotalReviews, SentimentStats, AnalysesHistory}` | ✅ ML monitoring |
| POST | `/api/admin/ml-analyses/{id}/approve` | ✅ Admin | — | `{message}` | ✅ Approve ML prediction |
| POST | `/api/admin/ml-analyses/{id}/override` | ✅ Admin | `{CategoryName}` | `{message}` | ✅ Override ML prediction |

### ML Test (MLTestController)
| Method | Route | Auth Required | Body | Response | ML Integration |
|---|---|---|---|---|---|
| POST | `/api/mltest/sentiment` | ❌ **OPEN** | `{text}` | `{label, score}` | ✅ Direct ML call |
| POST | `/api/mltest/classify` | ❌ **OPEN** | `{title, description}` | `{category, confidence, tags, quality_score}` | ✅ Direct ML call |
| POST | `/api/mltest/recommend` | ❌ **OPEN** | `{userId}` | `{recommendedCourseIds, scores}` | ✅ Direct ML call |

### Other Controllers
| Controller | Key Routes | Auth |
|---|---|---|
| CategoriesController | GET `/api/categories`, POST, PUT, DELETE | Read=Public, Write=Admin |
| CartController | GET `/api/cart`, POST `/api/cart/add/{courseId}`, DELETE `/api/cart/remove/{id}` | Student |
| OrdersController | POST `/api/orders/checkout`, GET `/api/orders/my-orders` | Student |
| WishlistController | GET `/api/wishlist`, POST `/api/wishlist/add/{id}`, DELETE `/api/wishlist/remove/{id}`, GET `/api/wishlist/check/{id}` | Student |
| UsersController | GET `/api/users/me`, PUT `/api/users/me` | Any Auth |
| MediaController | POST `/api/media/upload` | Any Auth |
| CertificatesController | GET `/api/certificates/{url}`, POST `/api/certificates/generate/{courseId}` | Student |
| QuizzesController | Quiz CRUD + attempt endpoints | Various |
| PaymentsController | Stripe webhook + session creation | Various |
| CouponsController | CRUD for discount coupons | Admin |

---

## 3. ML Data Flow (Detailed)

### Flow 1: Sentiment Analysis (Review Submission)

```
Student submits review
       │
       ▼
React: POST /api/courses/{id}/reviews
       │  body: { rating: 5, comment: "Great course!" }
       │
       ▼
CoursesController.AddReview()
       │
       ├── 1. Validate: user enrolled? → check Enrollments table
       │
       ├── 2. ML Call: _mlService.AnalyzeSentimentAsync(comment)
       │       │
       │       ├── HttpClient POST http://localhost:8001/sentiment/analyze
       │       │       body: { "text": "Great course!" }
       │       │
       │       ├── FastAPI BaselineSentimentAnalyzer:
       │       │       words = ["great", "course"]
       │       │       pos_match = {"great"} ∩ positive_words = 1
       │       │       neg_match = {} = 0
       │       │       → return { "label": "Positive", "score": 0.85 }
       │       │
       │       └── On failure: returns null → defaults to Neutral/0.5
       │
       ├── 3. Save Review to DB:
       │       review.SentimentLabel = "Positive"
       │       review.SentimentScore = 0.85
       │       context.Reviews.Add(review)
       │
       └── 4. Update Course.AverageRating
```

### Flow 2: Course Classification (Course Creation)

```
Instructor creates course (no category selected)
       │
       ▼
React: POST /api/courses
       │  body: { title: "Python Bootcamp", description: "...", categoryId: null }
       │
       ▼
CoursesController.CreateCourse()
       │
       ├── 1. Save course as Draft
       │
       ├── 2. IF categoryId == null:
       │       │
       │       ├── ML Call: _mlService.ClassifyCourseAsync(title, description)
       │       │       │
       │       │       ├── HttpClient POST http://localhost:8001/classify/course
       │       │       │       body: { "title": "Python Bootcamp", "description": "..." }
       │       │       │
       │       │       ├── FastAPI BaselineClassifier:
       │       │       │       text = "python bootcamp ..."
       │       │       │       Development keywords match: "programming" → 1
       │       │       │       → return { "category": "Development", "confidence": 0.73, "is_fallback": true }
       │       │       │
       │       │       └── On failure: categoryId stays null
       │       │
       │       ├── Look up Category by name in DB
       │       ├── course.CategoryId = category.CategoryId
       │       │
       │       └── Create CourseMlAnalysis record:
       │               PrimaryCategory = "Development"
       │               Confidence = 73
       │               QualityScore = computed
       │               RiskLevel = based on confidence
       │               Status = "AutoApproved" or "NeedsManualReview"
       │
       └── 3. Return created course
```

### Flow 3: Course Recommendation (Homepage)

```
Student visits homepage
       │
       ▼
React: useEffect → api.get('/courses/recommend')
       │  Authorization: Bearer <jwt_token>
       │
       ▼
CoursesController.GetRecommendedCourses()
       │
       ├── 1. Extract userId from JWT token
       │
       ├── 2. ML Call: _mlService.RecommendCoursesAsync(userId)
       │       │
       │       ├── HttpClient POST http://localhost:8001/recommend/courses
       │       │       body: { "user_id": 5 }
       │       │
       │       ├── FastAPI handler:
       │       │       → return { "recommendedCourseIds": [1, 2, 3], "scores": [0.9, 0.8, 0.7] }
       │       │       (completely hardcoded, ignores user_id)
       │       │
       │       └── On failure: returns null → empty recommendation list
       │
       ├── 3. Query Courses WHERE CourseId IN (1, 2, 3)
       │
       └── 4. Return [Course, Course, Course]
              │
              ▼
React: Shows "AI Recommended For You" section
       with "AI Match" badge on each card
```

### Flow 4: Content Analysis (ML Trigger)

```
Instructor clicks "Run AI Analysis" button
       │
       ▼
React: POST /api/courses/{id}/status
       │  body: "Analyzing"
       │
       ▼
CoursesController.UpdateCourseStatus()
       │
       ├── 1. Set course.Status = "Analyzing"
       │
       ├── 2. ML Call: _mlService.ClassifyCourseAsync(title, description)
       │       → Same as Flow 2
       │
       ├── 3. ML Call: _mlService.AnalyzeContentAsync(title, description)
       │       │
       │       ├── HttpClient POST http://localhost:8001/course/analyze-content
       │       │
       │       ├── FastAPI BaselineContentAnalyzer:
       │       │       → Check 8 toxic keywords
       │       │       → Extract tags from 12 predefined tags
       │       │       → quality_score = min(word_count / 200, 100)
       │       │       → return { tags, is_toxic, toxicity_score, quality_score, ... }
       │       │
       │       └── On failure: course.Status = "NeedsReview"
       │
       ├── 4. Create CourseMlAnalysis record with results
       │
       ├── 5. Create CourseMlAnalysisTag records
       │
       ├── 6. Set course.NeedsReanalysis = false
       │
       └── 7. Set course.Status based on RiskLevel:
              - High risk → "NeedsManualReview"
              - Otherwise → "AutoApproved"
```

---

## 4. Authentication Flow

```
Login: POST /api/auth/login
       │  { email: "student@edumy.com", password: "Student@123" }
       │
       ▼
AuthController:
       ├── Verify password with BCrypt
       ├── Generate JWT with claims: { sub: userId, email, unique_name, role }
       ├── Set 2-hour expiry
       └── Return { token: "eyJ...", fullName, role }
              │
              ▼
React AuthContext:
       ├── localStorage.setItem('token', token)
       ├── Decode JWT base64 payload
       └── setUser({ id, email, fullName, role })
              │
              ▼
Axios Interceptor:
       └── Auto-attach: Authorization: Bearer <token>
```

### Test Accounts (from DataSeeder)

| Email | Password | Role |
|---|---|---|
| admin@edumy.com | Admin@123 | Admin |
| instructor@edumy.com | Instructor@123 | Instructor |
| instructor2@edumy.com | Instructor@123 | Instructor |
| instructor3@edumy.com | Instructor@123 | Instructor |
| student@edumy.com | Student@123 | Student |
| student2@edumy.com – student10@edumy.com | Student@123 | Student |

---

## 5. Database Connection

```
// appsettings.json
"ConnectionStrings": {
    "DefaultConnection": "Server=(localdb)\\mssqllocaldb;Database=EduMyDb;Trusted_Connection=True;TrustServerCertificate=True"
}

// docker-compose.yml (for container deployment)
"Server=sqlserver;Database=EduMyDb;User Id=sa;Password=EduMySuperSecurePassword123!;TrustServerCertificate=True"
```

### Migration History
8 migration files in `Migrations/` directory.

### Data Seeding
`DataSeeder.Initialize()` runs on startup (`Program.cs`):
1. Applies pending migrations
2. Checks if `admin@edumy.com` exists → if yes, skips
3. If DB is in dirty state → deletes and recreates
4. Seeds: Roles → Users → Categories → Coupons → Courses → Sections/Lessons/Quizzes → Enrollments/Reviews/Orders

---

## 6. Frontend-to-Backend API Mapping

| Frontend Action | React File | API Call | Backend Controller | DB Tables |
|---|---|---|---|---|
| Load homepage | `HomePage.jsx` | GET `/categories`, GET `/courses?...`, GET `/courses/recommend` | CategoriesController, CoursesController | Categories, Courses, Enrollments |
| Search courses | `CourseList.jsx` | GET `/courses?search=X` | CoursesController | Courses |
| View course | `CourseDetail.jsx` | GET `/courses/{id}`, GET `/orders/my-orders`, GET `/wishlist/check/{id}` | CoursesController, OrdersController, WishlistController | Courses, Orders, Wishlists |
| Submit review | `CourseDetail.jsx` | POST `/courses/{id}/reviews` | CoursesController | Reviews + ML sentiment |
| Add to cart | `CourseDetail.jsx` | POST `/cart/add/{id}` | CartController | CartItems |
| Checkout | `Cart.jsx` | POST `/orders/checkout` | OrdersController | Orders, OrderItems, Enrollments |
| View learning | `MyLearning.jsx` | GET `/courses/enrolled` | CoursesController | Enrollments, Courses |
| Complete lesson | `CoursePlayer.jsx` | POST `/courses/{id}/lessons/{lid}/complete` | CoursesController | LessonProgresses |
| Create course | `CourseCreate.jsx` | POST `/courses` | CoursesController | Courses + ML classify |
| Instructor stats | `InstructorDashboard.jsx` | GET `/courses/my-courses`, GET `/instructor/stats` | CoursesController, InstructorController | Multiple tables |
| Admin overview | `AdminDashboard.jsx` | GET `/admin/stats`, GET `/admin/courses`, GET `/admin/users`, GET `/admin/ml-monitoring` | AdminController | Multiple tables |
| Login | `Login.jsx` | POST `/api/auth/login` | AuthController | Users |
| Register | `Register.jsx` | POST `/api/auth/register` | AuthController | Users, UserRoles |
