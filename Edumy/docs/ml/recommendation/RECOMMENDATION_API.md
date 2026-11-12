# API Specification — Course Recommendation

This document details the route endpoints, schemas, validation parameters, and error codes for the Course Recommendation service.

---

## 1. Recommendation predictions (FastAPI)
- **Endpoint**: `POST /recommendations`
- **Request Payload**:
  ```json
  {
      "userId": 123,
      "topK": 10
  }
  ```
- **Response Payload**:
  ```json
  {
      "modelVersion": "1.0.0",
      "trainingTimestamp": "2026-07-30T03:40:00Z",
      "recommendations": [
          {"courseId": "3", "score": 0.9812},
          {"courseId": "1", "score": 0.8543},
          {"courseId": "2", "score": 0.7231}
      ]
  }
  ```

---

## 2. Recommendation Endpoint (.NET Backend Gateway)
- **Endpoint**: `GET /api/courses/recommend`
- **Authorization**: Require `Student` role.
- **Response Payload**: Array of Course objects matching the recommended IDs, sorted by priority.
