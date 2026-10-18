import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';

function MockPaymentGateway() {
  const [searchParams] = useSearchParams();
  const orderId = searchParams.get('orderId');
  const amount = searchParams.get('amount');
  const navigate = useNavigate();
  const [processing, setProcessing] = useState(false);

  useEffect(() => {
    if (!orderId) {
      navigate('/cart');
    }
  }, [orderId, navigate]);

  const handlePayment = async (success) => {
    setProcessing(true);
    try {
      await api.post('/payment/callback', {
        orderId: parseInt(orderId),
        success: success
      });
      if (success) {
        navigate('/payment-success');
      } else {
        navigate('/payment-cancel');
      }
    } catch (error) {
      alert('Lỗi xử lý giao dịch thanh toán');
      setProcessing(false);
    }
  };

  return (
    <div className="container mt-5 mb-5 d-flex justify-content-center">
      <div className="card shadow-lg border-0" style={{ maxWidth: '500px', width: '100%' }}>
        <div className="card-header bg-primary text-white text-center py-4">
          <h3 className="mb-0">EduMy Pay</h3>
          <p className="mb-0 opacity-75">Cổng thanh toán thử nghiệm an toàn</p>
        </div>
        <div className="card-body p-4">
          <div className="text-center mb-4">
            <h5 className="text-muted mb-2">Tổng số tiền thanh toán</h5>
            <h1 className="display-4 fw-bold text-primary">₫{parseFloat(amount || 0).toLocaleString()}</h1>
            <p className="text-muted">Mã đơn hàng: #{orderId}</p>
          </div>

          <hr className="my-4" />

          {processing ? (
            <div className="text-center py-4">
              <div className="spinner-border text-primary" role="status">
                <span className="visually-hidden">Đang xử lý...</span>
              </div>
              <p className="mt-3 text-muted">Đang xử lý giao dịch thanh toán của bạn...</p>
            </div>
          ) : (
            <div className="d-grid gap-3">
              <button 
                className="btn btn-success btn-lg fw-bold d-flex align-items-center justify-content-center gap-2"
                onClick={() => handlePayment(true)}
              >
                <i className="bi bi-check-circle-fill"></i>
                Mô phỏng Thanh toán Thành công
              </button>
              
              <button 
                className="btn btn-outline-danger btn-lg fw-bold d-flex align-items-center justify-content-center gap-2"
                onClick={() => handlePayment(false)}
              >
                <i className="bi bi-x-circle-fill"></i>
                Mô phỏng Thanh toán Thất bại
              </button>
            </div>
          )}

          <div className="text-center mt-4 text-muted small">
            <i className="bi bi-shield-lock-fill text-success me-1"></i>
            Đây là cổng thanh toán giả lập phục vụ mục đích học tập và kiểm thử. Tài khoản thực tế của bạn sẽ không bị trừ tiền.
          </div>
        </div>
      </div>
    </div>
  );
}

export default MockPaymentGateway;
