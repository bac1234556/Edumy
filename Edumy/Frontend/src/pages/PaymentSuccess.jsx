import React, { useEffect, useContext } from 'react';
import { Link } from 'react-router-dom';
import { CartContext } from '../context/CartContext';
import { CheckCircle2, BookOpen, ArrowRight } from 'lucide-react';

function PaymentSuccess() {
  const { clearCartState } = useContext(CartContext);

  useEffect(() => {
    if (clearCartState) {
      clearCartState();
    }
  }, [clearCartState]);

  return (
    <div className="container py-5">
      <div className="row justify-content-center">
        <div className="col-md-7 col-lg-6 text-center">
          <div className="card border-0 shadow-lg p-5 rounded-4 bg-white">
            <div 
              className="d-inline-flex align-items-center justify-content-center bg-success bg-opacity-10 text-success rounded-circle mx-auto mb-4" 
              style={{ width: '96px', height: '96px' }}
            >
              <CheckCircle2 size={56} className="text-success" />
            </div>
            
            <h2 className="fw-bold mb-2 text-dark">Thanh toán thành công!</h2>
            <p className="text-muted mb-4 lead" style={{ fontSize: '1.05rem' }}>
              Chúc mừng bạn đã kích hoạt khóa học thành công. Quyền truy cập nội dung bài giảng và tài liệu học tập đã sẵn sàng.
            </p>

            <div className="d-flex flex-column flex-sm-row justify-content-center gap-3">
              <Link to="/my-courses" className="btn btn-primary btn-lg rounded-pill px-4 fw-bold d-inline-flex align-items-center justify-content-center gap-2">
                <BookOpen size={20} />
                Vào học ngay
                <ArrowRight size={18} />
              </Link>
              <Link to="/courses" className="btn btn-outline-secondary btn-lg rounded-pill px-4">
                Khám phá thêm khóa học
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default PaymentSuccess;
