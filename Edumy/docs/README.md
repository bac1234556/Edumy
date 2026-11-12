# 📚 Tài Liệu Kỹ Thuật Hệ Thống EduMy (Documentation Index)

Thư mục tài liệu của EduMy được tổ chức khoa học theo từng phân hệ chức năng:

```text
docs/
├── README.md                  # Mục lục điều hướng chính (file này)
├── design-system/             # Quy chuẩn thiết kế giao diện
│   └── MASTER.md              # Design system tokens, bảng màu, typography, UI rules
├── system/                    # Tài liệu kiến trúc & hệ sinh thái Web/Backend
│   ├── API_SPEC.md            # Đặc tả API các endpoint chính
│   ├── CURRENT_SYSTEM_AUDIT.md# Báo cáo đánh giá hệ thống hiện tại
│   ├── DATA_FLOW_API_MAP.md   # Bản đồ luồng dữ liệu giữa các dịch vụ
│   ├── IMPLEMENTATION_*.md    # Kế hoạch & danh mục triển khai
│   ├── RISK_ANALYSIS.md       # Đánh giá rủi ro hệ thống
│   ├── TEST_REPORT.md         # Báo cáo kiểm thử tích hợp
│   ├── WEB_ML_BOUNDARY.md     # Ranh giới tích hợp Web App & ML Microservice
│   └── walkthrough.md         # Hướng dẫn chi tiết sử dụng & kiểm thử luồng nghiệp vụ
└── ml/                        # Tài liệu phân hệ Trí tuệ nhân tạo (Machine Learning)
    ├── classification/        # Mô hình phân loại danh mục khóa học (Course Classification)
    │   ├── COURSE_CLASSIFICATION_API.md
    │   ├── COURSE_MODEL_DESIGN.md
    │   ├── COURSE_TRAINING_REPORT.md
    │   └── ...
    ├── recommendation/        # Mô hình gợi ý khóa học cá nhân hóa (Recommendation - OULAD)
    │   ├── RECOMMENDATION_API.md
    │   ├── RECOMMENDATION_MODEL_DESIGN.md
    │   ├── RECOMMENDATION_TRAINING_REPORT.md
    │   └── ...
    ├── sentiment/             # Mô hình phân tích cảm xúc bình luận (Sentiment Analysis)
    │   ├── SENTIMENT_DESIGN.md
    │   ├── SENTIMENT_PIPELINE.md
    │   ├── SENTIMENT_REVIEW.md
    │   └── THESIS_SENTIMENT_SUMMARY.md
    └── general/               # Báo cáo tổng quan, ma trận kiểm toán & quản trị mô hình
        ├── MODEL_ARCHITECTURE.md
        ├── MODEL_CARD.md
        ├── MODEL_VERSION.md
        ├── ML_MASTER_AUDIT_REPORT.md
        ├── ML_PRODUCTION_MAPPING.md
        └── ...
```

---

## 🧭 Hướng Dẫn Truy Cập Nhanh

| Phân hệ | Thư mục | Nội dung chính |
|---|---|---|
| **Hệ thống & API** | [`system/`](system/) | Đặc tả API, ranh giới tích hợp, báo cáo rà soát hệ thống, hướng dẫn kiểm thử |
| **Giao diện & UI Tokens** | [`design-system/`](design-system/) | Hệ thống Design System, Token màu sắc, bo góc, bóng đổ chuẩn |
| **ML Phân loại** | [`ml/classification/`](ml/classification/) | Huấn luyện & đánh giá mô hình phân loại danh mục khóa học |
| **ML Gợi ý** | [`ml/recommendation/`](ml/recommendation/) | Thuật toán gợi ý khóa học thông minh dựa trên hành vi học tập |
| **ML Phân tích cảm xúc** | [`ml/sentiment/`](ml/sentiment/) | Pipeline xử lý ngôn ngữ tự nhiên phân loại đánh giá tích cực/tiêu cực |
| **ML Tổng quan & MLOps** | [`ml/general/`](ml/general/) | Model Cards, quy trình triển khai FastAPI, báo cáo tái lập kết quả |
