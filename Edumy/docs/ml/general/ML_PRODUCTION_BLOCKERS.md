# Machine Learning Production Blockers

This document highlights critical issues preventing full production deployment.

---

## 1. CRITICAL: Identity Space Mismatch (OULAD Course Codes vs. SQL Server IDs)
- **Problem**: 
  - The recommendation pipeline is trained on the OULAD dataset. It takes string user/student IDs (`id_student`) and string course modules (`code_module`, e.g., `AAA`, `BBB`, `CCC`).
  - Consequently, the model outputs recommendations with string IDs (e.g., `CCC`, `DDD`).
  - However, in production, the ASP.NET Core database (`ApplicationDbContext`) has integer primary keys for courses (`CourseId`, e.g. `1`, `2`, `3`).
  - When the ASP.NET Core gateway (`MachineLearningService.cs` line 70) attempts to parse these IDs (`int.TryParse(r.CourseId, out int id)`), it fails because string course modules like `CCC` cannot be converted to integers.
  - This returns a list of zeros, which are filtered out. As a result, the recommendation service always returns an empty list `[]` to the React UI, completely breaking the end-to-end integration.
- **Required Fix**: 
  Implement a course mapping dictionary or database table linking OULAD codes (e.g., `CCC`) to SQL Server database `CourseId` primary keys (e.g., `3`) inside the MLService parser or ASP.NET Core gateway service.

---

## 2. HIGH: React Build Failure
- **Problem**: 
  Creating directory junctions (`mklink /j`) to work around NTFS file corruption causes Vite/Rollup compilation to fail since Vite restricts source index assets escaping the workspace root.
- **Required Fix**: 
  Copy the Frontend directory fully or adjust the Vite configuration `server.fs.allow` block.
