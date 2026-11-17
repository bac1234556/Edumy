# 📚 Tài Liệu Kỹ Thuật Hệ Thống EduMy (Documentation Index)

Thư mục tài liệu của EduMy được tổ chức theo từng phân hệ chức năng:

```text
docs/
├── README.md                  # Mục lục điều hướng chính (file này)
├── design-system/             # Quy chuẩn thiết kế giao diện
│   └── MASTER.md              # Design system tokens, bảng màu, typography, UI rules
├── system/                    # Tài liệu kiến trúc & nghiệp vụ hệ thống hiện tại (Spring Modulith + React 19)
│   ├── API_SPEC.md            # Đặc tả hợp đồng API chuẩn (ApiResponse, Mobile/Web Auth, Payments, Payout, Q&A)
│   ├── CURRENT_SYSTEM_AUDIT.md# Đánh giá hiện trạng kiến trúc Spring Modulith, Frontend, DB & Observability
│   ├── DATA_FLOW_API_MAP.md   # Kiến trúc phân tầng, sơ đồ module & bản đồ toàn bộ endpoint
│   ├── TEST_REPORT.md         # Báo cáo kiểm thử đơn vị (ModularityTest, Identity, Payout) & tích hợp
│   ├── RISK_ANALYSIS.md       # Đánh giá rủi ro kỹ thuật & vận hành hiện tại
│   ├── WEB_ML_BOUNDARY.md     # Ranh giới kiến trúc Web/Mobile - Spring Modulith & ghi chú chuyển đổi từ ML
│   ├── walkthrough.md         # Hướng dẫn chi tiết kiến trúc, luồng nghiệp vụ & vận hành hệ thống
│   ├── IMPLEMENTATION_PLAN.md # Kế hoạch triển khai (lưu trữ lịch sử giai đoạn ML)
│   └── IMPLEMENTATION_CHECKLIST.md
└── ml/                        # [LƯU TRỮ LỊCH SỬ] Tài liệu nghiên cứu Machine Learning giai đoạn trước
    ├── classification/        # Tài liệu mô hình phân loại danh mục khóa học
    ├── recommendation/        # Tài liệu mô hình gợi ý (OULAD)
    ├── sentiment/             # Tài liệu mô hình phân tích cảm xúc
    └── general/               # Báo cáo tổng quan & kiểm toán ML giai đoạn trước
```

---

## 🧭 Hướng Dẫn Truy Cập Nhanh

| Phân hệ | Thư mục | Nội dung chính |
|---|---|---|
| **Hệ thống & Kiến trúc hiện tại** | [`system/`](system/) | Kiến trúc Spring Modulith (8 module), API Map, luồng nghiệp vụ, kiểm thử, vận hành Docker & Monitoring |
| **Giao diện & UI Tokens** | [`design-system/`](design-system/) | Hệ thống Design System, Token màu sắc, bo góc, bóng đổ chuẩn |
| **ML (Lưu trữ lịch sử)** | [`ml/`](ml/) | Tài liệu nghiên cứu & thử nghiệm ML từ kiến trúc cũ (hiện Backend đã chuyển sang Spring Modulith độc lập) |
