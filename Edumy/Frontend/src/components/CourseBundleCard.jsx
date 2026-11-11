import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import { CartContext } from '../context/CartContext';
import { AuthContext } from '../context/AuthContext';
import { Plus, Check, ShoppingCart, Sparkles } from 'lucide-react';
import './CourseBundleCard.css';

const BACKEND_URL = (import.meta.env.VITE_API_URL || 'http://localhost:5000/api').replace('/api', '');

const CourseBundleCard = ({ courseId }) => {
  const { addToCart } = useContext(CartContext);
  const { user, isCourseEnrolled } = useContext(AuthContext);
  const navigate = useNavigate();

  const [bundleData, setBundleData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [includeMain, setIncludeMain] = useState(true);
  const [includeBundled, setIncludeBundled] = useState(true);
  const [addingToCart, setAddingToCart] = useState(false);
  const [addedSuccess, setAddedSuccess] = useState(false);

  useEffect(() => {
    if (!courseId) return;

    const fetchBundle = async () => {
      try {
        setLoading(true);
        const res = await api.get(`/courses/${courseId}/bundle-recommendation`);
        if (res.data && res.data.bundledCourse) {
          setBundleData(res.data);
        }
      } catch (err) {
        console.warn('Could not fetch bundle recommendation:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchBundle();
  }, [courseId]);

  const { mainCourse, bundledCourse, originalTotal, bundlePrice, discountPercent, savings } = bundleData || {};
  const mainEnrolled = isCourseEnrolled && isCourseEnrolled(courseId);
  const bundledEnrolled = bundledCourse?.courseId && isCourseEnrolled && isCourseEnrolled(bundledCourse.courseId);

  if (loading || !bundleData || !bundleData.bundledCourse || mainEnrolled || bundledEnrolled) {
    return null;
  }

  const formatPrice = (p) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p || 0);
  };

  const resolveImage = (url) => {
    if (!url) return 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=600&q=80';
    return url.startsWith('http') ? url : `${BACKEND_URL}${url}`;
  };

  // Calculate current price based on checkboxes
  let currentTotal = 0;
  if (includeMain && includeBundled) {
    currentTotal = bundlePrice;
  } else if (includeMain) {
    currentTotal = mainCourse.price || 399000;
  } else if (includeBundled) {
    currentTotal = bundledCourse.price || 499000;
  }

  const handleAddBundleToCart = async () => {
    if (!user) {
      navigate('/login');
      return;
    }

    try {
      setAddingToCart(true);
      if (includeMain && mainCourse?.courseId) {
        try {
          await addToCart(mainCourse.courseId);
        } catch (e) {
          // If already in cart, continue
        }
      }
      if (includeBundled && bundledCourse?.courseId) {
        try {
          await addToCart(bundledCourse.courseId);
        } catch (e) {
          // If already in cart, continue
        }
      }
      setAddedSuccess(true);
      setTimeout(() => setAddedSuccess(false), 3500);
    } catch (err) {
      alert('Không thể thêm combo vào giỏ hàng. Vui lòng thử lại.');
    } finally {
      setAddingToCart(false);
    }
  };

  return (
    <div className="course-bundle-card rounded-2xl shadow-sm hover:shadow-md transition-all duration-200">
      <div className="bundle-header">
        <div className="d-flex align-items-center gap-2">
          <div className="bundle-sparkle-icon">
            <Sparkles size={16} strokeWidth={1.75} aria-hidden="true" />
          </div>
          <h4 className="bundle-title mb-0 text-slate-900">Thường được mua cùng nhau</h4>
        </div>
        <span className="bundle-save-pill pill-badge">
          Tiết kiệm {discountPercent || 20}% khi mua Combo
        </span>
      </div>

      <p className="bundle-subtitle text-slate-600">
        Ghép cặp khóa học cùng lộ trình thực chiến để làm chủ kỹ năng từ cơ bản đến nâng cao.
      </p>

      {/* Course Thumbnails Row */}
      <div className="bundle-items-flow rounded-xl">
        {/* Course 1 */}
        <div className={`bundle-course-item ${!includeMain ? 'item-disabled' : ''}`}>
          <div className="bundle-thumb-wrap">
            <img src={resolveImage(mainCourse.thumbnailUrl)} alt={mainCourse.title} />
            <span className="bundle-course-tag pill-badge">Khóa học này</span>
          </div>
          <div className="bundle-course-meta">
            <label className="bundle-checkbox-label cursor-pointer">
              <input
                type="checkbox"
                checked={includeMain}
                onChange={(e) => setIncludeMain(e.target.checked)}
              />
              <span className="course-title-text line-clamp-2" title={mainCourse.title}>
                {mainCourse.title}
              </span>
            </label>
            <div className="bundle-item-price">{formatPrice(mainCourse.price)}</div>
          </div>
        </div>

        {/* Plus Divider */}
        <div className="bundle-plus-divider" aria-hidden="true">
          <div className="plus-circle">
            <Plus size={18} strokeWidth={1.75} />
          </div>
        </div>

        {/* Course 2 */}
        <div className={`bundle-course-item ${!includeBundled ? 'item-disabled' : ''}`}>
          <div className="bundle-thumb-wrap">
            <img src={resolveImage(bundledCourse.thumbnailUrl)} alt={bundledCourse.title} />
            <span className="bundle-course-tag next-tag pill-badge">Khóa học đề xuất</span>
          </div>
          <div className="bundle-course-meta">
            <label className="bundle-checkbox-label cursor-pointer">
              <input
                type="checkbox"
                checked={includeBundled}
                onChange={(e) => setIncludeBundled(e.target.checked)}
              />
              <span className="course-title-text line-clamp-2" title={bundledCourse.title}>
                {bundledCourse.title}
              </span>
            </label>
            <div className="bundle-item-price">{formatPrice(bundledCourse.price)}</div>
          </div>
        </div>
      </div>

      {/* Pricing & CTA Action */}
      <div className="bundle-action-bar">
        <div className="bundle-pricing-summary">
          <div className="d-flex align-items-baseline gap-2">
            <span className="label-total">Tổng combo:</span>
            <span className="bundle-final-price">{formatPrice(currentTotal)}</span>
            {includeMain && includeBundled && (
              <span className="bundle-original-price">{formatPrice(originalTotal)}</span>
            )}
          </div>
          {includeMain && includeBundled && (
            <div className="bundle-savings-note">
              Tiết kiệm <strong>{formatPrice(savings)}</strong> (Đã áp dụng giảm giá combo {discountPercent}%)
            </div>
          )}
        </div>

        <button
          type="button"
          onClick={handleAddBundleToCart}
          disabled={addingToCart || (!includeMain && !includeBundled)}
          className={`btn-add-bundle-cta cursor-pointer transition-all duration-200 active:scale-95 hover:opacity-90 ${addedSuccess ? 'btn-success-state' : ''}`}
        >
          {addedSuccess ? (
            <>
              <Check size={18} strokeWidth={2} aria-hidden="true" /> Đã thêm combo vào giỏ hàng
            </>
          ) : (
            <>
              <ShoppingCart size={18} strokeWidth={1.75} aria-hidden="true" />
              {includeMain && includeBundled
                ? `Thêm cả combo vào giỏ - Tiết kiệm ${formatPrice(savings)}`
                : 'Thêm khóa học đã chọn vào giỏ'}
            </>
          )}
        </button>
      </div>
    </div>
  );
};

export default CourseBundleCard;
