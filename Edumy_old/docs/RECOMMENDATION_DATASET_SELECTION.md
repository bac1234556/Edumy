# Dataset Selection — Course Recommendation

This document specifies the selection of the offline dataset for the Course Recommendation task.

---

## 1. Selected Dataset: OULAD
- **Full Name**: Open University Learning Analytics Dataset (OULAD).
- **Publisher**: Open University (UK).
- **Source/URL**: https://analyse.kmi.open.ac.uk/resources/open_university_learning_analytics_dataset
- **License**: CC BY 4.0.
- **Scope**: Contains demographics, student registration, and interaction logs (clicks on virtual learning environment) for 32,593 students across 22 module presentations.

---

## 2. Table Specifications & Dimensions

OULAD contains several interrelated tables:
- **`studentInfo.csv`**: Demographics of the 32,593 unique students.
- **`courses.csv`**: List of modules (`code_module`) and presentations (`code_presentation`).
- **`studentRegistration.csv`**: Registration timestamps.
- **`studentVle.csv`**: **Interaction logs** containing 10.6 million rows of implicit clicks (`sum_click`) by student ID, material ID, and day.

---

## 3. Scope Limitation
- **Domain Mismatch Warning**: OULAD items represent academic modules (e.g. `AAA`, `BBB`, `CCC`) which do not correspond to the auto-increment integer IDs in the EduMy SQL Server database (`CourseId` 1, 2, 3, etc.).
- **Evaluation Constraint**: OULAD is used strictly for **offline evaluation** to compare algorithms. The production model must be trained directly on actual database interaction tables (`Enrollments`, `Reviews`, `UserActivities`) to suggest real course IDs.
