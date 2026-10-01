using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;

namespace EduMy.Backend.Models
{
    public class Course
    {
        public int CourseId { get; set; }
        public int InstructorId { get; set; }
        public int? CategoryId { get; set; }
        public string Title { get; set; } = string.Empty;
        public string Slug { get; set; } = string.Empty;
        public string? Description { get; set; }
        public decimal Price { get; set; }
        [MaxLength(500)]
        public string? ThumbnailUrl { get; set; }
        public string Level { get; set; } = "Beginner"; // Beginner, Intermediate, Advanced
        public string Status { get; set; } = "Draft"; // Draft, Published
        public double AverageRating { get; set; } = 0;
        public int StudentCount { get; set; } = 0;
        public bool NeedsReanalysis { get; set; } = false;
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
        public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;

        public User? Instructor { get; set; }
        public Category? Category { get; set; }
        public ICollection<CourseSection> Sections { get; set; } = new List<CourseSection>();
        public ICollection<Enrollment> Enrollments { get; set; } = new List<Enrollment>();
        public ICollection<Review> Reviews { get; set; } = new List<Review>();
        public ICollection<OrderItem> OrderItems { get; set; } = new List<OrderItem>();
        
        public ICollection<Wishlist> Wishlists { get; set; } = new List<Wishlist>();
        public ICollection<CourseCoupon> CourseCoupons { get; set; } = new List<CourseCoupon>();
        public ICollection<Certificate> Certificates { get; set; } = new List<Certificate>();
        public ICollection<CourseMlAnalysis> MlAnalyses { get; set; } = new List<CourseMlAnalysis>();
        
        public ICollection<CourseTag> CourseTags { get; set; } = new List<CourseTag>();
    }
}
