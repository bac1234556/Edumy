using System.Collections.Generic;

namespace EduMy.Backend.DTOs
{
    public class PagedResultDto<T>
    {
        public IEnumerable<T> Items { get; set; } = new List<T>();
        public int PageNumber { get; set; }
        public int PageSize { get; set; }
        public int TotalItems { get; set; }
        public int TotalPages { get; set; }
        public bool HasPreviousPage => PageNumber > 1;
        public bool HasNextPage => PageNumber < TotalPages;
    }

    public class CourseQueryDto
    {
        public string? Search { get; set; }
        public int? CategoryId { get; set; }
        public string? Level { get; set; }
        public decimal? MinPrice { get; set; }
        public decimal? MaxPrice { get; set; }
        public string? SortBy { get; set; } // price, rating, newest
        public string? SortOrder { get; set; } // asc, desc
        public int PageNumber { get; set; } = 1;
        public int PageSize { get; set; } = 12;
    }

    public class CreateCourseDto
    {
        public string Title { get; set; } = string.Empty;
        public string? Description { get; set; }
        public decimal Price { get; set; }
        public string Level { get; set; } = "Beginner";
        public int? CategoryId { get; set; }
        public string? ThumbnailUrl { get; set; }
    }
}
