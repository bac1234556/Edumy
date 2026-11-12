# ML Production Course Mapping Documentation

This document describes the identity mapping strategy designed to align the offline recommendation system (trained on OULAD) with the production database (SQL Server) Course ID space.

## 1. Identity Space Mapping Table

The OULAD offline dataset contains 7 unique course modules. These modules are mapped to the auto-incrementing integer `CourseId` keys generated during database seeding in `DataSeeder.cs`.

| OULAD Item Code (modelItemId) | SQL CourseId | Course Title | Mapping Status |
|---|---|---|---|
| AAA | 1 | ASP.NET Core Web API Mastery | MAPPED |
| BBB | 2 | React 19 & TypeScript Complete Guide | MAPPED |
| CCC | 3 | Data Structures & Algorithms in C# | MAPPED |
| DDD | 4 | Python Pro Bootcamp | MAPPED |
| EEE | 5 | MBA in a Box: Business Fundamentals | MAPPED |
| FFF | 6 | Financial Analysis & Modeling | MAPPED |
| GGG | 7 | Product Management A-Z | MAPPED |

## 2. Configuration Schema

Mappings are externalized in `MLService/config/recommendation_course_mapping.json` for maintainability and validation:

```json
{
  "version": "1.0.0",
  "mappings": [
    { "modelItemId": "AAA", "courseId": 1 },
    { "modelItemId": "BBB", "courseId": 2 },
    { "modelItemId": "CCC", "courseId": 3 },
    { "modelItemId": "DDD", "courseId": 4 },
    { "modelItemId": "EEE", "courseId": 5 },
    { "modelItemId": "FFF", "courseId": 6 },
    { "modelItemId": "GGG", "courseId": 7 }
  ]
}
```

## 3. Mapping Service Logic

The translation is handled by `MLService/services/recommendation_mapping_service.py` which:
- Loads the mapping once on startup to prevent repeated I/O.
- Validates the uniqueness of model IDs and database IDs.
- Offers bidirectional translation (`get_course_id` and `get_model_item_id`).
- Skips unmapped courses and logs structured warnings rather than crashing or using placeholder values.
