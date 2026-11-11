import React, { useState, useEffect, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { orderApi } from '../api/modules/orderApi';
import { courseApi } from '../api/modules/courseApi';
import { AuthContext } from '../context/AuthContext';
import { CartContext } from '../context/CartContext';
import CourseCard from '../components/CourseCard';
import {
  ShoppingCart,
  Trash2,
  ArrowRight,
  ShieldCheck,
  BookOpen,
  ArrowLeft,
  CheckCircle2,
  Star,
  Lock,
  Sparkles,
  PlayCircle,
  Tag,
  AlertCircle,
  Check
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
  const [suggestedCourses, setSuggestedCourses] = useState([]);

  // Coupon / Voucher state
  const [couponInput, setCouponInput] = useState('');
  const [appliedCoupon, setAppliedCoupon] = useState(null);
  const [couponLoading, setCouponLoading] = useState(false);
  const [couponError, setCouponError] = useState('');

  useEffect(() => {
    fetchCart();
    // Load suggested public courses for empty cart state
    courseApi.getCourses({ pageSize: 4, sortBy: 'rating', sortOrder: 'desc' })
      .then((res) => {
        const list = Array.isArray(res.data?.items) ? res.data.items : (Array.isArray(res.data) ? res.data : []);
        setSuggestedCourses(list.slice(0, 4));
      })
      .catch(() => {});
  }, [fetchCart]);

  useEffect(() => {
    if (cart?.items) {
      setCartItems(cart.items);
    } else {
      setCartItems([]);
    }
  }, [cart]);

  // Apply Coupon Handler
  const handleApplyCoupon = async (e) => {
    e.preventDefault();
    if (!couponInput.trim()) return;
    setCouponLoading(true);
    setCouponError('');
    try {
      const res = await orderApi.validateCoupon(couponInput.trim());
      setAppliedCoupon(res.data);
      setCouponError('');
    } catch (err) {
      setCouponError(err.response?.data?.message || 'Mã giảm giá không hợp lệ hoặc đã hết hạn.');
      setAppliedCoupon(null);
    } finally {
      setCouponLoading(false);
    }
  };

  // Remove Coupon Handler
  const handleRemoveCoupon = () => {
    setAppliedCoupon(null);
    setCouponInput('');
    setCouponError('');
  };

  // Remove item from cart
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
      setCartItems((prev) =>
        prev.filter((ci) => {
          if (cartItemId && ci.id === cartItemId) return false;
          if (courseId && ci.courseId === courseId) return false;
          return true;
        })
      );
    } catch (err) {
      alert(err.response?.data?.message || 'Không thể xóa khóa học khỏi giỏ hàng. Vui lòng thử lại.');
    } finally {
      setDeletingId(null);
    }
  };

  // Checkout Handler
  const handleCheckout = async () => {
    try {
      setCheckoutLoading(true);
      const res = await orderApi.checkout(appliedCoupon ? appliedCoupon.code : null);

      if (res.data.isFree) {
        alert(res.data.message || 'Ghi danh khóa học thành công!');
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
              Đăng nhập tài khoản học viên để quản lý giỏ hàng và tạo đơn thanh toán QR.
            </p>
            <Link to="/login" className="btn-continue-shopping">
              Đăng nhập ngay <ArrowRight size={16} />
            </Link>
          </div>
        </div>
      </div>
    );
  }

  if (loadingCart && !cart) {
    return (
      <div className="cart-page-wrapper">
        <div className="cart-container">
          <div className="cart-header-row">
            <div className="skeleton-box mb-2" style={{ width: '260px', height: '36px' }}></div>
          </div>
        </div>
      </div>
    );
  }

  const items = cartItems;
  const payableItems = items.filter((item) => !item.isEnrolled && !item.isPaid);
  const enrolledItems = items.filter((item) => item.isEnrolled || item.isPaid);
  const allPaid = items.length > 0 && payableItems.length === 0;
  const payableRawTotal = payableItems.reduce((acc, item) => acc + (item.course?.price || 0), 0);

  const discountPercent = appliedCoupon?.discountPercentage ? Number(appliedCoupon.discountPercentage) : 0;
  const discountAmount = discountPercent > 0 ? Math.round((payableRawTotal * discountPercent) / 100) : 0;
  const finalTotal = Math.max(0, payableRawTotal - discountAmount);

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
          <Link to="/orders" className="btn btn-outline-primary btn-sm rounded-pill px-3">
            Xem lịch sử đơn hàng
          </Link>
        </div>

        {/* Empty Cart State + Suggested Public Courses */}
        {items.length === 0 ? (
          <div>
            <div className="cart-empty-box mb-5">
              <div className="cart-empty-icon-wrap">
                <ShoppingCart size={40} />
              </div>
              <h3 className="cart-empty-title">Giỏ hàng của bạn đang trống</h3>
              <p className="cart-empty-subtitle">
                Bạn chưa thêm khóa học trả phí nào vào giỏ hàng. Hãy tham khảo các khóa học công khai gợi ý bên dưới!
              </p>
              <Link to="/courses" className="btn-continue-shopping">
                <BookOpen size={18} /> Khám phá danh mục khóa học
              </Link>
            </div>

            {suggestedCourses.length > 0 && (
              <div className="mt-4">
                <h4 className="fw-bold mb-3">Khóa học công khai gợi ý cho bạn</h4>
                <div className="row g-4">
                  {suggestedCourses.map((c) => (
                    <div className="col-12 col-md-6 col-lg-3" key={c.courseId}>
                      <CourseCard course={c} />
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        ) : (
          <div>
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
                      Bạn đã sở hữu toàn bộ {items.length} khóa học dưới đây. Hãy vào phòng học để bắt đầu học tập ngay!
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
                  <span>Danh sách khóa học trong giỏ ({items.length})</span>
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

                      <div className="cart-item-details">
                        <div>
                          {isEnrolled && (
                            <div className="mb-2">
                              <span className="badge-enrolled">
                                <CheckCircle2 size={14} /> ✓ Đã sở hữu
                              </span>
                            </div>
                          )}

                          <Link
                            to={`/courses/${item.courseId}`}
                            className="cart-item-title-link line-clamp-2"
                          >
                            {item.course?.title || 'Khóa học Edumy'}
                          </Link>
                          <p className="cart-item-instructor">
                            Giảng viên:{' '}
                            <strong>
                              {item.course?.instructorName || item.course?.instructor?.fullName || item.course?.instructor || 'Giảng viên Edumy'}
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
                          </div>
                        </div>

                        <div className="cart-item-actions">
                          {isEnrolled && (
                            <Link
                              to={`/my-courses/${item.courseId}/learn`}
                              className="btn-item-action btn-enter-learn"
                            >
                              <PlayCircle size={15} /> Vào học ngay
                            </Link>
                          )}
                          <button
                            type="button"
                            onClick={(e) => handleRemoveFromCart(e, item)}
                            disabled={deletingId === (item.id || item.courseId)}
                            className="btn-cart-remove"
                            title="Xóa khỏi giỏ hàng"
                          >
                            <Trash2 size={14} />{' '}
                            <span>{deletingId === (item.id || item.courseId) ? 'Đang xóa...' : 'Xóa'}</span>
                          </button>
                        </div>
                      </div>

                      <div className="cart-item-price-box">
                        {isEnrolled ? (
                          <div className="enrolled-price-stack">
                            <span className="enrolled-status-pill">Đã mua</span>
                            <span className="item-price-strikethrough">
                              {formatPrice(item.course?.price)}
                            </span>
                          </div>
                        ) : (
                          <span className="item-current-price">
                            {formatPrice(item.course?.price)}
                          </span>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>

              {/* Right Column: Order Summary */}
              {!allPaid && (
                <div className="cart-summary-column">
                  <div className="order-summary-card">
                    <h3 className="summary-heading">Tóm tắt đơn hàng</h3>

                    {enrolledItems.length > 0 && (
                      <div className="enrolled-exclusion-alert">
                        <ShieldCheck size={16} />
                        <span>
                          Đã tự động loại trừ <strong>{enrolledItems.length} khóa học</strong> bạn đã sở hữu.
                        </span>
                      </div>
                    )}

                    <div className="summary-rows-stack">
                      <div className="summary-row">
                        <span>Số khóa học thanh toán:</span>
                        <span className="fw-semibold">{payableItems.length} khóa học</span>
                      </div>

                      <div className="summary-row">
                        <span>Tạm tính:</span>
                        <span>{formatPrice(payableRawTotal)}</span>
                      </div>

                      {appliedCoupon && (
                        <div className="summary-row discount-row">
                          <span className="d-flex align-items-center gap-1">
                            <Tag size={15} /> Giảm giá voucher ({discountPercent}%):
                          </span>
                          <span className="discount-value">-{formatPrice(discountAmount)}</span>
                        </div>
                      )}

                      <div className="summary-row total-row">
                        <span>Tổng tiền thanh toán:</span>
                        <span className="final-price-value">{formatPrice(finalTotal)}</span>
                      </div>
                    </div>

                    {/* Ô nhập mã giảm giá (Coupon/Voucher) cùng hàng với nút Áp dụng */}
                    <div className="coupon-section-box">
                      <div className="coupon-section-label">
                        <Tag size={15} /> Mã giảm giá (Voucher)
                      </div>
                      {!appliedCoupon ? (
                        <form onSubmit={handleApplyCoupon} className="coupon-input-group">
                          <input
                            type="text"
                            className="coupon-input"
                            placeholder="Nhập mã voucher..."
                            value={couponInput}
                            onChange={(e) => {
                              setCouponInput(e.target.value);
                              if (couponError) setCouponError('');
                            }}
                            disabled={couponLoading || payableItems.length === 0}
                          />
                          <button
                            type="submit"
                            className="btn-apply-coupon"
                            disabled={couponLoading || !couponInput.trim() || payableItems.length === 0}
                          >
                            {couponLoading ? 'Đang kiểm tra...' : 'Áp dụng'}
                          </button>
                        </form>
                      ) : (
                        <div className="applied-coupon-tag">
                          <div className="d-flex align-items-center gap-2">
                            <CheckCircle2 size={16} />
                            <span>Mã <strong>{appliedCoupon.code}</strong> (Giảm {discountPercent}%)</span>
                          </div>
                          <button type="button" onClick={handleRemoveCoupon} className="btn-remove-coupon">
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

                    <button
                      onClick={handleCheckout}
                      disabled={checkoutLoading || payableItems.length === 0}
                      className="btn-solid-primary w-100"
                      style={{ padding: '0.85rem 1.25rem', fontSize: '1rem', fontWeight: 700 }}
                    >
                      {checkoutLoading ? (
                        'Đang khởi tạo đơn...'
                      ) : (
                        <>
                          Tiến hành thanh toán ({payableItems.length}) <ArrowRight size={18} />
                        </>
                      )}
                    </button>

                    <div className="payment-trust-box">
                      <div className="trust-badge-row">
                        <Lock size={14} /> Chốt giá khóa học tại thời điểm tạo đơn
                      </div>
                      <div className="trust-badge-row mb-3">
                        <ShieldCheck size={14} /> Tự động ghi danh ngay khi VietQR xác nhận
                      </div>
                      <div className="payment-methods-strip">
                        <span className="payment-pill">VietQR Napas 247</span>
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
