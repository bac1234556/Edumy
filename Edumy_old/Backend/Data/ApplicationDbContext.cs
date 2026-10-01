using Microsoft.EntityFrameworkCore;
using EduMy.Backend.Models;

namespace EduMy.Backend.Data
{
    public class ApplicationDbContext : DbContext
    {
        public ApplicationDbContext(DbContextOptions<ApplicationDbContext> options)
            : base(options)
        {
        }

        public DbSet<User> Users { get; set; } = null!;
        public DbSet<Role> Roles { get; set; } = null!;
        public DbSet<UserRole> UserRoles { get; set; } = null!;
        public DbSet<RefreshToken> RefreshTokens { get; set; } = null!;
        public DbSet<Category> Categories { get; set; } = null!;
        public DbSet<Course> Courses { get; set; } = null!;
        public DbSet<CourseSection> CourseSections { get; set; } = null!;
        public DbSet<Lesson> Lessons { get; set; } = null!;
        public DbSet<Enrollment> Enrollments { get; set; } = null!;
        public DbSet<Review> Reviews { get; set; } = null!;
        public DbSet<Order> Orders { get; set; } = null!;
        public DbSet<OrderItem> OrderItems { get; set; } = null!;
        
        public DbSet<Cart> Carts { get; set; } = null!;
        public DbSet<CartItem> CartItems { get; set; } = null!;
        public DbSet<Wishlist> Wishlists { get; set; } = null!;
        public DbSet<Coupon> Coupons { get; set; } = null!;
        public DbSet<CourseCoupon> CourseCoupons { get; set; } = null!;
        public DbSet<Certificate> Certificates { get; set; } = null!;
        public DbSet<CourseMlAnalysis> CourseMlAnalyses { get; set; } = null!;
        public DbSet<CourseMlAnalysisTag> CourseMlAnalysisTags { get; set; } = null!;
        
        public DbSet<Tag> Tags { get; set; } = null!;
        public DbSet<CourseTag> CourseTags { get; set; } = null!;
        public DbSet<LessonProgress> LessonProgresses { get; set; } = null!;
        public DbSet<SearchHistory> SearchHistories { get; set; } = null!;
        public DbSet<UserActivity> UserActivities { get; set; } = null!;
        
        // Phase 12: Quizzes
        public DbSet<Quiz> Quizzes { get; set; } = null!;
        public DbSet<Question> Questions { get; set; } = null!;
        public DbSet<Answer> Answers { get; set; } = null!;
        public DbSet<QuizAttempt> QuizAttempts { get; set; } = null!;
        public DbSet<QuizAttemptAnswer> QuizAttemptAnswers { get; set; } = null!;

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            modelBuilder.Entity<CourseSection>()
                .HasKey(cs => cs.SectionId);

            // Configure Users
            modelBuilder.Entity<User>()
                .HasIndex(u => u.Email)
                .IsUnique();

            // Configure UserRoles
            modelBuilder.Entity<UserRole>()
                .HasKey(ur => new { ur.UserId, ur.RoleId });
                
            modelBuilder.Entity<UserRole>()
                .HasOne(ur => ur.User)
                .WithMany(u => u.UserRoles)
                .HasForeignKey(ur => ur.UserId);
                
            modelBuilder.Entity<UserRole>()
                .HasOne(ur => ur.Role)
                .WithMany(r => r.UserRoles)
                .HasForeignKey(ur => ur.RoleId);

            // Configure RefreshTokens
            modelBuilder.Entity<RefreshToken>()
                .HasOne(rt => rt.User)
                .WithMany(u => u.RefreshTokens)
                .HasForeignKey(rt => rt.UserId)
                .OnDelete(DeleteBehavior.Cascade);

            // Configure Course relationships
            modelBuilder.Entity<Course>()
                .HasOne(c => c.Instructor)
                .WithMany(u => u.Courses)
                .HasForeignKey(c => c.InstructorId)
                .OnDelete(DeleteBehavior.Restrict);

            modelBuilder.Entity<Course>()
                .HasOne(c => c.Category)
                .WithMany(cat => cat.Courses)
                .HasForeignKey(c => c.CategoryId)
                .OnDelete(DeleteBehavior.Restrict);

            // Configure Enrollment
            modelBuilder.Entity<Enrollment>()
                .HasIndex(e => new { e.UserId, e.CourseId })
                .IsUnique();
            
            modelBuilder.Entity<Enrollment>()
                .HasOne(e => e.User)
                .WithMany(u => u.Enrollments)
                .HasForeignKey(e => e.UserId)
                .OnDelete(DeleteBehavior.Cascade);

            modelBuilder.Entity<Enrollment>()
                .HasOne(e => e.Course)
                .WithMany(c => c.Enrollments)
                .HasForeignKey(e => e.CourseId)
                .OnDelete(DeleteBehavior.Cascade);

            // Configure Review
            modelBuilder.Entity<Review>()
                .HasOne(r => r.User)
                .WithMany(u => u.Reviews)
                .HasForeignKey(r => r.UserId)
                .OnDelete(DeleteBehavior.Cascade);

            modelBuilder.Entity<Review>()
                .HasOne(r => r.Course)
                .WithMany(c => c.Reviews)
                .HasForeignKey(r => r.CourseId)
                .OnDelete(DeleteBehavior.Cascade);

            // Configure Order and OrderItem
            modelBuilder.Entity<Order>()
                .HasOne(o => o.User)
                .WithMany(u => u.Orders)
                .HasForeignKey(o => o.UserId)
                .OnDelete(DeleteBehavior.Cascade);

            modelBuilder.Entity<OrderItem>()
                .HasOne(oi => oi.Order)
                .WithMany(o => o.OrderItems)
                .HasForeignKey(oi => oi.OrderId)
                .OnDelete(DeleteBehavior.Cascade);

            modelBuilder.Entity<OrderItem>()
                .HasOne(oi => oi.Course)
                .WithMany(c => c.OrderItems)
                .HasForeignKey(oi => oi.CourseId)
                .OnDelete(DeleteBehavior.Restrict);
                
            // Configure Cart and CartItem
            modelBuilder.Entity<Cart>()
                .HasOne(c => c.User)
                .WithOne(u => u.Cart)
                .HasForeignKey<Cart>(c => c.UserId)
                .OnDelete(DeleteBehavior.Cascade);
                
            modelBuilder.Entity<CartItem>()
                .HasOne(ci => ci.Cart)
                .WithMany(c => c.CartItems)
                .HasForeignKey(ci => ci.CartId)
                .OnDelete(DeleteBehavior.Cascade);
                
            modelBuilder.Entity<CartItem>()
                .HasOne(ci => ci.Course)
                .WithMany()
                .HasForeignKey(ci => ci.CourseId)
                .OnDelete(DeleteBehavior.Restrict);
                
            // Configure Wishlist
            modelBuilder.Entity<Wishlist>()
                .HasOne(w => w.User)
                .WithMany(u => u.Wishlists)
                .HasForeignKey(w => w.UserId)
                .OnDelete(DeleteBehavior.Cascade);
                
            modelBuilder.Entity<Wishlist>()
                .HasOne(w => w.Course)
                .WithMany(c => c.Wishlists)
                .HasForeignKey(w => w.CourseId)
                .OnDelete(DeleteBehavior.Cascade);
                
            // Configure CourseCoupon
            modelBuilder.Entity<CourseCoupon>()
                .HasKey(cc => new { cc.CourseId, cc.CouponId });
                
            modelBuilder.Entity<CourseCoupon>()
                .HasOne(cc => cc.Course)
                .WithMany(c => c.CourseCoupons)
                .HasForeignKey(cc => cc.CourseId);
                
            modelBuilder.Entity<CourseCoupon>()
                .HasOne(cc => cc.Coupon)
                .WithMany(c => c.CourseCoupons)
                .HasForeignKey(cc => cc.CouponId);
                
            // Configure Certificate
            modelBuilder.Entity<Certificate>()
                .HasOne(cert => cert.User)
                .WithMany(u => u.Certificates)
                .HasForeignKey(cert => cert.UserId)
                .OnDelete(DeleteBehavior.Cascade);
                
            modelBuilder.Entity<Certificate>()
                .HasOne(cert => cert.Course)
                .WithMany(c => c.Certificates)
                .HasForeignKey(cert => cert.CourseId)
                .OnDelete(DeleteBehavior.Restrict);
                
            // Configure CourseMlAnalysis and Tags
            modelBuilder.Entity<CourseMlAnalysis>()
                .HasOne(ma => ma.Course)
                .WithMany(c => c.MlAnalyses)
                .HasForeignKey(ma => ma.CourseId)
                .OnDelete(DeleteBehavior.Cascade);
                
            modelBuilder.Entity<CourseMlAnalysis>()
                .HasOne(ma => ma.ApprovedByUser)
                .WithMany()
                .HasForeignKey(ma => ma.ApprovedByUserId)
                .OnDelete(DeleteBehavior.SetNull);
                
            modelBuilder.Entity<CourseMlAnalysisTag>()
                .HasOne(mat => mat.CourseMlAnalysis)
                .WithMany(ma => ma.Tags)
                .HasForeignKey(mat => mat.CourseMlAnalysisId)
                .OnDelete(DeleteBehavior.Cascade);
                
            // Configure Category self-referencing
            modelBuilder.Entity<Category>()
                .HasOne(c => c.ParentCategory)
                .WithMany(c => c.SubCategories)
                .HasForeignKey(c => c.ParentCategoryId)
                .OnDelete(DeleteBehavior.Restrict);
                
            // Configure CourseTag
            modelBuilder.Entity<CourseTag>()
                .HasKey(ct => new { ct.CourseId, ct.TagId });
                
            modelBuilder.Entity<CourseTag>()
                .HasOne(ct => ct.Course)
                .WithMany(c => c.CourseTags)
                .HasForeignKey(ct => ct.CourseId)
                .OnDelete(DeleteBehavior.Cascade);
                
            modelBuilder.Entity<CourseTag>()
                .HasOne(ct => ct.Tag)
                .WithMany(t => t.CourseTags)
                .HasForeignKey(ct => ct.TagId)
                .OnDelete(DeleteBehavior.Cascade);
            modelBuilder.Entity<LessonProgress>()
                .HasIndex(lp => new { lp.UserId, lp.CourseId, lp.LessonId })
                .IsUnique();

            modelBuilder.Entity<LessonProgress>()
                .HasOne(lp => lp.User)
                .WithMany()
                .HasForeignKey(lp => lp.UserId)
                .OnDelete(DeleteBehavior.Cascade);

            modelBuilder.Entity<LessonProgress>()
                .HasOne(lp => lp.Course)
                .WithMany()
                .HasForeignKey(lp => lp.CourseId)
                .OnDelete(DeleteBehavior.Restrict);

            modelBuilder.Entity<LessonProgress>()
                .HasOne(lp => lp.Lesson)
                .WithMany()
                .HasForeignKey(lp => lp.LessonId)
                .OnDelete(DeleteBehavior.Cascade);
                
            // Configure Quizzes (Phase 12)
            modelBuilder.Entity<Quiz>()
                .HasOne(q => q.CourseSection)
                .WithMany(cs => cs.Quizzes)
                .HasForeignKey(q => q.CourseSectionId)
                .OnDelete(DeleteBehavior.Cascade);

            modelBuilder.Entity<Question>()
                .HasOne(q => q.Quiz)
                .WithMany(qz => qz.Questions)
                .HasForeignKey(q => q.QuizId)
                .OnDelete(DeleteBehavior.Cascade);

            modelBuilder.Entity<Answer>()
                .HasOne(a => a.Question)
                .WithMany(q => q.Answers)
                .HasForeignKey(a => a.QuestionId)
                .OnDelete(DeleteBehavior.Cascade);

            modelBuilder.Entity<QuizAttempt>()
                .HasOne(qa => qa.User)
                .WithMany(u => u.QuizAttempts)
                .HasForeignKey(qa => qa.UserId)
                .OnDelete(DeleteBehavior.Cascade);

            modelBuilder.Entity<QuizAttempt>()
                .HasOne(qa => qa.Quiz)
                .WithMany(qz => qz.QuizAttempts)
                .HasForeignKey(qa => qa.QuizId)
                .OnDelete(DeleteBehavior.Cascade); // Cascade is fine here or restrict if we want to keep attempts

            modelBuilder.Entity<QuizAttemptAnswer>()
                .HasOne(qaa => qaa.QuizAttempt)
                .WithMany(qa => qa.SelectedAnswers)
                .HasForeignKey(qaa => qaa.QuizAttemptId)
                .OnDelete(DeleteBehavior.Cascade);

            modelBuilder.Entity<QuizAttemptAnswer>()
                .HasOne(qaa => qaa.Question)
                .WithMany()
                .HasForeignKey(qaa => qaa.QuestionId)
                .OnDelete(DeleteBehavior.Restrict);

            modelBuilder.Entity<QuizAttemptAnswer>()
                .HasOne(qaa => qaa.Answer)
                .WithMany()
                .HasForeignKey(qaa => qaa.AnswerId)
                .OnDelete(DeleteBehavior.Restrict);

            // Configure SearchHistory
            modelBuilder.Entity<SearchHistory>()
                .HasOne(sh => sh.User)
                .WithMany()
                .HasForeignKey(sh => sh.UserId)
                .OnDelete(DeleteBehavior.SetNull);

            // Configure UserActivity
            modelBuilder.Entity<UserActivity>()
                .HasOne(ua => ua.User)
                .WithMany()
                .HasForeignKey(ua => ua.UserId)
                .OnDelete(DeleteBehavior.Cascade);
        }
    }
}

