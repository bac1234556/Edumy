using EduMy.Backend.Models;
using Microsoft.EntityFrameworkCore;
using System;
using System.Collections.Generic;
using System.Linq;

namespace EduMy.Backend.Data
{
    public static class DataSeeder
    {
        public static void Initialize(IServiceProvider serviceProvider)
        {
            using (var context = new ApplicationDbContext(
                serviceProvider.GetRequiredService<DbContextOptions<ApplicationDbContext>>()))
            {
                try
                {
                    ExecuteMigrationAndSeed(context);
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"DB in inconsistent/dirty state, resetting DB: {ex.Message}");
                    try
                    {
                        context.ChangeTracker.Clear(); // Clear tracked entities to avoid tracking conflicts from failed run
                        context.Database.EnsureDeleted();
                        context.Database.Migrate();
                        ExecuteMigrationAndSeed(context);
                    }
                    catch (Exception exInner)
                    {
                        Console.WriteLine($"Critical error resetting DB: {exInner.Message}");
                        throw;
                    }
                }
            }
        }

        private static void ExecuteMigrationAndSeed(ApplicationDbContext context)
        {
            // Apply pending migrations
            context.Database.Migrate();

            if (context.Users.Any(u => u.Email == "admin@edumy.com"))
            {
                // Database has already been initialized
                return;
            }

            // Clean existing data in seed tables safely
            try
            {
                context.UserActivities.RemoveRange(context.UserActivities);
                context.SearchHistories.RemoveRange(context.SearchHistories);
                context.QuizAttemptAnswers.RemoveRange(context.QuizAttemptAnswers);
                context.QuizAttempts.RemoveRange(context.QuizAttempts);
                context.Answers.RemoveRange(context.Answers);
                context.Questions.RemoveRange(context.Questions);
                context.Quizzes.RemoveRange(context.Quizzes);
                context.LessonProgresses.RemoveRange(context.LessonProgresses);
                context.Certificates.RemoveRange(context.Certificates);
                context.CourseCoupons.RemoveRange(context.CourseCoupons);
                context.Wishlists.RemoveRange(context.Wishlists);
                context.OrderItems.RemoveRange(context.OrderItems);
                context.Orders.RemoveRange(context.Orders);
                context.Reviews.RemoveRange(context.Reviews);
                context.Enrollments.RemoveRange(context.Enrollments);
                context.Lessons.RemoveRange(context.Lessons);
                context.CourseSections.RemoveRange(context.CourseSections);
                context.CourseTags.RemoveRange(context.CourseTags);
                context.Tags.RemoveRange(context.Tags);
                context.CourseMlAnalysisTags.RemoveRange(context.CourseMlAnalysisTags);
                context.CourseMlAnalyses.RemoveRange(context.CourseMlAnalyses);
                context.CartItems.RemoveRange(context.CartItems);
                context.Carts.RemoveRange(context.Carts);
                context.UserRoles.RemoveRange(context.UserRoles);
                context.Users.RemoveRange(context.Users);
                context.Roles.RemoveRange(context.Roles);
                context.Categories.RemoveRange(context.Categories);
                context.Coupons.RemoveRange(context.Coupons);
                context.Courses.RemoveRange(context.Courses);
                context.SaveChanges();
            }
            catch (Exception cleanEx)
            {
                Console.WriteLine($"Ignored clean tables error: {cleanEx.Message}");
            }

                // --- 1. SEED ROLES ---
                var roles = new Role[]
                {
                    new Role { Name = "Admin" },
                    new Role { Name = "Instructor" },
                    new Role { Name = "Student" }
                };
                context.Roles.AddRange(roles);
                context.SaveChanges();

                var adminRole = context.Roles.Single(r => r.Name == "Admin");
                var instructorRole = context.Roles.Single(r => r.Name == "Instructor");
                var studentRole = context.Roles.Single(r => r.Name == "Student");

                // --- 2. SEED USERS ---
                // 1 Admin
                var admin = new User
                {
                    Email = "admin@edumy.com",
                    PasswordHash = BCrypt.Net.BCrypt.HashPassword("Admin@123"),
                    FullName = "System Admin",
                    IsActive = true,
                    Role = "Admin"
                };
                context.Users.Add(admin);

                // 3 Instructors
                var instructors = new List<User>
                {
                    new User { Email = "instructor@edumy.com", PasswordHash = BCrypt.Net.BCrypt.HashPassword("Instructor@123"), FullName = "Dr. John Doe", IsActive = true, Role = "Instructor", Bio = "Senior Software Architect with 15+ years of industry experience." },
                    new User { Email = "instructor2@edumy.com", PasswordHash = BCrypt.Net.BCrypt.HashPassword("Instructor@123"), FullName = "Jane Miller", IsActive = true, Role = "Instructor", Bio = "Marketing Director & Brand Consultant specialized in growth hacking." },
                    new User { Email = "instructor3@edumy.com", PasswordHash = BCrypt.Net.BCrypt.HashPassword("Instructor@123"), FullName = "Bob Smith", IsActive = true, Role = "Instructor", Bio = "Creative Director, UX designer & digital painter." }
                };
                context.Users.AddRange(instructors);

                // 10 Students
                var students = new List<User>();
                for (int i = 1; i <= 10; i++)
                {
                    students.Add(new User
                    {
                        Email = i == 1 ? "student@edumy.com" : $"student{i}@edumy.com",
                        PasswordHash = BCrypt.Net.BCrypt.HashPassword("Student@123"),
                        FullName = $"Student Learner {i}",
                        IsActive = true,
                        Role = "Student"
                    });
                }
                context.Users.AddRange(students);
                context.SaveChanges();

                // Assign UserRoles
                context.UserRoles.Add(new UserRole { UserId = admin.UserId, RoleId = adminRole.Id });
                foreach (var inst in instructors)
                {
                    context.UserRoles.Add(new UserRole { UserId = inst.UserId, RoleId = instructorRole.Id });
                }
                foreach (var stud in students)
                {
                    context.UserRoles.Add(new UserRole { UserId = stud.UserId, RoleId = studentRole.Id });
                }
                context.SaveChanges();

                // --- 3. SEED CATEGORIES ---
                var categoriesList = new List<Category>
                {
                    new Category { Name = "Development" },
                    new Category { Name = "Business" },
                    new Category { Name = "Design" },
                    new Category { Name = "Marketing" },
                    new Category { Name = "IT & Software" },
                    new Category { Name = "Office Productivity" },
                    new Category { Name = "Personal Development" },
                    new Category { Name = "Photography" }
                };
                context.Categories.AddRange(categoriesList);
                context.SaveChanges();

                // --- 4. SEED COUPONS ---
                var coupons = new List<Coupon>
                {
                    new Coupon { Code = "SUMMER20", DiscountPercentage = 20, ExpiryDate = DateTime.UtcNow.AddMonths(2), IsActive = true },
                    new Coupon { Code = "EDUMYNEW", DiscountPercentage = 10, ExpiryDate = DateTime.UtcNow.AddMonths(6), IsActive = true }
                };
                context.Coupons.AddRange(coupons);
                context.SaveChanges();

                // --- 5. SEED COURSES (At least 20) ---
                var coursesData = new List<(string Title, string Subtitle, string Desc, string Level, decimal Price, string CatName, User Inst, string Thumb)>
                {
                    // Development
                    ("ASP.NET Core Web API Mastery", "Build enterprise REST APIs from scratch", "Learn Clean Architecture, EF Core, JWT, and custom middleware.", "Intermediate", 49.99m, "Development", instructors[0], "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800&q=80"),
                    ("React 19 & TypeScript Complete Guide", "Modern frontend development", "Master Hooks, Context, TanStack Query, and Framer Motion.", "Beginner", 39.99m, "Development", instructors[0], "https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=800&q=80"),
                    ("Data Structures & Algorithms in C#", "Crack the coding interview", "Detailed implementation of arrays, lists, trees, graphs, and search/sort algorithms.", "Advanced", 59.99m, "Development", instructors[0], "https://images.unsplash.com/photo-1526379095098-d400fd0bfce8?w=800&q=80"),
                    ("Python Pro Bootcamp", "Go from zero to hero in Python", "Build 100 projects in 100 days. Automation, web scraping, and data science.", "Beginner", 29.99m, "Development", instructors[0], "https://images.unsplash.com/photo-1515879218367-8466d910aaa4?w=800&q=80"),

                    // Business
                    ("MBA in a Box: Business Fundamentals", "Everything you need to know about corporate strategy", "Covers finance, marketing, strategy, and project management.", "Beginner", 99.99m, "Business", instructors[1], "https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?w=800&q=80"),
                    ("Financial Analysis & Modeling", "Excel modeling for valuation & corporate finance", "Build DCF models, learn financial statements, and valuation techniques.", "Advanced", 79.99m, "Business", instructors[1], "https://images.unsplash.com/photo-1590283603385-17ffb3a7f29f?w=800&q=80"),
                    ("Product Management A-Z", "Launch successful digital products", "Covers roadmap planning, Scrum, agile methodology, and product analytics.", "Intermediate", 49.99m, "Business", instructors[1], "https://images.unsplash.com/photo-1531403009284-440f080d1e12?w=800&q=80"),

                    // Design
                    ("Figma UI/UX Design Essentials", "Design beautiful web and mobile apps", "Master Figma components, auto layout, and responsive layouts.", "Beginner", 34.99m, "Design", instructors[2], "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&q=80"),
                    ("Adobe Photoshop Advanced Course", "Digital art, photo editing and manipulation", "Master masks, color grading, non-destructive editing, and graphics.", "Advanced", 44.99m, "Design", instructors[2], "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800&q=80"),
                    ("Design System Design: From Scratch", "Scale visual designs with Figma tokens", "Learn how to build reusable variables, typography scales, and component guidelines.", "Intermediate", 54.99m, "Design", instructors[2], "https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?w=800&q=80"),

                    // Marketing
                    ("Digital Marketing Strategy Masterclass", "Grow brands using SEO, Ads, and Social Media", "Detailed walkthrough of Google Analytics, Facebook Ads, and email funnels.", "Beginner", 24.99m, "Marketing", instructors[1], "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=800&q=80"),
                    ("SEO Blueprint: Rank #1 on Google", "Practical search engine optimization tricks", "Covers keyword research, on-page optimization, backlink strategies.", "Intermediate", 29.99m, "Marketing", instructors[1], "https://images.unsplash.com/photo-1432888622747-4eb9a8efeb07?w=800&q=80"),
                    ("Copywriting that Converts", "Write high-selling landing pages & ads", "Psychology-based copywriting methods, headlines, and calls to action.", "Beginner", 19.99m, "Marketing", instructors[1], "https://images.unsplash.com/photo-1455390582262-044cdead277a?w=800&q=80"),

                    // IT & Software
                    ("AWS Certified Solutions Architect", "Pass the SAA-C03 certification exam", "Covers EC2, S3, RDS, VPC, Lambda, IAM, and architectural patterns.", "Intermediate", 89.99m, "IT & Software", instructors[0], "https://images.unsplash.com/photo-1484417894907-623942c8ea29?w=800&q=80"),
                    ("Kubernetes & Docker Microservices", "Container orchestration for beginners", "Learn Docker containers, Kubernetes pods, deployments, services, and Helm charts.", "Intermediate", 64.99m, "IT & Software", instructors[0], "https://images.unsplash.com/photo-1607799279861-4dd421887fb3?w=800&q=80"),

                    // Office Productivity
                    ("Microsoft Excel: Advanced Formulas & Charts", "Formulas, Pivots, PowerQuery & VBA", "Master VLOOKUP, INDEX/MATCH, Pivot Tables, and automation macro tools.", "Intermediate", 19.99m, "Office Productivity", instructors[1], "https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=800&q=80"),

                    // Personal Development
                    ("Time Management & Productivity Secrets", "Double your output and regain focus", "Learn deep work strategies, habit building, and procrastination killers.", "Beginner", 14.99m, "Personal Development", instructors[2], "https://images.unsplash.com/photo-1506784983877-45594efa4cbe?w=800&q=80"),
                    ("Public Speaking & Presentation Skills", "Deliver memorable talks with confidence", "Master body language, voice modulation, and slide design basics.", "Beginner", 24.99m, "Personal Development", instructors[2], "https://images.unsplash.com/photo-1475721027785-f74eccf877e2?w=800&q=80"),

                    // Photography
                    ("Photography Masterclass: Guide to Cameras", "Take professional digital photographs", "Understand exposure triangle, aperture, shutter speed, ISO, and composition rules.", "Beginner", 39.99m, "Photography", instructors[2], "https://images.unsplash.com/photo-1452780212940-6f5c0d14d848?w=800&q=80"),
                    ("Lightroom & Portrait Photography Editing", "Color grade portraits like a pro", "Master tone curves, HSL sliders, masking tools, and portrait retouching.", "Intermediate", 29.99m, "Photography", instructors[2], "https://images.unsplash.com/photo-1542038784456-1ea8e935640e?w=800&q=80")
                };

                var courses = new List<Course>();
                int courseIndex = 1;
                foreach (var cData in coursesData)
                {
                    var category = context.Categories.First(cat => cat.Name == cData.CatName);
                    var slug = cData.Title.ToLower()
                        .Replace(" & ", "-")
                        .Replace(" ", "-")
                        .Replace(":", "-")
                        .Replace("--", "-");
                    slug = System.Text.RegularExpressions.Regex.Replace(slug, @"[^a-z0-9\-]", "");
                    slug += "-" + Guid.NewGuid().ToString("N")[..4];

                    var course = new Course
                    {
                        Title = cData.Title,
                        Description = string.IsNullOrEmpty(cData.Subtitle) ? cData.Desc : $"{cData.Subtitle}. {cData.Desc}",
                        Level = cData.Level,
                        Price = cData.Price,
                        ThumbnailUrl = cData.Thumb,
                        Slug = slug,
                        Status = "Published",
                        InstructorId = cData.Inst.UserId,
                        CategoryId = category.CategoryId,
                        AverageRating = 4.5, // Seed average rating
                        StudentCount = 0,
                        CreatedAt = DateTime.UtcNow.AddDays(-30 + courseIndex),
                        UpdatedAt = DateTime.UtcNow
                    };
                    courses.Add(course);
                    context.Courses.Add(course);
                    courseIndex++;
                }
                context.SaveChanges();

                // --- 6. SEED SECTIONS, LESSONS, AND QUIZZES ---
                foreach (var course in courses)
                {
                    // Create 2 Sections per course
                    var section1 = new CourseSection
                    {
                        Title = "Introduction & Core Concepts",
                        OrderIndex = 1,
                        CourseId = course.CourseId
                    };
                    var section2 = new CourseSection
                    {
                        Title = "Advanced Workflows & Applications",
                        OrderIndex = 2,
                        CourseId = course.CourseId
                    };
                    context.CourseSections.AddRange(section1, section2);
                    context.SaveChanges();

                    // Create 3 Lessons for Section 1
                    var lesson1_1 = new Lesson
                    {
                        Title = "Welcome & Course Overview",
                        VideoUrl = "/uploads/preview.mp4",
                        OrderIndex = 1,
                        Duration = 300, // 5 mins
                        IsPreview = true, // Free preview
                        SectionId = section1.SectionId
                    };
                    var lesson1_2 = new Lesson
                    {
                        Title = "Key Terminology & Setup Instructions",
                        VideoUrl = "/uploads/lesson2.mp4",
                        OrderIndex = 2,
                        Duration = 600, // 10 mins
                        IsPreview = false,
                        SectionId = section1.SectionId
                    };
                    var lesson1_3 = new Lesson
                    {
                        Title = "Hands-on Exercise: First Project Run",
                        VideoUrl = "/uploads/lesson3.mp4",
                        OrderIndex = 3,
                        Duration = 900, // 15 mins
                        IsPreview = false,
                        SectionId = section1.SectionId
                    };

                    // Create 2 Lessons for Section 2
                    var lesson2_1 = new Lesson
                    {
                        Title = "Deep Dive into Best Practices",
                        VideoUrl = "/uploads/lesson4.mp4",
                        OrderIndex = 1,
                        Duration = 1200, // 20 mins
                        IsPreview = false,
                        SectionId = section2.SectionId
                    };
                    var lesson2_2 = new Lesson
                    {
                        Title = "Closing Words & Certificate Requirements",
                        VideoUrl = "/uploads/lesson5.mp4",
                        OrderIndex = 2,
                        Duration = 450, // 7.5 mins
                        IsPreview = false,
                        SectionId = section2.SectionId
                    };

                    context.Lessons.AddRange(lesson1_1, lesson1_2, lesson1_3, lesson2_1, lesson2_2);
                    context.SaveChanges();

                    // Seed Quiz in Section 2
                    var quiz = new Quiz
                    {
                        Title = $"{course.Title} Assessment",
                        Description = "Test your core knowledge learned in this course.",
                        PassingScore = 60,
                        TimeLimitMinutes = 10,
                        CourseSectionId = section2.SectionId,
                        Questions = new List<Question>
                        {
                            new Question
                            {
                                Content = "Which level best matches this course material?",
                                Points = 1,
                                Answers = new List<Answer>
                                {
                                    new Answer { Content = course.Level, IsCorrect = true },
                                    new Answer { Content = "None of the above", IsCorrect = false }
                                }
                            }
                        }
                    };
                    context.Quizzes.Add(quiz);
                    context.SaveChanges();
                }

                // --- 7. SEED ENROLLMENTS, REVIEWS, AND ORDERS ---
                // We enroll students into courses to make the stats dashboard populated.
                var random = new Random();
                int orderCounter = 10001;

                // Let's enroll students in some courses
                for (int i = 0; i < 15; i++)
                {
                    var studentUser = students[i % students.Count];
                    var course = courses[i % courses.Count];

                    // Double check enrollment doesn't exist
                    var exists = context.Enrollments.Any(e => e.UserId == studentUser.UserId && e.CourseId == course.CourseId);
                    if (exists) continue;

                    // Create Order
                    var order = new Order
                    {
                        UserId = studentUser.UserId,
                        TotalAmount = course.Price,
                        Status = "Completed",
                        CreatedAt = DateTime.UtcNow.AddDays(-15 + (i % 5))
                    };
                    context.Orders.Add(order);
                    context.SaveChanges();

                    var orderItem = new OrderItem
                    {
                        OrderId = order.OrderId,
                        CourseId = course.CourseId,
                        Price = course.Price
                    };
                    context.OrderItems.Add(orderItem);

                    // Create Enrollment
                    var enrollment = new Enrollment
                    {
                        UserId = studentUser.UserId,
                        CourseId = course.CourseId,
                        EnrolledAt = DateTime.UtcNow.AddDays(-15 + (i % 5)),
                        ProgressPercentage = (i % 3) * 50 // Seeding 0%, 50%, 100% progress
                    };
                    context.Enrollments.Add(enrollment);
                    
                    course.StudentCount++;
                    context.Entry(course).State = EntityState.Modified;

                    // Create Review (from enrolled students)
                    var sentimentLabels = new[] { "Positive", "Neutral", "Negative" };
                    var sentimentScores = new[] { 0.95, 0.50, 0.15 };
                    var ratings = new[] { 5, 3, 1 };
                    
                    int index = i % 3; // mix positive, neutral, negative reviews
                    var comments = new[] {
                        "Outstanding content! The instructor is clear and explaining everything thoroughly.",
                        "Average course. Good basic info but lacking practical coding exercises.",
                        "Very disappointed. The audio quality is terrible and the instructor speaks too fast."
                    };

                    var review = new Review
                    {
                        UserId = studentUser.UserId,
                        CourseId = course.CourseId,
                        Rating = ratings[index],
                        Comment = comments[index],
                        SentimentLabel = sentimentLabels[index],
                        SentimentScore = sentimentScores[index],
                        CreatedAt = DateTime.UtcNow.AddDays(-10 + (i % 5))
                    };
                    context.Reviews.Add(review);
                }
                context.SaveChanges();

                // Re-calculate course average ratings based on reviews
                foreach (var course in context.Courses.Include(c => c.Reviews).ToList())
                {
                    if (course.Reviews.Any())
                    {
                        course.AverageRating = Math.Round(course.Reviews.Average(r => r.Rating), 1);
                        context.Entry(course).State = EntityState.Modified;
                    }
                }
                context.SaveChanges();
        }
    }
}
