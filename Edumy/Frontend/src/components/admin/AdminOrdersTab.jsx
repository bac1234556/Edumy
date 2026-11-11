import React, { useState, useEffect } from 'react';
import {
  Search,
  DollarSign,
  Receipt,
  CheckCircle2,
  Clock,
  RefreshCw,
  Mail,
  Eye,
  X,
  PlayCircle,
  Loader2
} from 'lucide-react';
import api from '../../api/axiosConfig';

export default function AdminOrdersTab({ showNotification }) {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL'); // 'ALL', 'PAID', 'PENDING'
  const [selectedOrder, setSelectedOrder] = useState(null);

  const fetchOrders = async () => {
    setLoading(true);
    try {
      const res = await api.get('/admin/orders');
      setOrders(res.data || []);
    } catch (err) {
      console.error('Lỗi tải danh sách đơn hàng:', err);
      showNotification?.('Không thể tải danh sách đơn hàng từ server.', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount || 0);
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    try {
      const date = new Date(dateString);
      return date.toLocaleString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch {
      return dateString;
    }
  };

  // KPI Calculations
  const totalOrders = orders.length;
  const paidOrders = orders.filter(o => o.status === 'PAID' || o.status === 'COMPLETED');
  const pendingOrders = orders.filter(o => o.status === 'PENDING');
  const totalRevenue = paidOrders.reduce((acc, curr) => acc + (Number(curr.totalAmount) || 0), 0);

  // Filters
  const filteredOrders = orders.filter(order => {
    const term = searchTerm.toLowerCase();
    const orderCode = (order.orderCode || `EDUMY${order.orderId}`).toLowerCase();
    const email = (order.customerEmail || '').toLowerCase();
    const name = (order.customerName || '').toLowerCase();
    const txnRef = (order.bankTransactionRef || '').toLowerCase();

    const matchesSearch = !term || orderCode.includes(term) || email.includes(term) || name.includes(term) || txnRef.includes(term);

    const isPaid = order.status === 'PAID' || order.status === 'COMPLETED';
    const isPending = order.status === 'PENDING';

    if (statusFilter === 'PAID') return matchesSearch && isPaid;
    if (statusFilter === 'PENDING') return matchesSearch && isPending;
    return matchesSearch;
  });

  return (
    <div className="admin-tab-content">
      {/* KPI METRIC CARDS */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
        gap: '1.25rem',
        marginBottom: '1.5rem'
      }}>
        {/* Doanh thu thực nhận */}
        <div
          className="bg-white rounded-2xl shadow-sm"
          style={{
            border: '1px solid rgba(226, 232, 240, 0.85)',
            borderLeft: '4px solid #10b981',
            padding: '1.25rem 1.5rem'
          }}
        >
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em', color: '#64748b' }}>
              Doanh thu thực nhận
            </span>
            <div style={{ width: '36px', height: '36px', borderRadius: '0.65rem', background: '#ecfdf5', color: '#059669', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <DollarSign size={18} strokeWidth={1.75} aria-hidden="true" />
            </div>
          </div>
          <div style={{ fontSize: '1.65rem', fontWeight: 800, color: '#0f172a' }}>{formatCurrency(totalRevenue)}</div>
          <div style={{ fontSize: '0.78rem', color: '#64748b', marginTop: '0.25rem' }}>
            Từ {paidOrders.length} đơn hàng thành công
          </div>
        </div>

        {/* Tổng đơn hàng */}
        <div
          className="bg-white rounded-2xl shadow-sm"
          style={{
            border: '1px solid rgba(226, 232, 240, 0.85)',
            padding: '1.25rem 1.5rem'
          }}
        >
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em', color: '#64748b' }}>
              Tổng đơn hàng
            </span>
            <div style={{ width: '36px', height: '36px', borderRadius: '0.65rem', background: '#eef2ff', color: '#4f46e5', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <Receipt size={18} strokeWidth={1.75} aria-hidden="true" />
            </div>
          </div>
          <div style={{ fontSize: '1.65rem', fontWeight: 800, color: '#0f172a' }}>{totalOrders}</div>
          <div style={{ fontSize: '0.78rem', color: '#64748b', marginTop: '0.25rem' }}>Toàn thời gian trên hệ thống</div>
        </div>

        {/* Đơn đã thanh toán */}
        <div
          className="bg-white rounded-2xl shadow-sm"
          style={{
            border: '1px solid rgba(226, 232, 240, 0.85)',
            padding: '1.25rem 1.5rem'
          }}
        >
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em', color: '#64748b' }}>
              Đã thanh toán (PAID)
            </span>
            <div style={{ width: '36px', height: '36px', borderRadius: '0.65rem', background: '#ecfdf5', color: '#10b981', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <CheckCircle2 size={18} strokeWidth={1.75} aria-hidden="true" />
            </div>
          </div>
          <div style={{ fontSize: '1.65rem', fontWeight: 800, color: '#059669' }}>{paidOrders.length}</div>
          <div style={{ fontSize: '0.78rem', color: '#64748b', marginTop: '0.25rem' }}>
            Tỉ lệ: {totalOrders > 0 ? Math.round((paidOrders.length / totalOrders) * 100) : 0}% thành công
          </div>
        </div>

        {/* Đơn chờ thanh toán */}
        <div
          className="bg-white rounded-2xl shadow-sm"
          style={{
            border: '1px solid rgba(226, 232, 240, 0.85)',
            padding: '1.25rem 1.5rem'
          }}
        >
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em', color: '#64748b' }}>
              Chờ thanh toán (PENDING)
            </span>
            <div style={{ width: '36px', height: '36px', borderRadius: '0.65rem', background: '#fffbeb', color: '#d97706', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <Clock size={18} strokeWidth={1.75} aria-hidden="true" />
            </div>
          </div>
          <div style={{ fontSize: '1.65rem', fontWeight: 800, color: '#d97706' }}>{pendingOrders.length}</div>
          <div style={{ fontSize: '0.78rem', color: '#64748b', marginTop: '0.25rem' }}>Đang đợi quét mã VietQR</div>
        </div>
      </div>

      {/* FILTER & SEARCH CONTROLS */}
      <div
        className="bg-white rounded-2xl shadow-sm"
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '1rem',
          marginBottom: '1.5rem',
          padding: '1.25rem 1.5rem',
          border: '1px solid rgba(226, 232, 240, 0.85)'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flex: '1 1 360px' }}>
          <div style={{ position: 'relative', width: '100%', maxWidth: '420px' }}>
            <Search
              size={17}
              strokeWidth={1.75}
              style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)', color: '#64748b' }}
              aria-hidden="true"
            />
            <input
              type="text"
              placeholder="Tra cứu theo mã đơn (EDUMY...), email khách hàng hoặc mã giao dịch..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="focus:ring-primary"
              style={{
                width: '100%',
                height: '44px',
                padding: '0 1rem 0 2.5rem',
                borderRadius: '0.75rem',
                border: '1px solid #cbd5e1',
                fontSize: '0.9rem',
                color: '#0f172a',
                background: '#f8fafc',
                outline: 'none'
              }}
            />
          </div>
        </div>

        {/* Status Filter Tabs */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
          <span style={{ fontSize: '0.85rem', fontWeight: 600, color: '#64748b', marginRight: '0.25rem' }}>
            Trạng thái:
          </span>
          <button
            type="button"
            onClick={() => setStatusFilter('ALL')}
            className="cursor-pointer transition-all duration-200 active:scale-95"
            style={{
              height: '40px',
              padding: '0 0.9rem',
              borderRadius: '0.75rem',
              border: statusFilter === 'ALL' ? '1px solid #4f46e5' : '1px solid #cbd5e1',
              background: statusFilter === 'ALL' ? '#eef2ff' : '#ffffff',
              color: statusFilter === 'ALL' ? '#4f46e5' : '#475569',
              fontWeight: 600,
              fontSize: '0.825rem'
            }}
          >
            Tất cả ({orders.length})
          </button>
          <button
            type="button"
            onClick={() => setStatusFilter('PAID')}
            className="cursor-pointer transition-all duration-200 active:scale-95"
            style={{
              height: '40px',
              padding: '0 0.9rem',
              borderRadius: '0.75rem',
              border: statusFilter === 'PAID' ? '1px solid #10b981' : '1px solid #cbd5e1',
              background: statusFilter === 'PAID' ? '#ecfdf5' : '#ffffff',
              color: statusFilter === 'PAID' ? '#047857' : '#475569',
              fontWeight: 600,
              fontSize: '0.825rem',
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.35rem'
            }}
          >
            <CheckCircle2 size={14} strokeWidth={2} aria-hidden="true" />
            Đã thanh toán ({paidOrders.length})
          </button>
          <button
            type="button"
            onClick={() => setStatusFilter('PENDING')}
            className="cursor-pointer transition-all duration-200 active:scale-95"
            style={{
              height: '40px',
              padding: '0 0.9rem',
              borderRadius: '0.75rem',
              border: statusFilter === 'PENDING' ? '1px solid #f59e0b' : '1px solid #cbd5e1',
              background: statusFilter === 'PENDING' ? '#fffbeb' : '#ffffff',
              color: statusFilter === 'PENDING' ? '#b45309' : '#475569',
              fontWeight: 600,
              fontSize: '0.825rem',
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.35rem'
            }}
          >
            <Clock size={14} strokeWidth={2} aria-hidden="true" />
            Chờ thanh toán ({pendingOrders.length})
          </button>
          <button
            type="button"
            onClick={fetchOrders}
            title="Làm mới danh sách đơn hàng"
            aria-label="Làm mới danh sách đơn hàng"
            className="cursor-pointer transition-all duration-200 active:scale-95"
            style={{
              height: '40px',
              width: '40px',
              borderRadius: '0.75rem',
              border: '1px solid #cbd5e1',
              background: '#f8fafc',
              color: '#334155',
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}
          >
            <RefreshCw size={16} strokeWidth={1.75} aria-hidden="true" />
          </button>
        </div>
      </div>

      {/* ORDERS TABLE */}
      <div
        className="bg-white rounded-2xl shadow-sm"
        style={{
          border: '1px solid rgba(226, 232, 240, 0.85)',
          overflow: 'hidden'
        }}
      >
        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.88rem' }}>
            <thead>
              <tr style={{ background: '#f8fafc', borderBottom: '1px solid #e2e8f0', color: '#475569', fontWeight: 700 }}>
                <th style={{ padding: '1rem 1.25rem' }}>Mã Đơn hàng</th>
                <th style={{ padding: '1rem 1.25rem' }}>Khách hàng</th>
                <th style={{ padding: '1rem 1.25rem' }}>Khóa học</th>
                <th style={{ padding: '1rem 1.25rem' }}>Số tiền (VNĐ)</th>
                <th style={{ padding: '1rem 1.25rem' }}>Mã GD / Tham chiếu</th>
                <th style={{ padding: '1rem 1.25rem' }}>Thời gian</th>
                <th style={{ padding: '1rem 1.25rem', textAlign: 'center' }}>Trạng thái</th>
                <th style={{ padding: '1rem 1.25rem', textAlign: 'right' }}>Chi tiết</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="8" style={{ padding: '3rem', textAlign: 'center', color: '#64748b' }}>
                    <Loader2 size={24} strokeWidth={1.75} className="animate-spin mb-2" style={{ display: 'inline-block', color: '#4f46e5' }} />
                    <div>Đang tải dữ liệu đơn hàng...</div>
                  </td>
                </tr>
              ) : filteredOrders.length === 0 ? (
                <tr>
                  <td colSpan="8" style={{ padding: '3rem', textAlign: 'center', color: '#64748b' }}>
                    <Receipt size={36} strokeWidth={1.5} style={{ display: 'inline-block', marginBottom: '0.5rem', color: '#94a3b8' }} />
                    <div>Không tìm thấy đơn hàng nào phù hợp với bộ lọc.</div>
                  </td>
                </tr>
              ) : (
                filteredOrders.map((order) => {
                  const isPaid = order.status === 'PAID' || order.status === 'COMPLETED';
                  const orderCode = order.orderCode || `EDUMY${order.orderId}`;
                  const courseTitles = (order.courses || []).map(c => c.title);

                  return (
                    <tr
                      key={order.orderId}
                      style={{ borderBottom: '1px solid #f1f5f9', transition: 'background-color 0.15s' }}
                      onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f8fafc'}
                      onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                    >
                      {/* Mã đơn */}
                      <td style={{ padding: '1rem 1.25rem' }}>
                        <span className="pill-badge" style={{
                          background: '#f8fafc',
                          border: '1px solid #e2e8f0',
                          color: '#0f172a',
                          fontFamily: 'monospace',
                          fontWeight: 700,
                          fontSize: '0.82rem'
                        }}>
                          <Receipt size={13} strokeWidth={1.75} style={{ color: '#4f46e5' }} aria-hidden="true" />
                          {orderCode}
                        </span>
                      </td>

                      {/* Khách hàng */}
                      <td style={{ padding: '1rem 1.25rem' }}>
                        <div className="line-clamp-1" style={{ fontWeight: 700, color: '#0f172a' }}>{order.customerName}</div>
                        <div className="line-clamp-1" style={{ fontSize: '0.78rem', color: '#64748b', display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                          <Mail size={12} strokeWidth={1.75} aria-hidden="true" />
                          {order.customerEmail}
                        </div>
                      </td>

                      {/* Khóa học */}
                      <td style={{ padding: '1rem 1.25rem', maxWidth: '240px' }}>
                        {courseTitles.length > 0 ? (
                          <div>
                            <div className="line-clamp-1" style={{ fontWeight: 600, color: '#0f172a' }} title={courseTitles.join(', ')}>
                              {courseTitles[0]}
                            </div>
                            {courseTitles.length > 1 && (
                              <span style={{ fontSize: '0.75rem', color: '#4f46e5', fontWeight: 600 }}>
                                + {courseTitles.length - 1} khóa học khác
                              </span>
                            )}
                          </div>
                        ) : (
                          <span style={{ color: '#94a3b8', fontStyle: 'italic' }}>Không có dữ liệu</span>
                        )}
                      </td>

                      {/* Số tiền */}
                      <td style={{ padding: '1rem 1.25rem', fontWeight: 800, color: isPaid ? '#059669' : '#0f172a', whiteSpace: 'nowrap' }}>
                        {formatCurrency(order.totalAmount)}
                      </td>

                      {/* Mã giao dịch ngân hàng */}
                      <td style={{ padding: '1rem 1.25rem', fontFamily: 'monospace', fontSize: '0.82rem', color: '#475569' }}>
                        <span style={{
                          padding: '0.2rem 0.55rem',
                          background: '#f8fafc',
                          borderRadius: '0.5rem',
                          border: '1px solid #e2e8f0'
                        }}>
                          {order.bankTransactionRef || `EDUMY ${order.orderId}`}
                        </span>
                      </td>

                      {/* Thời gian */}
                      <td style={{ padding: '1rem 1.25rem', color: '#64748b', fontSize: '0.82rem', whiteSpace: 'nowrap' }}>
                        {formatDate(order.createdAt)}
                      </td>

                      {/* Badge trạng thái */}
                      <td style={{ padding: '1rem 1.25rem', textAlign: 'center' }}>
                        {isPaid ? (
                          <span className="pill-badge" style={{
                            background: '#ecfdf5',
                            color: '#047857',
                            border: '1px solid #a7f3d0'
                          }}>
                            <CheckCircle2 size={12} strokeWidth={2} aria-hidden="true" />
                            PAID
                          </span>
                        ) : (
                          <span className="pill-badge" style={{
                            background: '#fffbeb',
                            color: '#b45309',
                            border: '1px solid #fde68a'
                          }}>
                            <Clock size={12} strokeWidth={2} aria-hidden="true" />
                            PENDING
                          </span>
                        )}
                      </td>

                      {/* Thao tác */}
                      <td style={{ padding: '1rem 1.25rem', textAlign: 'right' }}>
                        <button
                          type="button"
                          onClick={() => setSelectedOrder(order)}
                          className="cursor-pointer transition-all duration-200 active:scale-95"
                          style={{
                            padding: '0.4rem 0.75rem',
                            background: '#f8fafc',
                            border: '1px solid #cbd5e1',
                            borderRadius: '0.65rem',
                            color: '#4f46e5',
                            fontWeight: 600,
                            fontSize: '0.82rem',
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: '0.3rem'
                          }}
                        >
                          <Eye size={14} strokeWidth={1.75} aria-hidden="true" />
                          Xem
                        </button>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* MODAL CHI TIẾT ĐƠN HÀNG */}
      {selectedOrder && (
        <div style={{
          position: 'fixed',
          inset: 0,
          background: 'rgba(15, 23, 42, 0.55)',
          backdropFilter: 'blur(4px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 9999,
          padding: '1rem'
        }}>
          <div
            className="bg-white rounded-2xl shadow-sm"
            style={{
              width: '100%',
              maxWidth: '580px',
              padding: '1.75rem',
              boxShadow: '0 20px 40px -12px rgba(15, 23, 42, 0.2)',
              border: '1px solid rgba(226, 232, 240, 0.9)'
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem', borderBottom: '1px solid #f1f5f9', paddingBottom: '1rem' }}>
              <div>
                <span style={{ fontSize: '0.725rem', textTransform: 'uppercase', color: '#64748b', fontWeight: 700, letterSpacing: '0.05em' }}>
                  Chi tiết Đơn hàng
                </span>
                <h4 style={{ margin: 0, fontSize: '1.3rem', fontWeight: 800, color: '#0f172a' }}>
                  {selectedOrder.orderCode || `EDUMY${selectedOrder.orderId}`}
                </h4>
              </div>
              <button
                type="button"
                onClick={() => setSelectedOrder(null)}
                className="cursor-pointer transition-all duration-200 active:scale-95"
                style={{ background: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: '0.5rem', width: '34px', height: '34px', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', color: '#64748b' }}
                aria-label="Đóng"
              >
                <X size={18} strokeWidth={1.75} />
              </button>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1.25rem' }}>
              <div>
                <div style={{ fontSize: '0.75rem', color: '#64748b', fontWeight: 600 }}>Khách hàng</div>
                <div style={{ fontWeight: 700, color: '#0f172a' }}>{selectedOrder.customerName}</div>
                <div style={{ fontSize: '0.82rem', color: '#475569' }}>{selectedOrder.customerEmail}</div>
              </div>
              <div>
                <div style={{ fontSize: '0.75rem', color: '#64748b', fontWeight: 600 }}>Trạng thái thanh toán</div>
                <div style={{ marginTop: '0.25rem' }}>
                  {(selectedOrder.status === 'PAID' || selectedOrder.status === 'COMPLETED') ? (
                    <span className="pill-badge" style={{ background: '#ecfdf5', color: '#047857', border: '1px solid #a7f3d0' }}>
                      <CheckCircle2 size={12} strokeWidth={2} aria-hidden="true" /> ĐÃ THANH TOÁN (PAID)
                    </span>
                  ) : (
                    <span className="pill-badge" style={{ background: '#fffbeb', color: '#b45309', border: '1px solid #fde68a' }}>
                      <Clock size={12} strokeWidth={2} aria-hidden="true" /> ĐANG CHỜ (PENDING)
                    </span>
                  )}
                </div>
              </div>
              <div>
                <div style={{ fontSize: '0.75rem', color: '#64748b', fontWeight: 600 }}>Thời gian tạo</div>
                <div style={{ fontWeight: 600, color: '#0f172a', fontSize: '0.88rem' }}>{formatDate(selectedOrder.createdAt)}</div>
              </div>
              <div>
                <div style={{ fontSize: '0.75rem', color: '#64748b', fontWeight: 600 }}>Mã đối soát Ngân hàng</div>
                <div style={{ fontFamily: 'monospace', fontWeight: 700, color: '#4f46e5', fontSize: '0.88rem' }}>
                  {selectedOrder.bankTransactionRef || `EDUMY ${selectedOrder.orderId}`}
                </div>
              </div>
            </div>

            <div style={{ marginBottom: '1.5rem' }}>
              <div style={{ fontSize: '0.85rem', fontWeight: 700, color: '#334155', marginBottom: '0.6rem' }}>
                Danh sách khóa học trong đơn ({selectedOrder.courses?.length || 0}):
              </div>
              <div style={{ background: '#f8fafc', borderRadius: '0.75rem', border: '1px solid #e2e8f0', padding: '0.75rem 1rem' }}>
                {(selectedOrder.courses && selectedOrder.courses.length > 0) ? (
                  selectedOrder.courses.map((c, i) => (
                    <div key={i} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '0.75rem', padding: '0.45rem 0', borderBottom: i < selectedOrder.courses.length - 1 ? '1px solid #e2e8f0' : 'none' }}>
                      <div className="line-clamp-1" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.88rem', fontWeight: 600, color: '#0f172a' }}>
                        <PlayCircle size={15} strokeWidth={1.75} style={{ color: '#4f46e5', flexShrink: 0 }} aria-hidden="true" />
                        <span>{c.title}</span>
                      </div>
                      <div style={{ fontWeight: 700, color: '#059669', fontSize: '0.88rem', flexShrink: 0 }}>
                        {formatCurrency(c.price)}
                      </div>
                    </div>
                  ))
                ) : (
                  <div style={{ color: '#94a3b8', fontStyle: 'italic', fontSize: '0.85rem' }}>Không có chi tiết khóa học</div>
                )}
              </div>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderTop: '1px solid #f1f5f9', paddingTop: '1rem' }}>
              <span style={{ fontSize: '1rem', fontWeight: 700, color: '#334155' }}>Tổng cộng thanh toán:</span>
              <span style={{ fontSize: '1.4rem', fontWeight: 800, color: '#059669' }}>
                {formatCurrency(selectedOrder.totalAmount)}
              </span>
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '1.5rem' }}>
              <button
                type="button"
                onClick={() => setSelectedOrder(null)}
                className="cursor-pointer transition-all duration-200 active:scale-95"
                style={{
                  height: '44px',
                  padding: '0 1.5rem',
                  background: '#0f172a',
                  color: '#ffffff',
                  border: 'none',
                  borderRadius: '0.75rem',
                  fontWeight: 600
                }}
              >
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
