import { Link } from 'react-router-dom';
import { XCircle } from 'lucide-react';

function PaymentCancel() {
  return (
    <div className="container py-5">
      <div className="row justify-content-center mt-4">
        <div className="col-md-6 text-center">
          <div className="edumy-card p-5">
            <div
              className="d-inline-flex align-items-center justify-content-center rounded-circle mb-4"
              style={{
                width: '80px',
                height: '80px',
                backgroundColor: 'var(--color-error-bg, #FEE2E2)',
                color: 'var(--color-error, #DC2626)'
              }}
            >
              <XCircle size={40} strokeWidth={1.75} aria-hidden="true" />
            </div>
            <h1 className="h2 fw-bold mb-3">Thanh toán đã bị hủy</h1>
            <p className="text-secondary mb-4">
              Giao dịch thanh toán đã bị hủy hoặc không thành công. Tài khoản của bạn chưa bị trừ tiền.
            </p>
            <div className="d-flex justify-content-center gap-3 flex-wrap">
              <Link to="/cart" className="btn-edumy-primary">Quay lại Giỏ hàng</Link>
              <Link to="/courses" className="btn-outline-neutral">Khám phá Khóa học</Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default PaymentCancel;
