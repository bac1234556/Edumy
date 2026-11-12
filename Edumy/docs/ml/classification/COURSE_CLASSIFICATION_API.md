# API Specification — Course Classification

This document details the route inputs, expected payloads, validation constraints, and error codes for the Course Classification service.

---

## 1. Classification prediction (FastAPI)
- **Endpoint**: `POST /classification/course`
- **Request Payload**:
  ```json
  {
      "title": "ASP.NET Core Web API Development",
      "description": "Build high-speed, secure REST APIs with C# and Entity Framework.",
      "tags": ["C#", "ASP.NET Core", "Web API"]
  }
  ```
- **Response Payload (Calibrated SVM)**:
  ```json
  {
      "predictedCategory": "Development",
      "confidence": 0.9412,
      "confidenceAvailable": true,
      "topPredictions": [
          {"category": "Development", "score": 0.9412},
          {"category": "Graphic Design", "score": 0.0321},
          {"category": "Business Finance", "score": 0.0152},
          {"category": "Musical Instruments", "score": 0.0115}
      ],
      "modelType": "LinearSVM",
      "modelVersion": "1.0.0"
  }
  ```
- **Response Payload (Degraded/Not Loaded)**:
  ```json
  {
      "predictedCategory": "",
      "confidence": null,
      "confidenceAvailable": false,
      "topPredictions": [],
      "modelType": "LinearSVM",
      "modelVersion": "1.0.0"
  }
  ```

---

## 2. Suggest Category Endpoint (.NET Backend Gateway)
- **Endpoint**: `POST /api/MLTest/course-classification`
- **Authorization**: Require `Instructor` or `Admin` roles.
- **Request Payload**:
  ```json
  {
      "title": "Course Title",
      "description": "Course Description"
  }
  ```
- **Response Payload (Success)**:
  ```json
  {
      "success": true,
      "suggestion": {
          "predictedCategory": "Development",
          "confidence": 0.9412,
          "confidenceAvailable": true,
          "topPredictions": [...],
          "modelType": "LinearSVM",
          "modelVersion": "1.0.0"
      }
  }
  ```
- **Response Payload (ML Unavailable Fallback)**:
  ```json
  {
      "success": false,
      "code": "ML_SERVICE_UNAVAILABLE",
      "message": "Không thể gợi ý danh mục lúc này. Vui lòng chọn danh mục thủ công."
  }
  ```
