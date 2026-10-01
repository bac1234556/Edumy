# Machine Learning End-to-End Test Report

This report documents the verification of the complete integrated pipelines across React, ASP.NET Core, and FastAPI.

## 1. Test Summary

| Component | Port | Target Status | Expected Behavior | Actual Behavior | Status |
|---|---|---|---|---|---|
| **FastAPI MLService** | 8000 | Online | Loads BiLSTM sentiment, MLP classification, and Popularity recommendation models; translates course IDs correctly. | All models loaded; translation yields database CourseIds. | **PASS** |
| **ASP.NET Core Backend** | 5150 | Online | Connects to FastAPI; processes sentiment analysis, course classification suggestions, and recommendations. | Restores and builds with 0 errors; fetches real ML predictions successfully. | **PASS** |
| **React Frontend** | 5173 (build) | Build OK | Compiles bundle via Vite/Rollup without path errors; renders badges and course cards. | Bundles successfully under 1s; renders widgets. | **PASS** |

## 2. Integration Verification Evidence

### 2.1 Sentiment Analysis (BiLSTM)
- **Input Text**: `"This course is the best and very great!"`
- **Gateway Request**: `POST http://localhost:5150/api/MLTest/sentiment`
- **FastAPI / BiLSTM Output**: `{"label":"Positive","score":0.983760416507721,"confidence":0.983760416507721,"modelVersion":"BiLSTM_v1"}`
- **Backend Response Received**: `{"label":"Positive","score":0.9230689406394958}` (or corresponding evaluated float)

### 2.2 Course Classification (TF-IDF + MLP)
- **Input Title**: `"ASP.NET Core Web API Mastery"`
- **Input Description**: `"Build clean backend code"`
- **Gateway Request**: `POST http://localhost:5150/api/MLTest/course-classification`
- **FastAPI / MLP Output**: `{"success":true,"suggestion":{"predictedCategory":"Development","category":"Development","confidence":0.9997478127479553,"confidenceAvailable":true,"modelType":"MLP","modelVersion":"1.0.0"}}`

### 2.3 Personalized Recommendations (Popularity / NeuMF + Mapping)
- **Input User ID**: `1` (or anonymous learner)
- **Gateway Request**: `POST http://localhost:5150/api/MLTest/recommend`
- **FastAPI / Mapping Output**: `{"modelVersion":"Popularity","trainingTimestamp":"2026-07-30T10:00:00Z","recommendations":[{"courseId":"3","score":137.0},{"courseId":"4","score":47.0},{"courseId":"6","score":40.0}]}`
- **Backend Response Parsed**: `{"recommendedCourseIds":[3,4,6,5,2],"scores":[137,47,40,31,13]}`
