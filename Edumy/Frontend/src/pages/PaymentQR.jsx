import React, { useState, useEffect, useRef, useContext } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import api from '../api/axiosConfig';
import { CartContext } from '../context/CartContext';
import { 
  CheckCircle2, 
  Copy, 
  Check, 
  Clock, 
  ArrowRight, 
  ShieldCheck, 
  RefreshCw, 
  AlertTriangle,
  QrCode,
  BookOpen,
  Sparkles,
  ExternalLink
} from 'lucide-react';
import './PaymentQR.css';

function PaymentQR() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { clearCartState } = useContext(CartContext);

  const orderId = searchParams.get('orderId');
  const initialAmount = searchParams.get('amount');

  const [loading, setLoading] = useState(true);
  const [qrData, setQrData] = useState(null);
  const [error, setError] = useState('');
  const [copiedField, setCopiedField] = useState(null);
  const [timeLeft, setTimeLeft] = useState(600); // 10 minutes in seconds
  const [checking, setChecking] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const [firstCourseId, setFirstCourseId] = useState(null);

  const pollingTimerRef = useRef(null);
  const countdownTimerRef = useRef(null);

  // 1. Fetch VietQR details on mount
  useEffect(() => {
    if (!orderId) {
      navigate('/cart');
      return;
    }

    const fetchQrDetails = async () => {
      try {
        setLoading(true);
        // Call backend VietQR generation endpoint
        const res = await api.get(`/v1/payments/vietqr/${orderId}`);
        setQrData(res.data);
        if (res.data.firstCourseId) {
          setFirstCourseId(res.data.firstCourseId);
        }

        // If order was already paid, show success immediately
        if (res.data.status === 'PAID' || res.data.status === 'Completed') {
          handlePaymentSuccess(res.data.firstCourseId);
        }
      } catch (err) {
        console.error('Failed to load VietQR info:', err);
        // Fallback dynamic QR generation based on user requirements (LPBank, LE VIET BAC, 0398201106)
        const amt = parseInt(initialAmount || '0');
        const transferContent = `EDUMY ${orderId}`;
        const fallbackUrl = `https://img.vietqr.io/image/LPB-0398201106-compact2.png?amount=${amt}&addInfo=EDUMY%20${orderId}&accountName=LE%20VIET%20BAC`;
        
        setQrData({
          orderId: parseInt(orderId),
          amount: amt,
          transferContent: transferContent,
          bankId: 'LPB',
          bankName: 'LPBank (Ngân hàng Bưu điện Liên Việt)',
          accountNo: '0398201106',
          accountName: 'LE VIET BAC',
          qrImageUrl: fallbackUrl,
          status: 'Pending'
        });
      } finally {
        setLoading(false);
      }
    };

    fetchQrDetails();
  }, [orderId, navigate, initialAmount]);

  // 2. Countdown Timer (10:00 minutes)
  useEffect(() => {
    if (isSuccess) return;

    countdownTimerRef.current = setInterval(() => {
      setTimeLeft((prev) => {
        if (prev <= 1) {
          clearInterval(countdownTimerRef.current);
          clearInterval(pollingTimerRef.current);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(countdownTimerRef.current);
  }, [isSuccess]);

  // 3. Polling Mechanism: Check order status every 3 seconds
  useEffect(() => {
    if (!orderId || isSuccess || timeLeft <= 0) return;

    const checkOrderStatus = async () => {
      try {
        const res = await api.get(`/v1/orders/${orderId}/status`);
        if (res.data.isPaid || res.data.status === 'PAID' || res.data.status === 'Completed') {
          handlePaymentSuccess(res.data.firstCourseId);
        }
      } catch (err) {
        // Silent poll error
      }
    };

    pollingTimerRef.current = setInterval(checkOrderStatus, 3000);

    return () => clearInterval(pollingTimerRef.current);
  }, [orderId, isSuccess, timeLeft]);

  // Handle successful payment
  const handlePaymentSuccess = (courseId) => {
    setIsSuccess(true);
    if (pollingTimerRef.current) clearInterval(pollingTimerRef.current);
    if (countdownTimerRef.current) clearInterval(countdownTimerRef.current);

    if (clearCartState) {
      clearCartState();
    }

    const targetCourse = courseId || firstCourseId;
    // Auto redirect after 2.5 seconds
    setTimeout(() => {
      if (targetCourse) {
        navigate(`/my-courses/${targetCourse}/learn`);
      } else {
        navigate('/my-courses');
      }
    }, 2500);
  };

  // Manual Check Button
  const handleManualCheck = async () => {
    if (checking || !orderId) return;
    setChecking(true);
    try {
      const res = await api.get(`/v1/orders/${orderId}/status`);
      if (res.data.isPaid || res.data.status === 'PAID' || res.data.status === 'Completed') {
        handlePaymentSuccess(res.data.firstCourseId);
      } else {
        alert('Hệ thống đang chờ chuyển khoản. Vui lòng quét mã QR hoặc chờ 1-2 phút sau khi chuyển tiền.');
      }
    } catch (err) {
      alert('Chưa nhận được tín hiệu thanh toán. Hãy kiểm tra lại số tài khoản và nội dung chuyển khoản.');
    } finally {
      setChecking(false);
    }
  };

  // Copy to Clipboard
  const handleCopy = (text, fieldName) => {
    if (!text) return;
    navigator.clipboard.writeText(String(text));
    setCopiedField(fieldName);
    setTimeout(() => setCopiedField(null), 2000);
  };

  // Format currency
  const formatMoney = (val) => {
    if (!val && val !== 0) return '0 ₫';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
  };

  // Format countdown minutes & seconds
  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  if (loading) {
    return (
      <div className="payment-qr-container">
        <div className="payment-qr-card p-5 text-center">
          <div className="spinner-border text-primary mb-3" style={{ width: '3rem', height: '3rem' }}></div>
          <h4 className="fw-bold">Đang khởi tạo mã thanh toán VietQR...</h4>
          <p className="text-muted">Hệ thống đang kết nối với cổng thanh toán ngân hàng.</p>
        </div>
      </div>
    );
  }

  // Success view
  if (isSuccess) {
    return (
      <div className="payment-qr-container">
        <div className="payment-qr-card payment-success-card shadow-lg border-0">
          <div className="celebration-icon-box">
            <CheckCircle2 size={56} />
          </div>

          <span className="success-badge-order">Đơn hàng #{orderId}</span>
          <h2 className="fw-bold text-dark mb-2">Thanh Toán Thành Công!</h2>
          <p className="text-muted mb-4 lead" style={{ maxWidth: '480px' }}>
            Hệ thống đã tự động nhận diện khoản thanh toán VietQR và mở khóa toàn bộ khóa học cho bạn.
          </p>

          <div className="d-flex align-items-center gap-2 text-success fw-bold mb-4">
            <Sparkles size={20} />
            Đang tự động chuyển hướng vào phòng học trong giây lát...
          </div>

          <div className="d-flex gap-3">
            <Link
              to={firstCourseId ? `/my-courses/${firstCourseId}/learn` : '/my-courses'}
              className="btn-enter-classroom"
            >
              <BookOpen size={20} />
              Vào phòng học ngay
              <ArrowRight size={18} />
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="payment-qr-container">
      <div className="payment-qr-card">
        {/* Card Header */}
        <div className="payment-qr-header">
          <div className="payment-brand-box">
            <div className="bank-logo-badge">
              <span>LPBank</span>
            </div>
            <div className="payment-title-group">
              <h2>
                <QrCode size={22} className="text-primary" />
                Thanh Toán Quét Mã VietQR
              </h2>
              <p>Mở ứng dụng ngân hàng bất kỳ để quét mã Napas 247</p>
            </div>
          </div>

          <div className="napas-badge">
            <ShieldCheck size={16} />
            VietQR Napas 247 • Tự động kích hoạt
          </div>
        </div>

        {/* Card Body */}
        <div className="payment-qr-body">
          {/* Left: QR Code Display */}
          <div className="payment-qr-preview-col">
            <div className="qr-image-wrapper">
              {qrData?.qrImageUrl ? (
                <img 
                  src={qrData.qrImageUrl} 
                  alt="Mã QR Thanh Toán VietQR LPBank" 
                  title="Quét mã bằng app ngân hàng của bạn"
                />
              ) : (
                <div className="spinner-border text-primary"></div>
              )}
            </div>

            <p className="qr-helper-text">
              Sử dụng <strong>App Ngân hàng</strong> hoặc ví điện tử để quét mã QR thanh toán nhanh chóng, an toàn.
            </p>

            {/* Radar Pulsing Status Indicator */}
            <div className="polling-status-pill">
              <span className="radar-dot"></span>
              Đang chờ thanh toán tự động...
            </div>
          </div>

          {/* Right: Payment Instructions & Copy Details */}
          <div className="payment-details-col">
            <div>
              {/* Countdown Bar */}
              <div className="countdown-box">
                <span className="d-flex align-items-center gap-2">
                  <Clock size={18} />
                  Thời gian chờ thanh toán:
                </span>
                <span className="countdown-timer-value">
                  {formatTime(timeLeft)}
                </span>
              </div>

              {timeLeft <= 0 && (
                <div className="alert alert-warning d-flex align-items-center gap-2 mb-3">
                  <AlertTriangle size={20} />
                  Đơn hàng đã hết hạn chờ thanh toán. Vui lòng tạo đơn mới hoặc liên hệ hỗ trợ.
                </div>
              )}

              {/* Transaction details list */}
              <div className="payment-info-list">
                {/* 1. Bank */}
                <div className="payment-info-item">
                  <span className="info-label">Ngân hàng thụ hưởng</span>
                  <div className="info-value-group">
                    <span className="info-value">LPBank (LienVietPostBank)</span>
                  </div>
                </div>

                {/* 2. Account Number */}
                <div className="payment-info-item">
                  <span className="info-label">Số tài khoản</span>
                  <div className="info-value-group">
                    <span className="info-value font-monospace">{qrData?.accountNo || '0398201106'}</span>
                    <button
                      className={`btn-copy ${copiedField === 'accountNo' ? 'copied' : ''}`}
                      onClick={() => handleCopy(qrData?.accountNo || '0398201106', 'accountNo')}
                      title="Sao chép số tài khoản"
                    >
                      {copiedField === 'accountNo' ? <Check size={14} /> : <Copy size={14} />}
                      {copiedField === 'accountNo' ? 'Đã chép' : 'Sao chép'}
                    </button>
                  </div>
                </div>

                {/* 3. Account Name */}
                <div className="payment-info-item">
                  <span className="info-label">Chủ tài khoản</span>
                  <div className="info-value-group">
                    <span className="info-value text-uppercase">{qrData?.accountName || 'LE VIET BAC'}</span>
                  </div>
                </div>

                {/* 4. Amount */}
                <div className="payment-info-item highlight">
                  <span className="info-label">Số tiền cần thanh toán</span>
                  <div className="info-value-group">
                    <span className="info-value amount">{formatMoney(qrData?.amount)}</span>
                    <button
                      className={`btn-copy ${copiedField === 'amount' ? 'copied' : ''}`}
                      onClick={() => handleCopy(qrData?.amount, 'amount')}
                      title="Sao chép số tiền"
                    >
                      {copiedField === 'amount' ? <Check size={14} /> : <Copy size={14} />}
                      {copiedField === 'amount' ? 'Đã chép' : 'Sao chép'}
                    </button>
                  </div>
                </div>

                {/* 5. Transfer Content (Crucial) */}
                <div className="payment-info-item highlight">
                  <div>
                    <span className="info-label">Nội dung chuyển khoản (Bắt buộc)</span>
                    <small className="text-muted d-block" style={{ fontSize: '0.75rem' }}>
                      Giữ nguyên nội dung này để hệ thống kích hoạt khóa học tự động
                    </small>
                  </div>
                  <div className="info-value-group">
                    <span className="info-value content-code">{qrData?.transferContent || `EDUMY ${orderId}`}</span>
                    <button
                      className={`btn-copy ${copiedField === 'transferContent' ? 'copied' : ''}`}
                      onClick={() => handleCopy(qrData?.transferContent || `EDUMY ${orderId}`, 'transferContent')}
                      title="Sao chép nội dung chuyển khoản"
                    >
                      {copiedField === 'transferContent' ? <Check size={14} /> : <Copy size={14} />}
                      {copiedField === 'transferContent' ? 'Đã chép' : 'Sao chép'}
                    </button>
                  </div>
                </div>
              </div>
            </div>

            {/* Action Buttons */}
            <div className="payment-actions-group">
              <button
                className="btn-manual-check"
                onClick={handleManualCheck}
                disabled={checking}
              >
                <RefreshCw size={18} className={checking ? 'spin' : ''} />
                {checking ? 'Đang kiểm tra giao dịch...' : 'Tôi đã chuyển tiền xong'}
              </button>

              <button
                className="btn-cancel-payment"
                onClick={() => navigate('/cart')}
              >
                Hủy và quay lại giỏ hàng
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default PaymentQR;
