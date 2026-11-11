import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Receipt,
  CheckCircle2,
  Clock,
  XCircle,
  Eye,
  QrCode,
  Ban,
  BookOpen,
  Calendar,
  ArrowLeft
} from 'lucide-react';
import api from '../api/axiosConfig';

const formatPrice = (price) => {
  if (!price && price !== 0) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
};

const OrderHistory = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [actionLoadingId, setActionLoadingId] = useState(null);
  const navigate = useNavigate();

  const fetchOrders = async () => {
    setLoading(true);
    try {
      const res = await api.get('/v1/orders');
      setOrders(Array.isArray(res.data) ? res.data : (res.data?.items || []));
    } catch (err) {
      console.error('Failed to load orders:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  const handleViewDetail = async (orderId) => {
    setDetailLoading(true);
    try {
      const res = await api.get(`/v1/orders/${orderId}`);
      setSelectedOrder(res.data);
    } catch (err) {
      alert(err.response?.data?.message || 'Không thể tải chi tiết đơn hàng.');
    } finally {
      setDetailLoading(false);
    }
  };

  const handleCancelOrder = async (orderId) => {
    if (!window.confirm(`Bạn có chắc chắn muốn hủy đơn hàng #${orderId} đang chờ thanh toán không?`)) return;
    setActionLoadingId(orderId);
    try {
      await api.post(`/v1/orders/${orderId}/cancel`);
      await fetchOrders();
      if (selectedOrder && selectedOrder.orderId === orderId) {
        handleViewDetail(orderId);
      }
    } catch (err) {
      alert(err.response?.data?.message || 'Không thể hủy đơn hàng này.');
    } finally {
      setActionLoadingId(null);
    }
  };

  const renderStatusBadge = (status) => {
    const normalized = (status || '').toUpperCase();
    if (normalized === 'PAID' || normalized === 'COMPLETED') {
      return (
        <span style={{
          display: 'inline-flex', alignItems: 'center', gap: 6,
          padding: '0.3rem 0.75rem', borderRadius: 999,
          background: 'rgba(16, 185, 129, 0.15)', color: '#34d399',
          fontSize: '0.8rem', fontWeight: 700
        }}>
          <CheckCircle2 size={14} /> ĐÃ THANH TOÁN (PAID)
        </span>
      );
    }
    if (normalized === 'CANCELLED' || normalized === 'FAILED') {
      return (
        <span style={{
          display: 'inline-flex', alignItems: 'center', gap: 6,
          padding: '0.3rem 0.75rem', borderRadius: 999,
          background: 'rgba(239, 68, 68, 0.15)', color: '#f87171',
          fontSize: '0.8rem', fontWeight: 700
        }}>
          <XCircle size={14} /> ĐÃ HỦY (CANCELLED)
        </span>
      );
    }
    return (
      <span style={{
        display: 'inline-flex', alignItems: 'center', gap: 6,
        padding: '0.3rem 0.75rem', borderRadius: 999,
        background: 'rgba(245, 158, 11, 0.15)', color: '#fbbf24',
        fontSize: '0.8rem', fontWeight: 700
      }}>
        <Clock size={14} /> CHỜ THANH TOÁN (PENDING_PAYMENT)
      </span>
    );
  };

  return (
    <div style={{ maxWidth: 1120, margin: '0 auto', padding: '2.5rem 1.5rem', color: '#f8fafc' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <span style={{ fontSize: '0.78rem', textTransform: 'uppercase', letterSpacing: '0.08em', color: '#818cf8', fontWeight: 700 }}>
            Lịch sử giao dịch
          </span>
          <h1 style={{ fontSize: '1.85rem', fontWeight: 800, marginTop: '0.35rem', display: 'flex', alignItems: 'center', gap: 10 }}>
            <Receipt size={28} color="#818cf8" /> Đơn hàng của tôi
          </h1>
          <p style={{ color: '#94a3b8', fontSize: '0.92rem', marginTop: 4 }}>
            Theo dõi trạng thái đơn hàng, xem chi tiết khóa học theo mức giá tại thời điểm đặt mua, hoặc hủy đơn đang chờ thanh toán.
          </p>
        </div>
        <Link
          to="/courses"
          style={{
            display: 'inline-flex', alignItems: 'center', gap: 8,
            padding: '0.65rem 1.2rem', borderRadius: '0.75rem',
            background: 'rgba(30, 41, 59, 0.8)', border: '1px solid rgba(148, 163, 184, 0.2)',
            color: '#e2e8f0', textDecoration: 'none', fontWeight: 600, fontSize: '0.9rem'
          }}
        >
          <ArrowLeft size={16} /> Tiếp tục tìm khóa học
        </Link>
      </div>

      {loading ? (
        <div style={{ padding: '3rem', textAlign: 'center', color: '#94a3b8' }}>
          Đang tải danh sách đơn hàng...
        </div>
      ) : orders.length === 0 ? (
        <div style={{
          background: 'rgba(15, 23, 42, 0.7)',
          border: '1px solid rgba(148, 163, 184, 0.15)',
          borderRadius: '1.25rem',
          padding: '3.5rem 2rem',
          textAlign: 'center'
        }}>
          <Receipt size={48} color="#64748b" style={{ marginBottom: '1rem' }} />
          <h3 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '0.5rem' }}>Bạn chưa có đơn hàng nào</h3>
          <p style={{ color: '#94a3b8', marginBottom: '1.5rem' }}>
            Các đơn hàng khóa học trả phí của bạn sẽ được lưu trữ đầy đủ tại đây.
          </p>
          <Link
            to="/courses"
            style={{
              display: 'inline-flex', alignItems: 'center', gap: 8,
              padding: '0.75rem 1.5rem', borderRadius: '0.75rem',
              background: 'linear-gradient(135deg, #6366f1, #8b5cf6)',
              color: '#fff', textDecoration: 'none', fontWeight: 700
            }}
          >
            <BookOpen size={18} /> Khám phá khóa học ngay
          </Link>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: selectedOrder ? '1.35fr 1fr' : '1fr', gap: '1.5rem', alignItems: 'start' }}>
          {/* Orders Table */}
          <div style={{
            background: 'rgba(15, 23, 42, 0.75)',
            border: '1px solid rgba(148, 163, 184, 0.15)',
            borderRadius: '1.25rem',
            overflow: 'hidden'
          }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
              <thead>
                <tr style={{ background: 'rgba(30, 41, 59, 0.7)', borderBottom: '1px solid rgba(148, 163, 184, 0.15)', color: '#94a3b8', fontSize: '0.82rem', textTransform: 'uppercase' }}>
                  <th style={{ padding: '1rem 1.25rem' }}>Mã đơn</th>
                  <th style={{ padding: '1rem 1.25rem' }}>Ngày tạo</th>
                  <th style={{ padding: '1rem 1.25rem' }}>Số khóa học</th>
                  <th style={{ padding: '1rem 1.25rem' }}>Tổng tiền</th>
                  <th style={{ padding: '1rem 1.25rem' }}>Trạng thái</th>
                  <th style={{ padding: '1rem 1.25rem', textAlign: 'right' }}>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {orders.map((ord) => {
                  const isPending = (ord.status || '').toUpperCase() === 'PENDING' || (ord.status || '').toUpperCase() === 'PENDING_PAYMENT';
                  return (
                    <tr key={ord.orderId} style={{ borderBottom: '1px solid rgba(148, 163, 184, 0.08)' }}>
                      <td style={{ padding: '1rem 1.25rem', fontWeight: 700, color: '#a5b4fc' }}>
                        #{ord.orderId}
                      </td>
                      <td style={{ padding: '1rem 1.25rem', color: '#cbd5e1', fontSize: '0.88rem' }}>
                        <Calendar size={14} style={{ display: 'inline', marginRight: 5, verticalAlign: -2 }} />
                        {ord.orderDate ? new Date(ord.orderDate).toLocaleString('vi-VN') : 'Vừa xong'}
                      </td>
                      <td style={{ padding: '1rem 1.25rem', color: '#e2e8f0' }}>
                        {ord.itemCount || (ord.items ? ord.items.length : 1)} khóa học
                      </td>
                      <td style={{ padding: '1rem 1.25rem', fontWeight: 700, color: '#f8fafc' }}>
                        {formatPrice(ord.totalAmount)}
                      </td>
                      <td style={{ padding: '1rem 1.25rem' }}>
                        {renderStatusBadge(ord.status)}
                      </td>
                      <td style={{ padding: '1rem 1.25rem', textAlign: 'right' }}>
                        <div style={{ display: 'inline-flex', gap: '0.5rem', flexWrap: 'wrap', justifyContent: 'flex-end' }}>
                          <button
                            type="button"
                            onClick={() => handleViewDetail(ord.orderId)}
                            style={{
                              padding: '0.42rem 0.75rem', borderRadius: '0.5rem',
                              border: '1px solid rgba(99, 102, 241, 0.35)',
                              background: 'rgba(99, 102, 241, 0.12)', color: '#a5b4fc',
                              cursor: 'pointer', fontSize: '0.8rem', fontWeight: 600,
                              display: 'inline-flex', alignItems: 'center', gap: 5
                            }}
                          >
                            <Eye size={14} /> Chi tiết
                          </button>
                          {isPending && (
                            <>
                              <button
                                type="button"
                                onClick={() => navigate(`/payment?orderId=${ord.orderId}`)}
                                style={{
                                  padding: '0.42rem 0.75rem', borderRadius: '0.5rem',
                                  border: 'none', background: '#10b981', color: '#fff',
                                  cursor: 'pointer', fontSize: '0.8rem', fontWeight: 600,
                                  display: 'inline-flex', alignItems: 'center', gap: 5
                                }}
                              >
                                <QrCode size={14} /> Thanh toán QR
                              </button>
                              <button
                                type="button"
                                disabled={actionLoadingId === ord.orderId}
                                onClick={() => handleCancelOrder(ord.orderId)}
                                style={{
                                  padding: '0.42rem 0.75rem', borderRadius: '0.5rem',
                                  border: '1px solid rgba(239, 68, 68, 0.35)',
                                  background: 'rgba(239, 68, 68, 0.12)', color: '#f87171',
                                  cursor: 'pointer', fontSize: '0.8rem', fontWeight: 600,
                                  display: 'inline-flex', alignItems: 'center', gap: 5
                                }}
                              >
                                <Ban size={14} /> Hủy đơn
                              </button>
                            </>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          {/* Order Detail Panel */}
          {selectedOrder && (
            <div style={{
              background: 'rgba(15, 23, 42, 0.9)',
              border: '1px solid rgba(99, 102, 241, 0.35)',
              borderRadius: '1.25rem',
              padding: '1.5rem'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <h3 style={{ fontSize: '1.15rem', fontWeight: 700 }}>
                  Chi tiết Đơn hàng #{selectedOrder.orderId}
                </h3>
                <button
                  type="button"
                  onClick={() => setSelectedOrder(null)}
                  style={{ background: 'transparent', border: 'none', color: '#94a3b8', cursor: 'pointer', fontWeight: 700 }}
                >
                  Đóng ✕
                </button>
              </div>

              {detailLoading ? (
                <p style={{ color: '#94a3b8' }}>Đang tải chi tiết...</p>
              ) : (
                <>
                  <div style={{ marginBottom: '1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ color: '#94a3b8', fontSize: '0.88rem' }}>Trạng thái:</span>
                    {renderStatusBadge(selectedOrder.status)}
                  </div>

                  <div style={{ borderTop: '1px solid rgba(148, 163, 184, 0.15)', paddingTop: '1rem', marginBottom: '1rem' }}>
                    <div style={{ fontSize: '0.82rem', textTransform: 'uppercase', color: '#94a3b8', marginBottom: '0.75rem', fontWeight: 700 }}>
                      Danh sách khóa học (Giá chốt tại thời điểm tạo đơn)
                    </div>
                    {(selectedOrder.items || []).map((item, idx) => (
                      <div
                        key={item.orderDetailId || idx}
                        style={{
                          display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                          padding: '0.75rem', borderRadius: '0.75rem',
                          background: 'rgba(30, 41, 59, 0.6)', marginBottom: '0.5rem'
                        }}
                      >
                        <div>
                          <Link
                            to={`/courses/${item.courseId}`}
                            style={{ color: '#f8fafc', fontWeight: 600, textDecoration: 'none', fontSize: '0.92rem' }}
                          >
                            {item.courseTitle || `Khóa học #${item.courseId}`}
                          </Link>
                          <div style={{ fontSize: '0.78rem', color: '#94a3b8', marginTop: 2 }}>
                            Mã khóa học: #{item.courseId}
                          </div>
                        </div>
                        <div style={{ fontWeight: 700, color: '#a5b4fc' }}>
                          {formatPrice(item.unitPrice ?? item.price)}
                        </div>
                      </div>
                    ))}
                  </div>

                  <div style={{
                    display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                    borderTop: '1px solid rgba(148, 163, 184, 0.15)', paddingTop: '1rem',
                    fontSize: '1.05rem', fontWeight: 800
                  }}>
                    <span>Tổng thanh toán:</span>
                    <span style={{ color: '#34d399' }}>{formatPrice(selectedOrder.totalAmount)}</span>
                  </div>
                </>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default OrderHistory;
