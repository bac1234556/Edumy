# Manual Test Checklist — Course Classification

This document provides step-by-step test plans to verify end-to-end course category suggestions.

---

## Scenario 1: Suggestion Applied Successfully
1. **Action**: Log in as an Instructor.
2. **Action**: Go to the **Create Course** dashboard.
3. **Action**: Type Title: *"Complete React Developer in 2026"* and Description: *"Learn React hooks, state management, and Redux."*
4. **Action**: Click the **"Gợi ý danh mục bằng AI"** button.
5. **Expected Output**:
   - Button enters loading state ("Đang phân tích...").
   - Box renders: **Đề xuất AI: Web Development** (Confidence e.g. 94%).
   - Model Type: `LinearSVM (1.0.0)` is visible.
6. **Action**: Click **"Áp dụng đề xuất"**.
7. **Expected Output**:
   - The Category Select dropdown automatically selects **"Web Development"**.
8. **Action**: Submit form. Verify the course is saved with the chosen category.

---

## Scenario 2: User rejects prediction and selects manually
1. **Action**: Request category suggestion for *"Classical Guitar for Beginners"*.
2. **Expected Output**:
   - AI Suggestion displays: **Musical Instruments**.
3. **Action**: Instead of clicking "Áp dụng đề xuất", select **"Graphic Design"** manually in the dropdown.
4. **Action**: Click submit.
5. **Expected Output**:
   - Course is saved with **"Graphic Design"** as its category. AI suggestion does not overwrite user choice.

---

## Scenario 3: ML Service offline fallback
1. **Action**: Stop the FastAPI server.
2. **Action**: Open Create Course, fill in title and description, and click **"Gợi ý danh mục bằng AI"**.
3. **Expected Output**:
   - UI displays warning alert: *"Không thể gợi ý danh mục lúc này. Vui lòng chọn danh mục thủ công."*
   - Text boxes and form values remain intact.
   - User can manually select category and submit form successfully.
