using System;

namespace EduMy.Backend.Models
{
    public class Enrollment
    {
        public int EnrollmentId { get; set; }
        public int UserId { get; set; }
        public int CourseId { get; set; }
        public DateTime EnrolledAt { get; set; } = DateTime.UtcNow;
        public int ProgressPercentage { get; set; } = 0;
        public DateTime? CompletedAt { get; set; }

        public User? User { get; set; }
        public Course? Course { get; set; }
    }
}
