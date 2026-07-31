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
    public class OrdersController : ControllerBase
    {
        private readonly ApplicationDbContext _context;

        public OrdersController(ApplicationDbContext context)
        {
            _context = context;
        }

        [HttpPost("checkout")]
        public async Task<IActionResult> Checkout([FromBody] CheckoutRequest request)
        {
            var userIdStr = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (!int.TryParse(userIdStr, out int userId)) return Unauthorized();

            // 1. Get user's cart
            var cart = await _context.Carts
                .Include(c => c.CartItems)
                    .ThenInclude(ci => ci.Course)
                .FirstOrDefaultAsync(c => c.UserId == userId);

            if (cart == null || !cart.CartItems.Any())
            {
                return BadRequest("Your cart is empty.");
            }

            // 2. Calculate total amount
            decimal totalAmount = cart.CartItems.Sum(ci => ci.Course.Price);

            // 3. Apply Coupon if any
            if (!string.IsNullOrWhiteSpace(request?.CouponCode))
            {
                var coupon = await _context.Coupons
                    .FirstOrDefaultAsync(c => c.Code.ToUpper() == request.CouponCode.ToUpper() && c.IsActive && c.ExpiryDate > DateTime.UtcNow);
                
                if (coupon != null)
                {
                    var discountAmount = totalAmount * (coupon.DiscountPercentage / 100m);
                    totalAmount -= discountAmount;
                }
            }

            // 4. Create Order in Pending state
            var order = new Order
            {
                UserId = userId,
                TotalAmount = totalAmount,
                Status = "Pending",
                CreatedAt = DateTime.UtcNow
            };

            foreach (var item in cart.CartItems)
            {
                order.OrderItems.Add(new OrderItem
                {
                    CourseId = item.CourseId,
                    Price = item.Course.Price
                });
            }

            _context.Orders.Add(order);
            await _context.SaveChangesAsync();

            // 5. Return payment redirect URL (Mock)
            var paymentUrl = $"/payment?orderId={order.OrderId}&amount={totalAmount}";

            return Ok(new { 
                message = "Order created. Redirecting to payment gateway...", 
                orderId = order.OrderId,
                paymentUrl = paymentUrl
            });
        }

        [HttpGet("my-orders")]
        public async Task<IActionResult> GetMyOrders()
        {
            var userIdStr = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (!int.TryParse(userIdStr, out int userId)) return Unauthorized();

            var orders = await _context.Orders
                .Include(o => o.OrderItems)
                    .ThenInclude(oi => oi.Course)
                .Where(o => o.UserId == userId)
                .OrderByDescending(o => o.CreatedAt)
                .ToListAsync();

            return Ok(orders);
        }
    }

    public class CheckoutRequest
    {
        public string? CouponCode { get; set; }
    }
}
