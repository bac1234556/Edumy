using EduMy.Backend.Data;
using EduMy.Backend.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace EduMy.Backend.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class CouponsController : ControllerBase
    {
        private readonly ApplicationDbContext _context;

        public CouponsController(ApplicationDbContext context)
        {
            _context = context;
        }

        [HttpPost("validate")]
        public async Task<IActionResult> ValidateCoupon([FromBody] ValidateCouponRequest request)
        {
            if (string.IsNullOrWhiteSpace(request.Code))
                return BadRequest("Coupon code is required");

            var coupon = await _context.Coupons
                .FirstOrDefaultAsync(c => c.Code.ToUpper() == request.Code.ToUpper());

            if (coupon == null)
                return NotFound(new { message = "Invalid coupon code" });

            if (!coupon.IsActive)
                return BadRequest(new { message = "This coupon is no longer active" });

            if (coupon.ExpiryDate < DateTime.UtcNow)
                return BadRequest(new { message = "This coupon has expired" });

            return Ok(new
            {
                id = coupon.Id,
                code = coupon.Code,
                discountPercentage = coupon.DiscountPercentage,
                message = "Coupon applied successfully!"
            });
        }
    }

    public class ValidateCouponRequest
    {
        public string Code { get; set; } = string.Empty;
    }
}
