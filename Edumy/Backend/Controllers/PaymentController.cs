using EduMy.Backend.Data;
using EduMy.Backend.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;

namespace EduMy.Backend.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class PaymentController : ControllerBase
    {
        private readonly ApplicationDbContext _context;

        public PaymentController(ApplicationDbContext context)
        {
            _context = context;
        }

        [HttpPost("callback")]
        public async Task<IActionResult> PaymentCallback([FromBody] PaymentResultDto result)
        {
            var userIdStr = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (!int.TryParse(userIdStr, out int userId)) return Unauthorized();

            var order = await _context.Orders
                .Include(o => o.OrderItems)
                    .ThenInclude(oi => oi.Course)
                .FirstOrDefaultAsync(o => o.OrderId == result.OrderId && o.UserId == userId);

            if (order == null) return NotFound("Order not found");

            if (order.Status != "Pending")
                return BadRequest($"Order already processed. Current status: {order.Status}");

            if (result.Success)
            {
                order.Status = "Completed";

                foreach (var item in order.OrderItems)
                {
                    // Create Enrollment
                    var existingEnrollment = await _context.Enrollments
                        .FirstOrDefaultAsync(e => e.UserId == userId && e.CourseId == item.CourseId);
                    
                    if (existingEnrollment == null)
                    {
                        _context.Enrollments.Add(new Enrollment
                        {
                            UserId = userId,
                            CourseId = item.CourseId,
                            EnrolledAt = DateTime.UtcNow
                        });
                    }
                    
                    // Increment student count
                    item.Course.StudentCount++;
                    _context.Entry(item.Course).State = EntityState.Modified;
                }

                // Clear Cart
                var cart = await _context.Carts
                    .Include(c => c.CartItems)
                    .FirstOrDefaultAsync(c => c.UserId == userId);

                if (cart != null && cart.CartItems.Any())
                {
                    _context.CartItems.RemoveRange(cart.CartItems);
                }
            }
            else
            {
                order.Status = "Cancelled";
            }

            await _context.SaveChangesAsync();

            return Ok(new { message = result.Success ? "Payment successful" : "Payment cancelled", status = order.Status });
        }
    }

    public class PaymentResultDto
    {
        public int OrderId { get; set; }
        public bool Success { get; set; }
    }
}
