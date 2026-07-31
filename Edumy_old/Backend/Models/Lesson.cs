namespace EduMy.Backend.Models
{
    public class Lesson
    {
        public int LessonId { get; set; }
        public int SectionId { get; set; }
        public string Title { get; set; } = string.Empty;
        public string? VideoUrl { get; set; }
        public int Duration { get; set; } // in seconds
        public int OrderIndex { get; set; }
        public bool IsPreview { get; set; } = false;

        public CourseSection? Section { get; set; }
    }
}
