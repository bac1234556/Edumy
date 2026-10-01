# TỔNG QUAN TRIỂN KHAI MACHINE LEARNING - EDUMY

## 1. Tổng quan
Hệ thống **EduMy** tích hợp trí tuệ nhân tạo (Machine Learning) để giải quyết 3 bài toán chính:
1. **Phân loại khóa học (Course Classification)**: Đề xuất danh mục và gắn thẻ khóa học tự động nhằm hỗ trợ Giảng viên biên soạn giáo trình và hỗ trợ Admin kiểm duyệt.
2. **Phân tích cảm xúc (Sentiment Analysis)**: Đánh giá cảm nhận của học viên thông qua các bài đánh giá (Reviews), hỗ trợ theo dõi mức độ hài lòng trên Dashboard của Giảng viên.
3. **Gợi ý khóa học (Recommendation System)**: Đề xuất danh sách khóa học phù hợp cho người dùng dựa trên lịch sử tương tác và mức độ phổ biến.

---

## 2. Kiến trúc hệ thống
Hệ thống tuân thủ thiết kế phân rã microservices và tích hợp E2E theo luồng:
```text
React (Frontend)
   │ (HTTP REST / Axios / Token JWT)
   ▼
ASP.NET Core (Backend / API Gateway)
   │ (HttpClient / Polly Resilience / Circuit Breaker)
   ▼
FastAPI (Inference Server / Python)
   ├── Sentiment Service (BiLSTM)
   ├── Course Classification Service (MLP)
   └── Recommendation Service (Popularity Baseline + Offline NeuMF/GMF)
   │
   ▼
SQL Server (Production Database / EF Core Hydration)
```

---

## 3. Danh sách tính năng ML đã triển khai

| Module | Model | Dataset | API Endpoint | Integration | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Sentiment Analysis** | BiLSTM (Embedding + BiLSTM) | PyCaret Amazon Reviews (20,000 mẫu) | `POST /sentiment/analyze` | Gọi từ C# `ReviewsController` và `MLTestController` | **IMPLEMENTED AND INTEGRATED** |
| **Course Classification** | TF-IDF + MLP (Dense Classifier) | Udemy Courses (3,678 mẫu) | `POST /classification/course` | Gọi từ C# `CoursesController` và `MLTestController` | **IMPLEMENTED AND INTEGRATED** |
| **Toxicity Moderation** | Rule-based (Keyword Filter) | Danh sách từ cấm | `POST /course/analyze-content` | Gọi từ C# `CoursesController` | **IMPLEMENTED AND INTEGRATED** |
| **Recommendation System** | Popularity Baseline (Offline NeuMF/GMF) | OULAD Student Logs (28,755 học viên) | `POST /recommendations` | Gọi từ C# `MLTestController` và Hydrate qua SQL | **IMPLEMENTED AND INTEGRATED** |

---

## 4. Sentiment Analysis

### 4.1 Mục tiêu
Tự động gắn nhãn cảm xúc (`Positive` hoặc `Negative`) cùng điểm tin cậy cho từng review của học viên, giúp tối ưu hóa công tác kiểm duyệt và thống kê trên dashboard.

### 4.2 Dataset
- **Nguồn**: PyCaret Amazon Reviews.
- **Số lượng mẫu**: 20,000 reviews sản phẩm thực tế.
- **Ngôn ngữ**: Tiếng Anh.
- **Nhãn**: 2 nhãn (`0: Negative`, `1: Positive`).
- **Data Split**: 70% Train, 15% Validation, 15% Test.

### 4.3 Tiền xử lý
- Chuẩn hóa chữ thường (lower casing).
- Loại bỏ các thẻ HTML và URL sử dụng Regex.
- Xóa bỏ các ký tự khoảng trắng thừa.
- Tokenization sử dụng `Keras Tokenizer` (Vocabulary size = 10,000, kí tự đặc biệt `<OOV>`).
- Padding & Truncating chuỗi văn bản về kích thước cố định `max_len = 100` (padding="post", truncating="post").

### 4.4 Kiến trúc BiLSTM
Mô hình Recurrent Neural Network với cấu trúc:
1. `Embedding Layer`: Kích thước vector 64 chiều.
2. `Bidirectional LSTM Layer`: 64 đơn vị LSTM mỗi hướng.
3. `Dense Layer 1`: 64 nodes, activation ReLU, Dropout = 0.5.
4. `Dense Layer 2 (Output)`: 1 node, activation Sigmoid.

### 4.5 Tham số mô hình
- **Tổng số tham số**: **714,369** tham số có thể huấn luyện (trainable parameters).
- **Loss**: `binary_crossentropy`.
- **Optimizer**: `adam`.

### 4.6 Metrics
- **Accuracy**: 0.9037 (90.37%) trên tập kiểm thử (test set).
- **Macro F1**: 0.8671 (86.71%).
- **Weighted F1**: 0.9036 (90.36%).

### 4.7 Artifacts
Lưu trữ tại `ml-training/artifacts/sentiment/`:
- `model.keras`: File lưu trữ trọng số và cấu trúc mạng nơ-ron.
- `tokenizer.joblib`: File lưu trạng thái bộ từ vựng Tokenizer.
- `metadata.json`: Lưu thông số kiến trúc mô hình.
- `metrics.json`: Báo cáo kết quả kiểm định chi tiết.
- `checksums.json`: Lưu mã SHA256 kiểm định toàn vẹn file.

### 4.8 API
- **FastAPI Endpoint**: `POST /sentiment/analyze`
  - Input: `{"text": "I love this course!"}`
  - Output: `{"label": "Positive", "score": 0.92306, "confidence": 0.92306, "modelVersion": "BiLSTM_v1"}`

### 4.9 Backend Integration
- Được tích hợp thông qua C# HttpClient trong `MachineLearningService.cs` gọi qua `/sentiment/analyze`.
- C# Controllers (`ReviewsController.cs`) gọi dịch vụ này để gắn nhãn sentiment tự động khi người học thêm mới một đánh giá vào hệ thống.

### 4.10 Frontend Integration
- Renders trực tiếp trên UI của trang chi tiết khóa học, các đánh giá tích cực hiển thị kèm badge phân loại màu xanh lá, các đánh giá tiêu cực hiển thị kèm badge đỏ giúp người quản trị dễ lọc.

### 4.11 Hạn chế
- **Domain Shift**: Do mô hình được huấn luyện trên dataset review hàng hóa thương mại điện tử (Amazon), khi áp dụng trực tiếp sang review học tập khoa học (courses) sẽ có độ lệch miền nhất định về mặt ngữ nghĩa (ví dụ: các thuật ngữ chuyên ngành học thuật bị nhận nhầm).

### 4.12 Trạng thái
- **IMPLEMENTED AND INTEGRATED**

---

## 5. Course Classification

### 5.1 Mục tiêu
Tự động gán danh mục khóa học (Development, Business, Design, Personal Development) từ Title và Description giảng viên nhập vào.

### 5.2 Dataset
- **Nguồn**: Udemy Courses Dataset.
- **Số lượng mẫu**: 3,678 khóa học.
- **Số danh mục**: 4 nhóm mục tiêu lớn.

### 5.3 TF-IDF
- **Cấu hình**: `ngram_range=(1, 2)`, `max_features=5000`, `min_df=2`, `max_df=0.9`, loại bỏ stop-words tiếng Anh.

### 5.4 Linear SVM
- Huấn luyện thuật toán SVM phân loại tuyến tính thông qua `CalibratedClassifierCV` (3-fold cross validation) để trích xuất xác suất tin cậy (Probability Calibration).

### 5.5 MLP
- Mạng nơ-ron đa tầng MLP:
  - Input layer: Kích thước 5,000 đặc trưng TF-IDF.
  - Hidden Layer 1: 256 nodes, Activation ReLU, Dropout = 0.3.
  - Hidden Layer 2: 64 nodes, Activation ReLU, Dropout = 0.3.
  - Output Layer: 4 nodes, Activation Softmax.

### 5.6 So sánh model
Được tính toán qua script `train_classification.py`:
- **Accuracy**: SVM (97.28%) vs. MLP (97.46%).
- **Macro F1**: SVM (97.15%) vs. MLP (97.30%).

### 5.7 Production model
- **MLP** được chọn làm mô hình chạy chính thức vì có điểm F-score vượt trội hơn.

### 5.8 Metrics
- **Accuracy**: 0.9746 (97.46%).
- **Macro F1**: 0.9730 (97.30%).

### 5.9 Artifacts
Lưu trữ tại `ml-training/artifacts/classification/`:
- `model.keras`: Lưu trữ mô hình mạng MLP Tensorflow.
- `tfidf_vectorizer.joblib`: Bộ chuyển đổi văn bản sang vector đặc trưng.
- `label_encoder.joblib`: Bộ ánh xạ chuỗi danh mục sang số nguyên.
- `metadata.json` & `metrics.json`: Chứa tham số kiến trúc và độ đo chi tiết.

### 5.10 API
- **FastAPI Endpoint**: `POST /classification/course`
  - Input: `{"title": "React Guide", "description": "Learn Frontend Development"}`
  - Output: `{"predictedCategory": "Development", "confidence": 0.9997, "confidenceAvailable": true, "modelType": "MLP", "modelVersion": "1.0.0"}`

### 5.11 Course workflow
Khi giảng viên lưu khóa học ở chế độ kiểm duyệt:
1. Trạng thái đặt là `Analyzing`.
2. Hệ thống gọi FastAPI để nhận phân loại và độ tin cậy.
3. **Phân luồng trạng thái**:
   - Nếu `confidence < 0.65` hoặc nội dung chứa toxic: Đặt trạng thái `NeedsReview` (Yêu cầu Admin duyệt thủ công).
   - Nếu `0.65 <= confidence < 0.85`: Trạng thái `NeedsReview` (Yêu cầu Giảng viên xác nhận lại danh mục gợi ý).
   - Nếu `confidence >= 0.85`: Tự động phê duyệt danh mục và chuyển khóa học sang `PendingApproval`.

### 5.12 Manual override
- Cả Giảng viên và Admin đều có quyền override ghi đè thủ công danh mục do AI phân loại nếu phát hiện sai lệch.

### 5.13 Trạng thái
- **IMPLEMENTED AND INTEGRATED**

---

## 6. Recommendation System

### 6.1 Mục tiêu
Gợi ý danh sách khóa học phù hợp nhất dựa trên lịch sử đăng ký của học viên.

### 6.2 Dataset OULAD
- **Nguồn**: Open University Learning Analytics Dataset (OULAD).
- **Số lượng users**: 28,755 học viên.
- **Số lượng items**: 7 module khóa học (`AAA` - `GGG`).
- **Interactions**: Lịch sử đăng ký khóa học của sinh viên.

### 6.3 Interaction processing
Dữ liệu được chuẩn hóa và mã hóa thông qua bộ mã hóa `LabelEncoder` nhằm chuyển đổi định dạng ID sinh viên và mã khóa học sang không gian chỉ mục số nguyên liên tục bắt đầu từ 0.

### 6.4 Popularity
Mô hình gợi ý theo độ phổ biến (lượng tương tác đăng ký nhiều nhất).
- **Trạng thái**: Đang làm mô hình chạy chính thức (Production Candidate) trong FastAPI.
- **Lý do**: Do danh mục khóa học trong dữ liệu huấn luyện quá hẹp (7 items), mô hình phổ biến đảm bảo tính an toàn cao và tránh được lỗi đề xuất lặp.

### 6.5 GMF
- Mô hình Generalized Matrix Factorization (tương tác tuyến tính giữa Latent Factors của User và Item). Được lưu dưới dạng `gmf_model.keras`.

### 6.6 NeuMF
- Neural Matrix Factorization kết hợp GMF và MLP đa tầng phi tuyến. Được lưu dưới dạng `neumf_model.keras`.

### 6.7 Model selection
- **Production**: Popularity.
- **Offline Evaluation**: GMF và NeuMF.

### 6.8 Metrics
Do tập sản phẩm hẹp (7 items), việc đánh giá bằng `HitRate@10` sẽ luôn ra `1.0` (tầm thường). Đội ngũ đã chuyển sang đánh giá bằng top-3 trên toàn bộ 28,755 học viên kiểm thử:
- **HitRate@1**: 0.0941
- **HitRate@3**: 0.5330
- **NDCG@3**: 0.3404
- **MRR**: 0.3545
- **Coverage**: 0.4286 (Đề xuất được 3/7 khóa học trong catalog).

### 6.9 Course ID mapping
Vì mô hình được huấn luyện dựa trên mã OULAD (`AAA` - `GGG`) nhưng database SQL Server lưu khóa học bằng mã tăng tự động (`1` - `7`), hệ thống cấu hình một lớp Mapping trung gian tại `MLService/config/recommendation_course_mapping.json`:
- `AAA` -> 1
- `BBB` -> 2
- `CCC` -> 3
- `DDD` -> 4
- `EEE` -> 5
- `FFF` -> 6
- `GGG` -> 7

Hệ thống tải dịch vụ này một lần duy nhất lên RAM khi khởi động. Mọi item không nằm trong danh sách map được bỏ qua và ghi log cảnh báo thay vì làm sập ứng dụng.

### 6.10 Response metadata
- **Response**: Trả về kèm trường `scoreType` để chỉ rõ ý nghĩa điểm số (`interaction_count` cho Popularity, hoặc `probability` nếu sử dụng mạng nơ-ron). Response mẫu:
  `{"modelVersion": "Popularity", "recommendations": [{"courseId": "3", "score": 137, "scoreType": "interaction_count"}]}`

### 6.11 Cold-start
- Đối với sinh viên mới chưa có lịch sử, hệ thống tự động trả về danh sách các khóa học phổ biến nhất được xếp hạng theo lượng người đăng ký thô.

### 6.12 Artifacts
Lưu trữ tại `ml-training/artifacts/recommendation/`:
- `model.json`: Lưu trữ điểm số đếm thô của Popularity.
- `neumf_model.keras` & `gmf_model.keras`: Các mô hình nơ-ron sâu được đánh giá offline.
- `user_encoder.joblib` & `item_encoder.joblib`: Các bộ mã hóa chỉ mục.

### 6.13 API
- **FastAPI Endpoint**: `POST /recommendations`
  - Input: `{"userId": 1, "topK": 3}`
  - Output: `{"modelVersion": "Popularity", "recommendations": [{"courseId": "3", "score": 137.0, "scoreType": "interaction_count"}], "recommendationType": "popularity", "topK": 3}`

### 6.14 Backend and React integration
- C# Gateway (`MLTestController.cs`) gọi FastAPI `/recommendations` và trích xuất danh sách khóa học. Database sẽ lấy chi tiết khóa học thông qua `CourseId` tương ứng để render lên UI React dưới dạng Course Card.

### 6.15 Hạn chế
- **Catalog hẹp**: Cơ sở dữ liệu huấn luyện chỉ có 7 khóa học khiến hệ thống khuyến nghị chưa thể cá nhân hóa một cách sâu sắc cho từng người dùng.

### 6.16 Trạng thái
- **IMPLEMENTED AND INTEGRATED**

---

## 7. FastAPI Inference Layer
FastAPI chạy tại cổng 8000 và tải trước các artifact mô hình vào bộ nhớ đệm.

### Danh sách Endpoint thực tế

| Method | Endpoint | Input | Output | Trạng thái |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/recommendation/health` | Không có | Thông số tải mô hình và RAM | **ACTIVE (healthy)** |
| `POST` | `/recommendations` | `RecommendationRequest` | Danh sách CourseId kèm điểm số | **ACTIVE** |
| `POST` | `/sentiment/analyze` | `SentimentRequest` | Nhãn cảm xúc + Độ tin cậy | **ACTIVE** |
| `POST` | `/classification/course` | `ClassificationRequest` | Category + ModelType | **ACTIVE** |

---

## 8. ASP.NET Core Orchestration
Dịch vụ `MachineLearningService.cs` đóng vai trò client kết nối với FastAPI:
- **Resilience**: Tích hợp thông qua `HttpClient` có gắn cấu hình Timeout và xử lý lỗi ngắt mạch nếu FastAPI phản hồi chậm.
- **DTOs**: `SentimentResult`, `ClassificationResult`, `RecommendationResult` đảm bảo việc deserialize dữ liệu trơn tru.

---

## 9. React Integration
- **Reviews Widget**: Hiển thị badge Sentiment cho đánh giá.
- **Instructor Panel**: Đề xuất danh mục tự động dựa trên Title và Desc khi biên soạn khóa học mới.
- **Student Dashboard**: Render danh sách Course Cards được lấy từ API gợi ý của cổng kết nối.

---

## 10. Database Integration
Hệ thống lưu trữ lịch sử phân tích và tương tác tại SQL Server:
- `CourseMlAnalyses` & `CourseMlAnalysisTags`: Lưu vết lịch sử suy luận tự động của MLP Classifier.
- `Reviews`: Lưu cột `SentimentLabel` và `SentimentScore`.

---

## 11. Model Artifacts

| Module | Artifact | Path | Used by Service | Status |
| :--- | :--- | :--- | :--- | :--- |
| Sentiment | model.keras | `ml-training/artifacts/sentiment/model.keras` | FastAPI | **VALID** |
| Sentiment | tokenizer.joblib | `ml-training/artifacts/sentiment/tokenizer.joblib` | FastAPI | **VALID** |
| Classification | model.keras | `ml-training/artifacts/classification/model.keras` | FastAPI | **VALID** |
| Classification | tfidf_vectorizer.joblib | `ml-training/artifacts/classification/tfidf_vectorizer.joblib` | FastAPI | **VALID** |
| Recommendation | model.json | `ml-training/artifacts/recommendation/model.json` | FastAPI | **VALID** |

---

## 12. Test Evidence

| Test | Result | Evidence |
| :--- | :--- | :--- |
| **Unit Mapping Tests** | `PASS` | Chạy thành công 8/8 tests trong `test_recommendation_mapping.py` |
| **Vite Client Build** | `PASS` | Biên dịch thành công dự án React Frontend qua Vite |
| **E2E Integration** | `PASS` | Kiểm tra gọi E2E thành công từ ASP.NET sang FastAPI trả đúng định dạng và CourseId |

---

## 13. Metrics Summary

| Module | Model | Accuracy | Macro F1 |
| :--- | :--- | :--- | :--- |
| Sentiment | BiLSTM | 0.9037 | 0.8671 |
| Classification | MLP (Production) | 0.9746 | 0.9730 |
| Classification | SVM (Offline) | 0.9728 | 0.9715 |

---

## 14. Known Limitations
1. **Domain Shift (Sentiment)**: Độ lệch ngôn từ giữa đánh giá hàng hóa Amazon và đánh giá khóa học.
2. **Catalog kích thước nhỏ**: Catalog 7 items của OULAD giới hạn sự linh động của hệ thống khuyến nghị.
3. **Mock Password Recovery**: Tính năng quên mật khẩu ghi logs thay vì gửi email SMTP thực (đây là hạn chế trong môi trường Development).

---

## 15. Current Readiness

**READY FOR DEMO**

*Lý do*: Toàn bộ 3 bài toán ML đã được huấn luyện với các bộ chỉ số (accuracy, F1) thực tế, đóng gói thành artifact chuẩn, tích hợp end-to-end thông qua backend ASP.NET Core lên giao diện React Frontend và được kiểm chứng thành công bằng logs chạy thực tế.

---

## 16. Conclusion
Dự án EduMy đã hoàn thành xuất sắc việc xây dựng và tích hợp hệ thống trí tuệ nhân tạo toàn diện từ pha huấn luyện mô hình ngoại tuyến (offline training) đến pha triển khai ứng dụng trực tuyến phục vụ người dùng cuối (online serving) với độ tin cậy cao và cấu trúc mã nguồn tường minh.
