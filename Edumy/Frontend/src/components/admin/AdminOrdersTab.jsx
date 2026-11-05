import React, { useState, useEffect } from 'react';
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
        <div style={{
          background: 'linear-gradient(135deg, #059669, #10b981)',
          color: '#ffffff',
          borderRadius: '12px',
          padding: '1.25rem 1.5rem',
          boxShadow: '0 4px 12px rgba(16, 185, 129, 0.25)'
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em', opacity: 0.9 }}>
              Doanh thu thực nhận
            </span>
            <i className="bi bi-cash-stack" style={{ fontSize: '1.5rem', opacity: 0.85 }}></i>
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800 }}>{formatCurrency(totalRevenue)}</div>
          <div style={{ fontSize: '0.78rem', opacity: 0.85, marginTop: '0.25rem' }}>
            Từ {paidOrders.length} đơn hàng thành công
          </div>
        </div>

        {/* Tổng đơn hàng */}
        <div style={{
          background: '#ffffff',
          border: '1px solid #e2e8f0',
          borderRadius: '12px',
          padding: '1.25rem 1.5rem',
          boxShadow: '0 2px 8px rgba(15, 23, 42, 0.04)'
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em', color: '#64748b' }}>
              Tổng đơn hàng
            </span>
            <div style={{ width: '36px', height: '36px', borderRadius: '8px', background: '#eff6ff', color: '#2563eb', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <i className="bi bi-receipt-cutoff" style={{ fontSize: '1.15rem' }}></i>
            </div>
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: '#0f172a' }}>{totalOrders}</div>
          <div style={{ fontSize: '0.78rem', color: '#64748b', marginTop: '0.25rem' }}>Toàn thời gian trên hệ thống</div>
        </div>

        {/* Đơn đã thanh toán */}
        <div style={{
          background: '#ffffff',
          border: '1px solid #e2e8f0',
          borderRadius: '12px',
          padding: '1.25rem 1.5rem',
          boxShadow: '0 2px 8px rgba(15, 23, 42, 0.04)'
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em', color: '#64748b' }}>
              Đã thanh toán (PAID)
            </span>
            <div style={{ width: '36px', height: '36px', borderRadius: '8px', background: '#ecfdf5', color: '#10b981', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <i className="bi bi-check-circle-fill" style={{ fontSize: '1.15rem' }}></i>
            </div>
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: '#10b981' }}>{paidOrders.length}</div>
          <div style={{ fontSize: '0.78rem', color: '#64748b', marginTop: '0.25rem' }}>Tỉ lệ: {totalOrders > 0 ? Math.round((paidOrders.length / totalOrders) * 100) : 0}% thành công</div>
        </div>

        {/* Đơn chờ thanh toán */}
        <div style={{
          background: '#ffffff',
          border: '1px solid #e2e8f0',
          borderRadius: '12px',
          padding: '1.25rem 1.5rem',
          boxShadow: '0 2px 8px rgba(15, 23, 42, 0.04)'
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em', color: '#64748b' }}>
              Chờ thanh toán (PENDING)
            </span>
            <div style={{ width: '36px', height: '36px', borderRadius: '8px', background: '#fffbeb', color: '#f59e0b', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <i className="bi bi-clock-history" style={{ fontSize: '1.15rem' }}></i>
            </div>
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: '#f59e0b' }}>{pendingOrders.length}</div>
          <div style={{ fontSize: '0.78rem', color: '#64748b', marginTop: '0.25rem' }}>Đang đợi quét mã VietQR</div>
        </div>
      </div>

      {/* FILTER & SEARCH CONTROLS */}
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        flexWrap: 'wrap',
        gap: '1rem',
        marginBottom: '1.5rem',
        background: '#ffffff',
        padding: '1.25rem 1.5rem',
        borderRadius: '12px',
        border: '1px solid #e2e8f0',
        boxShadow: '0 1px 3px rgba(0,0,0,0.04)'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flex: '1 1 360px' }}>
          <div style={{ position: 'relative', width: '100%', maxWidth: '420px' }}>
            <i className="bi bi-search" style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: '#94a3b8' }}></i>
            <input
              type="text"
              placeholder="Tra cứu theo mã đơn (EDUMY...), email khách hàng hoặc mã giao dịch..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              style={{
                width: '100%',
                padding: '0.6rem 1rem 0.6rem 2.4rem',
                borderRadius: '8px',
                border: '1px solid #cbd5e1',
                fontSize: '0.9rem',
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
            onClick={() => setStatusFilter('ALL')}
            style={{
              padding: '0.45rem 0.9rem',
              borderRadius: '8px',
              border: statusFilter === 'ALL' ? '1px solid #2563eb' : '1px solid #cbd5e1',
              background: statusFilter === 'ALL' ? '#eff6ff' : '#ffffff',
              color: statusFilter === 'ALL' ? '#2563eb' : '#475569',
              fontWeight: 600,
              fontSize: '0.85rem',
              cursor: 'pointer'
            }}
          >
            Tất cả ({orders.length})
          </button>
          <button
            onClick={() => setStatusFilter('PAID')}
            style={{
              padding: '0.45rem 0.9rem',
              borderRadius: '8px',
              border: statusFilter === 'PAID' ? '1px solid #10b981' : '1px solid #cbd5e1',
              background: statusFilter === 'PAID' ? '#ecfdf5' : '#ffffff',
              color: statusFilter === 'PAID' ? '#059669' : '#475569',
              fontWeight: 600,
              fontSize: '0.85rem',
              cursor: 'pointer'
            }}
          >
            ✓ Đã thanh toán ({paidOrders.length})
          </button>
          <button
            onClick={() => setStatusFilter('PENDING')}
            style={{
              padding: '0.45rem 0.9rem',
              borderRadius: '8px',
              border: statusFilter === 'PENDING' ? '1px solid #f59e0b' : '1px solid #cbd5e1',
              background: statusFilter === 'PENDING' ? '#fffbeb' : '#ffffff',
              color: statusFilter === 'PENDING' ? '#d97706' : '#475569',
              fontWeight: 600,
              fontSize: '0.85rem',
              cursor: 'pointer'
            }}
          >
            ⏳ Chờ thanh toán ({pendingOrders.length})
          </button>
          <button
            onClick={fetchOrders}
            title="Làm mới danh sách đơn hàng"
            style={{
              padding: '0.45rem 0.75rem',
              borderRadius: '8px',
              border: '1px solid #cbd5e1',
              background: '#f8fafc',
              color: '#334155',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '0.35rem'
            }}
          >
            <i className="bi bi-arrow-clockwise"></i>
          </button>
        </div>
      </div>

      {/* ORDERS TABLE */}
      <div style={{
        background: '#ffffff',
        borderRadius: '12px',
        border: '1px solid #e2e8f0',
        overflow: 'hidden',
        boxShadow: '0 2px 8px rgba(15, 23, 42, 0.04)'
      }}>
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
                    <i className="bi bi-arrow-repeat" style={{ animation: 'spin 1s linear infinite', fontSize: '1.5rem', display: 'inline-block', marginBottom: '0.5rem' }}></i>
                    <div>Đang tải dữ liệu đơn hàng...</div>
                  </td>
                </tr>
              ) : filteredOrders.length === 0 ? (
                <tr>
                  <td colSpan="8" style={{ padding: '3rem', textAlign: 'center', color: '#94a3b8' }}>
                    <i className="bi bi-receipt" style={{ fontSize: '2.5rem', display: 'block', marginBottom: '0.5rem' }}></i>
                    Không tìm thấy đơn hàng nào phù hợp với bộ lọc.
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
                        <span style={{
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: '0.35rem',
                          padding: '0.25rem 0.6rem',
                          borderRadius: '6px',
                          background: '#0f172a',
                          color: '#f8fafc',
                          fontFamily: 'monospace',
                          fontWeight: 700,
                          fontSize: '0.85rem'
                        }}>
                          <i className="bi bi-receipt" style={{ color: '#38bdf8' }}></i>
                          {orderCode}
                        </span>
                      </td>

                      {/* Khách hàng */}
                      <td style={{ padding: '1rem 1.25rem' }}>
                        <div style={{ fontWeight: 700, color: '#0f172a' }}>{order.customerName}</div>
                        <div style={{ fontSize: '0.78rem', color: '#64748b', display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                          <i className="bi bi-envelope"></i>
                          {order.customerEmail}
                        </div>
                      </td>

                      {/* Khóa học */}
                      <td style={{ padding: '1rem 1.25rem', maxWidth: '240px' }}>
                        {courseTitles.length > 0 ? (
                          <div>
                            <div style={{
                              whiteSpace: 'nowrap',
                              overflow: 'hidden',
                              textOverflow: 'ellipsis',
                              fontWeight: 600,
                              color: '#1e293b'
                            }} title={courseTitles.join(', ')}>
                              {courseTitles[0]}
                            </div>
                            {courseTitles.length > 1 && (
                              <span style={{ fontSize: '0.75rem', color: '#2563eb', fontWeight: 600 }}>
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
                          padding: '0.2rem 0.5rem',
                          background: '#f1f5f9',
                          borderRadius: '4px',
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
                          <span style={{
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: '0.35rem',
                            padding: '0.3rem 0.75rem',
                            borderRadius: '9999px',
                            fontSize: '0.78rem',
                            fontWeight: 800,
                            letterSpacing: '0.02em',
                            background: '#dcfce7',
                            color: '#15803d',
                            border: '1px solid #bbf7d0'
                          }}>
                            <i className="bi bi-check-circle-fill"></i>
                            PAID
                          </span>
                        ) : (
                          <span style={{
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: '0.35rem',
                            padding: '0.3rem 0.75rem',
                            borderRadius: '9999px',
                            fontSize: '0.78rem',
                            fontWeight: 800,
                            letterSpacing: '0.02em',
                            background: '#fef3c7',
                            color: '#b45309',
                            border: '1px solid #fde68a'
                          }}>
                            <i className="bi bi-clock-history"></i>
                            PENDING
                          </span>
                        )}
                      </td>

                      {/* Thao tác */}
                      <td style={{ padding: '1rem 1.25rem', textAlign: 'right' }}>
                        <button
                          onClick={() => setSelectedOrder(order)}
                          style={{
                            padding: '0.4rem 0.75rem',
                            background: '#f8fafc',
                            border: '1px solid #cbd5e1',
                            borderRadius: '6px',
                            color: '#2563eb',
                            fontWeight: 600,
                            fontSize: '0.82rem',
                            cursor: 'pointer',
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: '0.3rem',
                            transition: 'all 0.15s'
                          }}
                          onMouseEnter={(e) => { e.currentTarget.style.background = '#eff6ff'; }}
                          onMouseLeave={(e) => { e.currentTarget.style.background = '#f8fafc'; }}
                        >
                          <i className="bi bi-eye-fill"></i>
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
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: 'rgba(15, 23, 42, 0.65)',
          backdropFilter: 'blur(4px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 9999,
          padding: '1rem'
        }}>
          <div style={{
            background: '#ffffff',
            borderRadius: '16px',
            width: '100%',
            maxWidth: '580px',
            padding: '2rem',
            boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
            border: '1px solid #e2e8f0'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem', borderBottom: '1px solid #f1f5f9', paddingBottom: '1rem' }}>
              <div>
                <span style={{ fontSize: '0.75rem', textTransform: 'uppercase', color: '#64748b', fontWeight: 700, letterSpacing: '0.05em' }}>
                  Chi tiết Đơn hàng
                </span>
                <h4 style={{ margin: 0, fontSize: '1.35rem', fontWeight: 800, color: '#0f172a' }}>
                  {selectedOrder.orderCode || `EDUMY${selectedOrder.orderId}`}
                </h4>
              </div>
              <button
                onClick={() => setSelectedOrder(null)}
                style={{ background: 'none', border: 'none', fontSize: '1.25rem', color: '#94a3b8', cursor: 'pointer' }}
              >
                ✕
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
                <div style={{ marginTop: '0.2rem' }}>
                  {(selectedOrder.status === 'PAID' || selectedOrder.status === 'COMPLETED') ? (
                    <span style={{ padding: '0.25rem 0.6rem', borderRadius: '9999px', background: '#dcfce7', color: '#15803d', fontWeight: 800, fontSize: '0.8rem' }}>
                      ✓ ĐÃ THANH TOÁN (PAID)
                    </span>
                  ) : (
                    <span style={{ padding: '0.25rem 0.6rem', borderRadius: '9999px', background: '#fef3c7', color: '#b45309', fontWeight: 800, fontSize: '0.8rem' }}>
                      ⏳ ĐANG CHỜ (PENDING)
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
                <div style={{ fontFamily: 'monospace', fontWeight: 700, color: '#2563eb', fontSize: '0.88rem' }}>
                  {selectedOrder.bankTransactionRef || `EDUMY ${selectedOrder.orderId}`}
                </div>
              </div>
            </div>

            <div style={{ marginBottom: '1.5rem' }}>
              <div style={{ fontSize: '0.85rem', fontWeight: 700, color: '#334155', marginBottom: '0.6rem' }}>
                Danh sách khóa học trong đơn ({selectedOrder.courses?.length || 0}):
              </div>
              <div style={{ background: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0', padding: '0.75rem' }}>
                {(selectedOrder.courses && selectedOrder.courses.length > 0) ? (
                  selectedOrder.courses.map((c, i) => (
                    <div key={i} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.4rem 0', borderBottom: i < selectedOrder.courses.length - 1 ? '1px solid #e2e8f0' : 'none' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.88rem', fontWeight: 600, color: '#1e293b' }}>
                        <i className="bi bi-play-circle-fill" style={{ color: '#2563eb' }}></i>
                        {c.title}
                      </div>
                      <div style={{ fontWeight: 700, color: '#059669', fontSize: '0.88rem' }}>
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
                style={{
                  padding: '0.6rem 1.5rem',
                  background: '#0f172a',
                  color: '#ffffff',
                  border: 'none',
                  borderRadius: '8px',
                  fontWeight: 600,
                  cursor: 'pointer'
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
