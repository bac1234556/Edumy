# Web-ML Boundary — EduMy

This document defines the clear boundary and separation of concerns between the Web application development domain (Frontend/Backend/Database), the Machine Learning/Data Mining domain (Datasets/Notebooks/Training/Evaluation), and the Integration layer connecting them.

---

## 1. Boundary Classification

### A. Web Application Domain (Lập trình Web)
This domain encompasses the standard web application lifecycle, visual user interface, data persistence, business logic, routing, security configurations, and API controllers.
- **React UI**: All views in the `Frontend/src/pages/` and `Frontend/src/components/` directories, constructed using React 19, custom CSS, and Lucide icons.
- **Routing**: Client-side routing configured in [App.jsx](file:///e:/Edumy%20%281%29/Edumy/Edumy/Frontend/src/App.jsx) via `react-router-dom`.
- **Authentication**: JWT token base64 parsing in [AuthContext.jsx](file:///e:/Edumy%20%281%29/Edumy/Edumy/Frontend/src/context/AuthContext.jsx) and request authorization interception in [axiosConfig.js](file:///e:/Edumy%20%281%29/Edumy/Edumy/Frontend/src/api/axiosConfig.js).
- **Authorization**: Role-based access validation (Admin, Instructor, Student) managed through the `[Authorize(Roles = "...")]` attributes on backend controllers (e.g., [AdminController.cs](file:///e:/Edumy%20%281%29/Edumy/Edumy/Backend/Controllers/AdminController.cs)) and protected routes on the frontend via [ProtectedRoute.jsx](file:///e:/Edumy%20%281%29/Edumy/Edumy/Frontend/src/components/ProtectedRoute.jsx).
- **CRUD Operations**: Model CRUD logic in controllers (e.g., [CoursesController.cs](file:///e:/Edumy%20%281%29/Edumy/Edumy/Backend/Controllers/CoursesController.cs)).
- **ASP.NET Core API**: The entire REST API architecture located in the `Backend/` directory, exposing endpoints for shopping carts, wishlists, orders, and authentication.
- **SQL Server Database**: Structured relational data storage defined in [ApplicationDbContext.cs](file:///e:/Edumy%20%281%29/Edumy/Edumy/Backend/Data/ApplicationDbContext.cs) and seeded by [DataSeeder.cs](file:///e:/Edumy%20%281%29/Edumy/Edumy/Backend/Data/DataSeeder.cs).
- **Enrollment Flow**: Adding courses to cart, mocking payment via [MockPaymentGateway.jsx](file:///e:/Edumy%20%281%29/Edumy/Edumy/Frontend/src/pages/MockPaymentGateway.jsx), creating database records in `Orders` / `OrderItems`, and linking students via `Enrollments`.
- **Course Management**: Instructor interface to create draft courses ([CourseCreate.jsx](file:///e:/Edumy%20%281%29/Edumy/Edumy/Frontend/src/pages/CourseCreate.jsx)) and manage curricula.
- **Review Management**: Student review creation in [ReviewsController.cs](file:///e:/Edumy%20%281%29/Edumy/Edumy/Backend/Controllers/ReviewsController.cs) and storage in `Reviews` table.
- **Dashboard Interfaces**: Analytics views for instructors ([InstructorDashboard.jsx](file:///e:/Edumy%20%281%29/Edumy/Edumy/Frontend/src/pages/InstructorDashboard.jsx)) and admin moderation stats ([AdminDashboard.jsx](file:///e:/Edumy%20%281%29/Edumy/Edumy/Frontend/src/pages/AdminDashboard.jsx)).
- **HTTP Integration**: Outgoing HTTP client configurations with Polly resilience (retry and circuit breaker) in [Program.cs](file:///e:/Edumy%20%281%29/Edumy/Edumy/Backend/Program.cs).
- **Error Handling**: Catching system errors globally using the custom exception middleware [ExceptionMiddleware.cs](file:///e:/Edumy%20%281%29/Edumy/Edumy/Backend/Middlewares/ExceptionMiddleware.cs).
- **Deployment Orchestration**: Standard container definition for SQL Server, ASP.NET Core Backend, and React Frontend in [docker-compose.yml](file:///e:/Edumy%20%281%29/Edumy/Edumy/docker-compose.yml).

### B. Machine Learning / Data Mining Domain (Học máy & Khai phá dữ liệu)
This domain encompasses dataset extraction, exploratory data analysis (EDA), text preprocessing, model training pipelines, metric evaluation, and output serialization.
- **Dataset**: Kaggle Udemy Courses dataset for classification, education review datasets for sentiment, and student interaction logs (OULAD) for recommendation.
- **Exploratory Data Analysis (EDA)**: Understanding data distributions, word counts, and class balances.
- **Data Cleaning**: Lowercasing, removing special characters/numbers, and handling null entries in notebook preprocessing pipelines.
- **Feature Engineering**: Transforming cleaned text variables into numerical vectors using algorithms like TF-IDF or text embeddings.
- **Tokenization & Padding**: Generating word indexes using Keras Tokenizer and formatting sequences via `pad_sequences` for neural network compatibility (LSTM).
- **TF-IDF Vectorization**: Fitting `TfidfVectorizer` to capture title characteristics for course categorization.
- **Negative Sampling**: Generating unobserved user-item interactions to train binary classification layers in recommendation systems.
- **Train/Validation/Test Split**: Partitioning data split ratios (e.g., 70/15/15) to validate model performance without data leakage.
- **Model Training**: Executing deep learning optimization steps (Adam, gradient updates) for LSTM, MLP, GMF, and NeuMF structures.
- **Model Evaluation**: Computing validation loss, accuracy, precision, recall, confusion matrices, and ranking indicators (Hit Rate @ K, NDCG @ K).
- **Model Comparison**: Benchmarking MLP vs. Linear SVM for classification, or NeuMF vs. GMF/MLP/Popularity baselines for recommendation.
- **Error Analysis**: Manually inspecting false positives and false negatives to identify model classification deficiencies.
- **Exporting Model Artifacts**: Saving completed weights to file types like `.keras`, `.pkl`, and `.h5`.

### C. Integration Domain (Tích hợp Web-ML)
The glue layer connecting the Web and ML domains, ensuring seamless data exchange and fallback safety.
- **FastAPI Inference Service**: The API server running in `MLService/main.py` exposing inference endpoints to the backend.
- **ASP.NET Core ML Client**: The service class [MachineLearningService.cs](file:///e:/Edumy%20%281%29/Edumy/Edumy/Backend/Services/MachineLearningService.cs) sending serialized HTTP requests.
- **JSON API Contracts**: Serialized JSON payloads containing text inputs and output labels/scores (e.g. `SentimentRequest`/`SentimentResponse`).
- **Model Loading**: Initializing the trained Keras/pickle artifacts into FastAPI server memory upon startup.
- **Prediction Request/Response Handling**: Passing inputs from controller requests through FastAPI and parsing predicted scores back into .NET data objects.
- **Prediction Database Persistence**: Saving ML outputs (confidence scores, toxicity checks, primary categories) inside dedicated columns and tables (`CourseMlAnalyses`, `Review.SentimentLabel`).
- **React ML Visualizations**: Rendered elements displaying "AI Match" badge ([HomePage.jsx](file:///e:/Edumy%20%281%29/Edumy/Edumy/Frontend/src/pages/HomePage.jsx)) and sentiment category flags ([CourseDetail.jsx](file:///e:/Edumy%20%281%29/Edumy/Edumy/Frontend/src/pages/CourseDetail.jsx)).
- **Graceful Fallbacks**: Implementing default baseline logic (e.g., neutral review sentiment, skip auto-categorization) when the FastAPI service throws exceptions.
- **Cold-Start Recommendation Handling**: Serving popular baseline recommendations to new or anonymous users who lack historical interactions.

---

## 2. Feature Responsibility Matrix

| Chức năng | Web | ML | Integration | File hiện tại | Trạng thái |
|:---|:---:|:---:|:---:|:---|:---|
| **Instructor tạo khóa học** | ✅ | ❌ | ❌ | [CourseCreate.jsx](file:///e:/Edumy%20%281%29/Edumy/Edumy/Frontend/src/pages/CourseCreate.jsx)<br>[CoursesController.cs](file:///e:/Edumy%20%281%29/Edumy/Edumy/Backend/Controllers/CoursesController.cs) | **WORKING** |
| **ML phân loại danh mục** | ❌ | ✅ | ✅ | [course_classification_mlp (1).ipynb](file:///e:/Edumy%20%281%29/Edumy/Edumy/docs/ML_MODEL_STATUS.md#ml-2-course-classification-mlp)<br>[main.py](file:///e:/Edumy%20%281%29/Edumy/Edumy/MLService/main.py#L142-L145) | **FALLBACK** (keyword-based dictionary) |
| **Student gửi review** | ✅ | ❌ | ❌ | [CourseDetail.jsx](file:///e:/Edumy%20%281%29/Edumy/Edumy/Frontend/src/pages/CourseDetail.jsx#L275-L304)<br>[ReviewsController.cs](file:///e:/Edumy%20%281%29/Edumy/Edumy/Backend/Controllers/ReviewsController.cs#L35-L71) | **WORKING** |
| **ML phân tích cảm xúc** | ❌ | ✅ | ✅ | [main.py](file:///e:/Edumy%20%281%29/Edumy/Edumy/MLService/main.py#L76-L91) | **FALLBACK** (basic positive/negative word search) |
| **Homepage recommendation** | ✅ | ✅ | ✅ | [HomePage.jsx](file:///e:/Edumy%20%281%29/Edumy/Edumy/Frontend/src/pages/HomePage.jsx#L72-L90)<br>[CoursesController.cs](file:///e:/Edumy%20%281%29/Edumy/Edumy/Backend/Controllers/CoursesController.cs#L31-L47) | **FALLBACK** (returns static `[1, 2, 3]`) |
| **Popularity fallback** | ✅ | ❌ | ✅ | [main.py](file:///e:/Edumy%20%281%29/Edumy/Edumy/MLService/main.py#L147-L151) | **HARDCODED** (returns static list) |
| **GMF branch** | ❌ | ✅ | ❌ | (None) | **MISSING** (to be trained in recommendation notebooks) |
| **NeuMF** | ❌ | ✅ | ❌ | (None) | **MISSING** (to be trained in recommendation notebooks) |
| **Admin monitoring** | ✅ | ❌ | ✅ | [AdminController.cs](file:///e:/Edumy%20%281%29/Edumy/Edumy/Backend/Controllers/AdminController.cs#L105-L144)<br>[AdminDashboard.jsx](file:///e:/Edumy%20%281%29/Edumy/Edumy/Frontend/src/pages/AdminDashboard.jsx) | **WORKING** (monitoring fallback DB entries) |
| **Lưu kết quả ML** | ✅ | ❌ | ✅ | [CourseMlAnalysis.cs](file:///e:/Edumy%20%281%29/Edumy/Edumy/Backend/Models/CourseMlAnalysis.cs)<br>[CoursesController.cs](file:///e:/Edumy%20%281%29/Edumy/Edumy/Backend/Controllers/CoursesController.cs#L510-L530) | **WORKING** (persists values into SQL DB) |
