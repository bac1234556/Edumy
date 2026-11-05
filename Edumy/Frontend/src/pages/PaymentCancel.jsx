import { Link } from 'react-router-dom';

function PaymentCancel() {
  return (
    <div className="row justify-content-center mt-5">
      <div className="col-md-6 text-center">
        <div className="glass-card p-5">
          <div className="d-inline-flex bg-danger bg-opacity-10 text-danger p-3 rounded-circle mb-4" style={{width: '80px', height: '80px'}}>
            <i className="bi bi-x-circle-fill" style={{fontSize: '40px', lineHeight: '48px'}}></i>
          </div>
          <h1 className="display-5 fw-bold mb-3">Thanh toán đã bị hủy</h1>
          <p className="lead text-muted mb-4">
            Giao dịch thanh toán đã bị hủy hoặc không thành công. Tài khoản của bạn chưa bị trừ tiền.
          </p>
          <div className="d-flex justify-content-center gap-3">
            <Link to="/cart" className="btn btn-primary rounded-pill px-4">Quay lại Giỏ hàng</Link>
            <Link to="/courses" className="btn btn-outline-primary rounded-pill px-4">Khám phá Khóa học</Link>
          </div>
        </div>
      </div>
    </div>
  );
}

export default PaymentCancel;
