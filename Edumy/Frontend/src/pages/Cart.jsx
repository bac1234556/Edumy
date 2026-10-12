import React, { useState, useEffect, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';
import { CartContext } from '../context/CartContext';
import { 
  ShoppingCart, 
  Trash2, 
  ArrowRight, 
  Tag, 
  ShieldCheck, 
  BookOpen,
  ArrowLeft 
} from 'lucide-react';
import './Cart.css';

const BACKEND_URL = (import.meta.env.VITE_API_URL || 'http://localhost:5000/api').replace('/api', '');

function Cart() {
  const { user } = useContext(AuthContext);
  const { cart, loadingCart, removeFromCart, fetchCart } = useContext(CartContext);
  const navigate = useNavigate();

  const [checkoutLoading, setCheckoutLoading] = useState(false);
  const [couponCode, setCouponCode] = useState('');
  const [appliedCoupon, setAppliedCoupon] = useState(null);
  const [couponError, setCouponError] = useState('');
  const [couponLoading, setCouponLoading] = useState(false);

  useEffect(() => {
    fetchCart();
  }, [fetchCart]);

  const handleRemove = async (courseId) => {
    try {
      await removeFromCart(courseId);
    } catch (err) {
      alert('Không thể xóa khóa học khỏi giỏ hàng.');
    }
  };

  const handleApplyCoupon = async () => {
    if (!couponCode.trim()) return;
    setCouponLoading(true);
    setCouponError('');
    try {
      const res = await api.post('/coupons/validate', { code: couponCode.trim() });
      setAppliedCoupon({
        code: res.data.code,
        discountPercentage: res.data.discountPercentage,
        message: res.data.message
      });
      setCouponCode('');
    } catch (err) {
      setCouponError(err.response?.data?.message || 'Mã giảm giá không hợp lệ hoặc đã hết hạn.');
      setAppliedCoupon(null);
    } finally {
      setCouponLoading(false);
    }
  };

  const handleCheckout = async () => {
    try {
      setCheckoutLoading(true);
      const res = await api.post('/orders/checkout', {
        couponCode: appliedCoupon?.code || null
      });
      // Navigate to payment gateway URL (e.g. /payment?orderId=...)
      if (res.data.paymentUrl) {
        navigate(res.data.paymentUrl);
      } else {
        navigate(`/payment?orderId=${res.data.orderId}`);
      }
    } catch (err) {
      alert(err.response?.data?.message || 'Khởi tạo đơn hàng thanh toán thất bại.');
    } finally {
      setCheckoutLoading(false);
    }
  };

  const formatPrice = (price) => {
    if (!price && price !== 0) return '0 ₫';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
  };

  if (!user) {
    return (
      <div className="cart-page-wrapper">
        <div className="cart-container">
          <div className="cart-empty-box">
            <div className="cart-empty-icon-wrap">
              <ShoppingCart size={36} />
            </div>
            <h2 className="cart-empty-title">Vui lòng đăng nhập để xem giỏ hàng</h2>
            <p className="cart-empty-subtitle">
              Đăng nhập tài khoản học viên để xem các khóa học bạn đã thêm vào giỏ.
            </p>
            <Link to="/login" className="btn-continue-shopping">
              Đăng nhập ngay
            </Link>
          </div>
        </div>
      </div>
    );
  }

  if (loadingCart && !cart) {
    return (
      <div className="cart-page-wrapper">
        <div className="cart-container text-center" style={{ padding: '6rem 0', color: '#94a3b8' }}>
          <div style={{ width: 40, height: 40, border: '4px solid #1f2937', borderTopColor: '#6366f1', borderRadius: '50%', margin: '0 auto 1rem auto', animation: 'spin 0.8s linear infinite' }}></div>
          <p>Đang tải giỏ hàng của bạn...</p>
        </div>
      </div>
    );
  }

  const items = cart?.items || [];
  const rawTotal = cart?.totalPrice || 0;
  const discountAmount = appliedCoupon ? (rawTotal * appliedCoupon.discountPercentage) / 100 : 0;
  const finalTotal = Math.max(0, rawTotal - discountAmount);

  return (
    <div className="cart-page-wrapper">
      <div className="cart-container">
        <h1 className="cart-page-heading">Giỏ hàng của bạn</h1>

        {items.length === 0 ? (
          <div className="cart-empty-box">
            <div className="cart-empty-icon-wrap">
              <ShoppingCart size={36} />
            </div>
            <h3 className="cart-empty-title">Giỏ hàng của bạn đang trống</h3>
            <p className="cart-empty-subtitle">
              Khám phá hàng ngàn khóa học lập trình, thiết kế và công nghệ hàng đầu trên Edumy!
            </p>
            <Link to="/courses" className="btn-continue-shopping">
              <BookOpen size={18} /> Khám phá khóa học ngay
            </Link>
          </div>
        ) : (
          <div className="cart-layout-grid">
            {/* Left Column: List of items */}
            <div className="cart-items-column">
              <div className="cart-items-meta-header">
                {items.length} Khóa học đã chọn
              </div>

              <div className="cart-items-card-list">
                {items.map(item => (
                  <div key={item.id} className="cart-item-card">
                    <img 
                      src={item.course.thumbnailUrl 
                        ? (item.course.thumbnailUrl.startsWith('http') ? item.course.thumbnailUrl : `${BACKEND_URL}${item.course.thumbnailUrl}`)
                        : "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=300&q=80"} 
                      alt={item.course.title} 
                      className="cart-item-thumbnail" 
                    />
                    <div className="cart-item-details">
                      <div>
                        <Link to={`/courses/${item.courseId}`} className="cart-item-title-link">
                          {item.course.title}
                        </Link>
                        <p className="cart-item-instructor">
                          Giảng viên: {item.course.instructor?.fullName || 'Chuyên gia Edumy'}
                        </p>
                      </div>

                      <div className="cart-item-actions">
                        <button 
                          className="btn-remove-cart-item" 
                          onClick={() => handleRemove(item.courseId)}
                        >
                          <Trash2 size={14} /> Xóa khỏi giỏ
                        </button>
                      </div>
                    </div>

                    <div className="cart-item-price-col">
                      <div className="item-main-price">
                        {formatPrice(item.course.price)}
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              <div style={{ marginTop: '1.25rem' }}>
                <Link to="/courses" style={{ color: '#818cf8', textDecoration: 'none', fontSize: '0.875rem', display: 'inline-flex', alignItems: 'center', gap: '0.4rem', fontWeight: 600 }}>
                  <ArrowLeft size={16} /> Tiếp tục tìm kiếm khóa học
                </Link>
              </div>
            </div>

            {/* Right Column: Order Summary */}
            <aside className="cart-summary-column">
              <div className="summary-card-box">
                <h3 className="summary-heading">Tóm tắt đơn hàng</h3>

                <div className="summary-row">
                  <span>Tạm tính ({items.length} khóa học):</span>
                  <span>{formatPrice(rawTotal)}</span>
                </div>

                {appliedCoupon && (
                  <div className="summary-row discount-row">
                    <span>Khuyến mãi ({appliedCoupon.code} -{appliedCoupon.discountPercentage}%):</span>
                    <span>-{formatPrice(discountAmount)}</span>
                  </div>
                )}

                <div className="summary-total-divider"></div>

                <div className="summary-final-total-row">
                  <span className="total-label">Tổng cộng:</span>
                  <span className="total-amount-display">{formatPrice(finalTotal)}</span>
                </div>

                {/* Coupon Input */}
                <div style={{ marginBottom: '1.25rem' }}>
                  <div className="coupon-input-group">
                    <input 
                      type="text" 
                      placeholder="Mã giảm giá (ví dụ: EDUMY50)"
                      value={couponCode}
                      onChange={(e) => setCouponCode(e.target.value.toUpperCase())}
                      className="coupon-input-field"
                    />
                    <button 
                      type="button" 
                      onClick={handleApplyCoupon}
                      disabled={couponLoading || !couponCode.trim()}
                      className="btn-apply-coupon"
                    >
                      {couponLoading ? '...' : 'Áp dụng'}
                    </button>
                  </div>
                  {couponError && <div className="coupon-feedback-msg error">{couponError}</div>}
                  {appliedCoupon && <div className="coupon-feedback-msg success">✓ {appliedCoupon.message}</div>}
                </div>

                <button 
                  className="btn-checkout-now" 
                  onClick={handleCheckout}
                  disabled={checkoutLoading}
                >
                  {checkoutLoading ? 'Đang tạo đơn thanh toán...' : (
                    <>
                      Tiến hành Thanh toán <ArrowRight size={18} />
                    </>
                  )}
                </button>

                <div className="guarantee-note-card">
                  <ShieldCheck size={14} style={{ display: 'inline', marginRight: 4, verticalAlign: -2 }} />
                  Bảo mật giao diện thanh toán & Cam kết hoàn tiền 30 ngày
                </div>
              </div>
            </aside>
          </div>
        )}
      </div>
    </div>
  );
}

export default Cart;
