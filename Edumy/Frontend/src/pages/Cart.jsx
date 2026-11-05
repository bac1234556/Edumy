import React, { useState, useEffect, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';
import { CartContext } from '../context/CartContext';
import {
  ShoppingCart,
  Trash2,
  Heart,
  ArrowRight,
  Tag,
  ShieldCheck,
  BookOpen,
  ArrowLeft,
  CheckCircle,
  CheckCircle2,
  AlertCircle,
  Star,
  Lock,
  Sparkles,
  PlayCircle
} from 'lucide-react';
import './Cart.css';

const BACKEND_URL = (import.meta.env.VITE_API_URL || 'http://localhost:5000/api').replace('/api', '');

function Cart() {
  const { user } = useContext(AuthContext);
  const { cart, loadingCart, removeFromCart, fetchCart } = useContext(CartContext);
  const navigate = useNavigate();

  const [cartItems, setCartItems] = useState(cart?.items || []);
  const [deletingId, setDeletingId] = useState(null);
  const [checkoutLoading, setCheckoutLoading] = useState(false);
  const [couponCode, setCouponCode] = useState('');
  const [appliedCoupon, setAppliedCoupon] = useState(null);
  const [couponError, setCouponError] = useState('');
  const [couponLoading, setCouponLoading] = useState(false);
  const [wishlistLoadingId, setWishlistLoadingId] = useState(null);

  useEffect(() => {
    fetchCart();
  }, [fetchCart]);

  useEffect(() => {
    if (cart?.items) {
      setCartItems(cart.items);
    } else {
      setCartItems([]);
    }
  }, [cart]);

  // Remove single item from cart with robust ID checking, stopPropagation, and local state update
  const handleRemoveFromCart = async (e, item) => {
    if (e) {
      e.preventDefault();
      e.stopPropagation();
    }
    const courseId = item.courseId;
    const cartItemId = item.id;
    const actionKey = cartItemId || courseId;

    try {
      setDeletingId(actionKey);
      await removeFromCart(courseId, cartItemId);
      setCartItems((prev) => prev.filter((ci) => {
        if (cartItemId && ci.id === cartItemId) return false;
        if (courseId && ci.courseId === courseId) return false;
        return true;
      }));
    } catch (err) {
      alert(err.response?.data?.message || 'Không thể xóa khóa học khỏi giỏ hàng. Vui lòng thử lại.');
    } finally {
      setDeletingId(null);
    }
  };

  // Move item to Wishlist (Add to wishlist & remove from cart)
  const handleMoveToWishlist = async (e, item) => {
    if (e) {
      e.preventDefault();
      e.stopPropagation();
    }
    const courseId = item.courseId;
    const cartItemId = item.id;

    try {
      setWishlistLoadingId(courseId);
      try {
        await api.post(`/wishlist/add/${courseId}`);
      } catch (err) {
        // Ignored if already in wishlist
      }

      await removeFromCart(courseId, cartItemId);
      setCartItems((prev) => prev.filter((ci) => {
        if (cartItemId && ci.id === cartItemId) return false;
        if (courseId && ci.courseId === courseId) return false;
        return true;
      }));
      alert('Đã chuyển khóa học vào Danh sách yêu thích (Wishlist)!');
    } catch (err) {
      alert('Chuyển khóa học vào Wishlist thất bại. Vui lòng thử lại.');
    } finally {
      setWishlistLoadingId(null);
    }
  };

  // Apply & Validate Coupon Code
  const handleApplyCoupon = async () => {
    if (!couponCode.trim()) {
      setCouponError('Vui lòng nhập mã giảm giá trước khi áp dụng.');
      return;
    }
    setCouponLoading(true);
    setCouponError('');
    try {
      const res = await api.post('/coupons/validate', { code: couponCode.trim() });
      setAppliedCoupon({
        code: res.data.code,
        discountPercentage: res.data.discountPercentage,
        message: res.data.message || `Đã áp dụng giảm giá ${res.data.discountPercentage}%`
      });
      setCouponCode('');
    } catch (err) {
      setCouponError(err.response?.data?.message || 'Mã giảm giá không hợp lệ hoặc đã hết hạn.');
      setAppliedCoupon(null);
    } finally {
      setCouponLoading(false);
    }
  };

  const handleRemoveCoupon = () => {
    setAppliedCoupon(null);
    setCouponError('');
  };

  // Checkout Handler (chỉ thanh toán các khóa học CHƯA mua)
  const handleCheckout = async () => {
    try {
      setCheckoutLoading(true);
      const res = await api.post('/orders/checkout', {
        couponCode: appliedCoupon?.code || null
      });

      if (res.data.isFree) {
        alert(res.data.message || 'Đăng ký khóa học thành công! (Khóa học miễn phí)');
        navigate('/my-courses');
        return;
      }

      if (res.data.paymentUrl) {
        if (res.data.paymentUrl.startsWith('http://') || res.data.paymentUrl.startsWith('https://')) {
          window.location.href = res.data.paymentUrl;
        } else {
          navigate(res.data.paymentUrl);
        }
      } else {
        navigate(`/payment?orderId=${res.data.orderId}&amount=${res.data.amount || ''}`);
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

  // 1. Unauthenticated view
  if (!user) {
    return (
      <div className="cart-page-wrapper">
        <div className="cart-container">
          <div className="cart-empty-box">
            <div className="cart-empty-icon-wrap">
              <ShoppingCart size={40} />
            </div>
            <h2 className="cart-empty-title">Vui lòng đăng nhập để xem giỏ hàng</h2>
            <p className="cart-empty-subtitle">
              Đăng nhập tài khoản học viên để xem các khóa học bạn đã chọn và hoàn tất đăng ký học.
            </p>
            <Link to="/login" className="btn-continue-shopping">
              Đăng nhập ngay <ArrowRight size={16} />
            </Link>
          </div>
        </div>
      </div>
    );
  }

  // 2. Loading Skeleton View
  if (loadingCart && !cart) {
    return (
      <div className="cart-page-wrapper">
        <div className="cart-container">
          <div className="cart-header-row">
            <div className="skeleton-box mb-2" style={{ width: '260px', height: '36px' }}></div>
          </div>

          <div className="cart-layout-grid">
            <div className="cart-items-column">
              {[1, 2].map((n) => (
                <div key={n} className="cart-skeleton-item">
                  <div className="skeleton-thumb skeleton-box"></div>
                  <div>
                    <div className="skeleton-box mb-2" style={{ width: '70%', height: '20px' }}></div>
                    <div className="skeleton-box mb-2" style={{ width: '40%', height: '16px' }}></div>
                    <div className="skeleton-box" style={{ width: '30%', height: '16px' }}></div>
                  </div>
                  <div className="skeleton-box" style={{ width: '80px', height: '24px' }}></div>
                </div>
              ))}
            </div>

            <div className="cart-summary-column">
              <div className="order-summary-card">
                <div className="skeleton-box mb-3" style={{ width: '60%', height: '28px' }}></div>
                <div className="skeleton-box mb-3" style={{ width: '100%', height: '20px' }}></div>
                <div className="skeleton-box mb-4" style={{ width: '100%', height: '36px' }}></div>
                <div className="skeleton-box" style={{ width: '100%', height: '48px' }}></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  const items = cartItems;

  // Phân loại khóa học đã mua và chưa mua
  const payableItems = items.filter((item) => !item.isEnrolled && !item.isPaid);
  const enrolledItems = items.filter((item) => item.isEnrolled || item.isPaid);
  const allPaid = items.length > 0 && payableItems.length === 0;

  // Tính tiền: Chỉ tính trên các khóa học CHƯA mua (loại bỏ hoàn toàn khóa học đã sở hữu)
  const payableRawTotal = payableItems.reduce((acc, item) => acc + (item.course?.price || 0), 0);

  const discountAmount = appliedCoupon ? (payableRawTotal * appliedCoupon.discountPercentage) / 100 : 0;
  const finalPayableTotal = Math.max(0, payableRawTotal - discountAmount);

  return (
    <div className="cart-page-wrapper">
      <div className="cart-container">
        {/* Header Row */}
        <div className="cart-header-row d-flex justify-content-between align-items-center">
          <div>
            <h1 className="cart-page-heading">
              Giỏ hàng của bạn
              {items.length > 0 && (
                <span className="cart-heading-count">({items.length} khóa học)</span>
              )}
            </h1>
            <Link to="/courses" className="cart-breadcrumb-back">
              <ArrowLeft size={16} /> Tiếp tục tìm kiếm khóa học
            </Link>
          </div>
        </div>

        {/* Empty Cart State */}
        {items.length === 0 ? (
          <div className="cart-empty-box">
            <div className="cart-empty-icon-wrap">
              <ShoppingCart size={40} />
            </div>
            <h3 className="cart-empty-title">Giỏ hàng của bạn đang trống</h3>
            <p className="cart-empty-subtitle">
              Bạn chưa thêm khóa học nào vào giỏ hàng. Hãy khám phá kho tàng kiến thức công nghệ hiện đại trên Edumy ngay hôm nay!
            </p>
            <Link to="/courses" className="btn-continue-shopping">
              <BookOpen size={18} /> Khám phá các khóa học ngay
            </Link>
          </div>
        ) : (
          <div>
            {/* 1. Banner Thông Báo Thân Thiện: Khi TẤT CẢ khóa học trong giỏ đều ĐÃ THANH TOÁN */}
            {allPaid && (
              <div className="all-paid-success-banner">
                <div className="all-paid-banner-content">
                  <div className="all-paid-banner-icon">
                    <Sparkles size={28} />
                  </div>
                  <div>
                    <h3 className="all-paid-banner-title">
                      Tất cả khóa học trong giỏ hàng của bạn đã được thanh toán thành công!
                    </h3>
                    <p className="all-paid-banner-desc">
                      Bạn đã sở hữu toàn bộ {items.length} khóa học dưới đây. Bạn không cần phải thanh toán thêm, hãy vào phòng học để bắt đầu học tập ngay nhé!
                    </p>
                  </div>
                </div>
                <Link to="/my-courses" className="btn-all-paid-goto">
                  <BookOpen size={18} /> Đi tới Khóa học của tôi <ArrowRight size={18} />
                </Link>
              </div>
            )}

            <div className={`cart-layout-grid ${allPaid ? 'all-paid-layout' : ''}`}>
              {/* Left Column: Cart Items List */}
              <div className={`cart-items-column ${allPaid ? 'all-paid-full-width' : ''}`}>
                <div className="cart-items-meta-header d-flex justify-content-between align-items-center">
                  <span>Danh sách khóa học trong đơn ({items.length})</span>
                  {enrolledItems.length > 0 && (
                    <span className="enrolled-items-badge-count">
                      <CheckCircle2 size={14} /> {enrolledItems.length} khóa học đã sở hữu
                    </span>
                  )}
                </div>

                {items.map((item) => {
                  const isEnrolled = item.isEnrolled || item.isPaid;

                  return (
                    <div
                      key={item.id || item.courseId}
                      className={`cart-item-card ${isEnrolled ? 'is-enrolled-card' : ''}`}
                    >
                      {/* Thumbnail */}
                      <div className="cart-item-thumbnail-wrap">
                        <img
                          src={
                            item.course?.thumbnailUrl
                              ? item.course.thumbnailUrl.startsWith('http')
                                ? item.course.thumbnailUrl
                                : `${BACKEND_URL}${item.course.thumbnailUrl}`
                              : 'https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=300&q=80'
                          }
                          alt={item.course?.title}
                          className="cart-item-thumbnail"
                        />
                      </div>

                      {/* Details */}
                      <div className="cart-item-details">
                        <div>
                          {/* Badge nổi bật nếu khóa học ĐÃ SỞ HỮU */}
                          {isEnrolled && (
                            <div className="mb-2">
                              <span className="badge-enrolled">
                                <CheckCircle2 size={14} /> ✓ Đã sở hữu
                              </span>
                            </div>
                          )}

                          <Link
                            to={`/courses/${item.courseId}`}
                            className="cart-item-title-link"
                          >
                            {item.course?.title || 'Khóa học Edumy'}
                          </Link>
                          <p className="cart-item-instructor">
                            Giảng viên:{' '}
                            <strong>
                              {item.course?.instructorName || item.course?.instructor?.fullName || item.course?.instructor || 'Chuyên gia Edumy'}
                            </strong>
                          </p>
                          <div className="cart-item-rating-row">
                            <span className="cart-rating-score">
                              {item.course?.averageRating || '4.8'}
                            </span>
                            <div className="stars">
                              {[...Array(5)].map((_, i) => (
                                <Star key={i} size={13} fill="#f59e0b" color="#f59e0b" />
                              ))}
                            </div>
                            <span className="text-muted">
                              ({item.course?.reviewsCount || item.course?.reviews?.length || 24} đánh giá)
                            </span>
                          </div>
                        </div>

                        {/* Actions */}
                        <div className="cart-item-actions">
                          {isEnrolled ? (
                            /* Nút dành cho khóa học ĐÃ THANH TOÁN: Vào học ngay */
                            <>
                              <Link
                                to={`/my-courses/${item.courseId}/learn`}
                                className="btn-item-action btn-enter-learn"
                              >
                                <PlayCircle size={15} /> Vào học ngay
                              </Link>
                              <button
                                type="button"
                                onClick={(e) => handleRemoveFromCart(e, item)}
                                disabled={deletingId === (item.id || item.courseId)}
                                className="btn-item-action remove-btn"
                                title="Xóa khỏi giỏ hàng"
                              >
                                {deletingId === (item.id || item.courseId) ? (
                                  <>
                                    <span className="cart-remove-spinner" /> Đang xóa...
                                  </>
                                ) : (
                                  <>
                                    <Trash2 size={15} /> Xóa khỏi giỏ
                                  </>
                                )}
                              </button>
                            </>
                          ) : (
                            /* Nút dành cho khóa học CHƯA MUA */
                            <>
                              <button
                                type="button"
                                onClick={(e) => handleMoveToWishlist(e, item)}
                                disabled={wishlistLoadingId === item.courseId || deletingId === (item.id || item.courseId)}
                                className="btn-item-action"
                              >
                                {wishlistLoadingId === item.courseId ? 'Đang lưu...' : (
                                  <>
                                    <Heart size={15} /> Lưu vào yêu thích
                                  </>
                                )}
                              </button>
                              <button
                                type="button"
                                onClick={(e) => handleRemoveFromCart(e, item)}
                                disabled={deletingId === (item.id || item.courseId)}
                                className="btn-item-action remove-btn"
                                title="Xóa khỏi giỏ hàng"
                              >
                                {deletingId === (item.id || item.courseId) ? (
                                  <>
                                    <span className="cart-remove-spinner" /> Đang xóa...
                                  </>
                                ) : (
                                  <>
                                    <Trash2 size={15} /> Xóa khỏi giỏ
                                  </>
                                )}
                              </button>
                            </>
                          )}
                        </div>
                      </div>

                      {/* Price Box */}
                      <div className="cart-item-price-box">
                        {isEnrolled ? (
                          /* Khóa học đã mua: Giá gạch ngang + nhãn Đã mua */
                          <div className="enrolled-price-stack">
                            <span className="enrolled-status-pill">Đã mua</span>
                            <span className="item-price-strikethrough">
                              {formatPrice(item.course?.price)}
                            </span>
                          </div>
                        ) : (
                          /* Khóa học chưa mua: Hiển thị giá bình thường */
                          <>
                            <span className="item-current-price">
                              {formatPrice(item.course?.price)}
                            </span>
                            {item.course?.originalPrice && item.course.originalPrice > item.course.price && (
                              <span className="item-original-price">
                                {formatPrice(item.course.originalPrice)}
                              </span>
                            )}
                          </>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>

              {/* Right Column: Order Summary (ẨN HOÀN TOÀN nếu tất cả khóa học đều đã thanh toán) */}
              {!allPaid && (
                <div className="cart-summary-column">
                  <div className="order-summary-card">
                    <h3 className="summary-heading">Tóm tắt đơn hàng</h3>

                    {/* Thông báo loại trừ khóa học đã sở hữu nếu có */}
                    {enrolledItems.length > 0 && (
                      <div className="enrolled-exclusion-alert">
                        <ShieldCheck size={16} />
                        <span>
                          Đã tự động loại trừ <strong>{enrolledItems.length} khóa học</strong> bạn đã sở hữu ra khỏi hóa đơn.
                        </span>
                      </div>
                    )}

                    <div className="summary-rows-stack">
                      <div className="summary-row">
                        <span>Tạm tính ({payableItems.length} khóa học):</span>
                        <span>{formatPrice(payableRawTotal)}</span>
                      </div>

                      {appliedCoupon && (
                        <div className="summary-row discount-row">
                          <span>Giảm giá ({appliedCoupon.discountPercentage}%):</span>
                          <span>-{formatPrice(discountAmount)}</span>
                        </div>
                      )}

                      <div className="summary-row total-row">
                        <span>Tổng thanh toán:</span>
                        <span className="final-price-value">{formatPrice(finalPayableTotal)}</span>
                      </div>
                    </div>

                    {/* Coupon Code Section */}
                    <div className="coupon-section-box">
                      <label className="coupon-section-label">
                        <Tag size={16} /> Mã giảm giá (Coupon):
                      </label>
                      <div className="coupon-input-group">
                        <input
                          type="text"
                          className="coupon-input"
                          placeholder="Nhập mã (VD: EDUMY50)"
                          value={couponCode}
                          onChange={(e) => setCouponCode(e.target.value.toUpperCase())}
                        />
                        <button
                          type="button"
                          onClick={handleApplyCoupon}
                          disabled={couponLoading}
                          className="btn-apply-coupon"
                        >
                          {couponLoading ? 'Kiểm tra...' : 'Áp dụng'}
                        </button>
                      </div>

                      {appliedCoupon && (
                        <div className="applied-coupon-tag">
                          <span className="d-flex align-items-center gap-1">
                            <CheckCircle size={14} /> Mã <strong>{appliedCoupon.code}</strong> (-{appliedCoupon.discountPercentage}%)
                          </span>
                          <button
                            type="button"
                            onClick={handleRemoveCoupon}
                            className="btn-remove-coupon"
                          >
                            Gỡ bỏ
                          </button>
                        </div>
                      )}

                      {couponError && (
                        <div className="coupon-error-text">
                          <AlertCircle size={14} /> {couponError}
                        </div>
                      )}
                    </div>

                    {/* Checkout Primary Button */}
                    <button
                      onClick={handleCheckout}
                      disabled={checkoutLoading || payableItems.length === 0}
                      className="btn-checkout-primary"
                    >
                      {checkoutLoading ? (
                        'Đang khởi tạo đơn...'
                      ) : (
                        <>
                          Tiến hành Thanh toán ({payableItems.length}) <ArrowRight size={18} />
                        </>
                      )}
                    </button>

                    {/* Trust & Guarantee Info */}
                    <div className="payment-trust-box">
                      <div className="trust-badge-row">
                        <Lock size={14} /> Giao dịch bảo mật chuẩn SSL 256-bit
                      </div>
                      <div className="trust-badge-row mb-3">
                        <ShieldCheck size={14} /> Bảo đảm hoàn tiền trong 30 ngày
                      </div>
                      <div className="payment-methods-strip">
                        <span className="payment-pill">VietQR 24/7</span>
                        <span className="payment-pill">VNPay QR</span>
                        <span className="payment-pill">Visa / Master</span>
                      </div>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default Cart;
