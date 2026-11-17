import { useState, useEffect, useCallback } from 'react';
import { ChevronLeft, ChevronRight, Landmark, Wallet, CheckCircle2, XCircle } from 'lucide-react';
import api from '../../api/axiosConfig';

function AdminWithdrawalsTab({ showNotification }) {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('requested');
  const [page, setPage] = useState(0);
  const [pageMeta, setPageMeta] = useState({ number: 0, size: 10, totalElements: 0, totalPages: 1 });

  const [modalState, setModalState] = useState(null); // { type: 'pay' | 'reject', id, amount }
  const [inputValue, setInputValue] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const fetchWithdrawals = useCallback(async () => {
    try {
      setLoading(true);
      const params = { page, size: 10 };
      if (statusFilter !== 'ALL') {
        params.status = statusFilter;
      }
      const res = await api.get('/v1/admin/withdrawals', { params });
      const data = res.data || {};
      setItems(Array.isArray(data.content) ? data.content : []);
      if (data.page) {
        setPageMeta(data.page);
      }
    } catch (err) {
      showNotification?.(err.response?.data?.message || 'Không thể tải danh sách yêu cầu rút tiền.', 'error');
      setItems([]);
    } finally {
      setLoading(false);
    }
  }, [page, statusFilter, showNotification]);

  useEffect(() => {
    fetchWithdrawals();
  }, [fetchWithdrawals]);

  const handleOpenModal = (type, item) => {
    setModalState({ type, id: item.id, amount: item.amount });
    setInputValue('');
  };

  const handleConfirmAction = async (e) => {
    e.preventDefault();
    if (!modalState || !inputValue.trim()) return;
    try {
      setSubmitting(true);
      if (modalState.type === 'pay') {
        await api.post(`/v1/admin/withdrawals/${modalState.id}/pay`, {
          transferReference: inputValue.trim(),
        });
        showNotification?.('Đã xác nhận chuyển khoản rút tiền thành công!');
      } else {
        await api.post(`/v1/admin/withdrawals/${modalState.id}/reject`, {
          reason: inputValue.trim(),
        });
        showNotification?.('Đã từ chối yêu cầu rút tiền.');
      }
      setModalState(null);
      setInputValue('');
      fetchWithdrawals();
    } catch (err) {
      showNotification?.(err.response?.data?.message || 'Không thể xử lý yêu cầu rút tiền.', 'error');
    } finally {
      setSubmitting(false);
    }
  };

  const renderStatusBadge = (status) => {
    const st = (status || '').toLowerCase();
    if (st === 'paid') return <span className="admin-badge published">Đã chuyển (paid)</span>;
    if (st === 'requested') return <span className="admin-badge pending">Chờ duyệt (requested)</span>;
    if (st === 'rejected') return <span className="admin-badge blocked">Từ chối (rejected)</span>;
    return <span className="admin-badge draft">{status}</span>;
  };

  return (
    <div className="admin-card">
      <div className="admin-card-header">
        <div>
          <h3 className="admin-card-title">
            <Landmark size={18} className="text-primary" aria-hidden="true" />
            Yêu cầu Rút tiền Giảng viên
          </h3>
          <p className="admin-card-subtitle">
            Tổng số {pageMeta.totalElements || 0} yêu cầu (Trang {(pageMeta.number || 0) + 1}/{Math.max(1, pageMeta.totalPages || 1)})
          </p>
        </div>

        <div className="admin-card-toolbar">
          <select
            className="admin-filter-select"
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value);
              setPage(0);
            }}
          >
            <option value="requested">Chờ duyệt (requested)</option>
            <option value="paid">Đã chuyển (paid)</option>
            <option value="rejected">Đã từ chối (rejected)</option>
            <option value="cancelled">Đã hủy (cancelled)</option>
            <option value="ALL">Tất cả trạng thái</option>
          </select>
        </div>
      </div>

      <div className="admin-table-container">
        {loading ? (
          <div style={{ padding: '1.5rem' }}>
            <div className="admin-skeleton-row"></div>
            <div className="admin-skeleton-row"></div>
            <div className="admin-skeleton-row"></div>
          </div>
        ) : items.length === 0 ? (
          <div className="admin-empty-state">
            <div className="admin-empty-icon"><Wallet size={28} aria-hidden="true" /></div>
            <h4 className="admin-empty-title">Không có yêu cầu rút tiền nào</h4>
            <p className="admin-empty-desc">Thử thay đổi bộ lọc trạng thái phía trên.</p>
          </div>
        ) : (
          <table className="admin-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Giảng viên / Chủ TK</th>
                <th>Số tiền</th>
                <th>Tài khoản nhận</th>
                <th>Thời gian tạo</th>
                <th>Trạng thái</th>
                <th style={{ textAlign: 'right' }}>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {items.map((w) => (
                <tr key={w.id}>
                  <td>#{w.id}</td>
                  <td style={{ fontWeight: 600, color: '#0f172a' }}>{w.accountHolder || 'Giảng viên'}</td>
                  <td className="tabular-nums" style={{ fontWeight: 700, color: 'var(--color-success, #15803D)' }}>
                    ₫{(w.amount || 0).toLocaleString('vi-VN')}
                  </td>
                  <td>
                    {w.bankName} ••••{w.accountNumberLast4}
                  </td>
                  <td>
                    {w.createdAt ? new Date(w.createdAt).toLocaleString('vi-VN') : '—'}
                  </td>
                  <td>
                    {renderStatusBadge(w.status)}
                    {w.transferReference && (
                      <div className="small text-muted mt-1">Mã GD: {w.transferReference}</div>
                    )}
                    {w.rejectReason && (
                      <div className="small text-danger mt-1">Lý do: {w.rejectReason}</div>
                    )}
                  </td>
                  <td>
                    <div className="admin-action-btn-group">
                      {w.status === 'requested' && (
                        <>
                          <button
                            type="button"
                            className="btn-admin-action success"
                            onClick={() => handleOpenModal('pay', w)}
                          >
                            <CheckCircle2 size={14} aria-hidden="true" /> Đã chuyển khoản
                          </button>
                          <button
                            type="button"
                            className="btn-admin-action outline-danger"
                            onClick={() => handleOpenModal('reject', w)}
                          >
                            <XCircle size={14} aria-hidden="true" /> Từ chối
                          </button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {!loading && items.length > 0 && (
        <div className="admin-card-footer">
          <div className="admin-pagination-info">
            Trang {(pageMeta.number || 0) + 1} / {Math.max(1, pageMeta.totalPages || 1)}
          </div>
          <div className="edumy-pagination">
            <button
              type="button"
              className="page-btn"
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              disabled={page === 0}
            >
              <ChevronLeft size={16} /> <span>Trước</span>
            </button>
            <button
              type="button"
              className="page-btn"
              onClick={() => setPage((p) => p + 1)}
              disabled={page + 1 >= (pageMeta.totalPages || 1)}
            >
              <span>Tiếp theo</span> <ChevronRight size={16} />
            </button>
          </div>
        </div>
      )}

      {modalState && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }} tabIndex="-1">
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content border-0 shadow-lg rounded-4">
              <div className="modal-header border-bottom">
                <h5 className="modal-title fw-bold">
                  {modalState.type === 'pay'
                    ? `Xác nhận đã chuyển khoản #${modalState.id}`
                    : `Từ chối yêu cầu rút tiền #${modalState.id}`}
                </h5>
                <button type="button" className="btn-close" onClick={() => setModalState(null)}></button>
              </div>
              <form onSubmit={handleConfirmAction}>
                <div className="modal-body p-4">
                  <p className="small text-muted mb-3">
                    Số tiền: <strong>₫{(modalState.amount || 0).toLocaleString('vi-VN')}</strong>
                  </p>
                  <label className="form-label fw-semibold">
                    {modalState.type === 'pay' ? 'Mã giao dịch chuyển khoản (bắt buộc)' : 'Lý do từ chối (bắt buộc)'}
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    value={inputValue}
                    onChange={(e) => setInputValue(e.target.value)}
                    placeholder={modalState.type === 'pay' ? 'VD: FT26321998812' : 'VD: Sai thông tin tài khoản nhận tiền'}
                    required
                    autoFocus
                  />
                </div>
                <div className="modal-footer border-top">
                  <button type="button" className="btn btn-outline-secondary" onClick={() => setModalState(null)}>
                    Hủy
                  </button>
                  <button
                    type="submit"
                    className={`btn ${modalState.type === 'pay' ? 'btn-success' : 'btn-danger'}`}
                    disabled={submitting || !inputValue.trim()}
                  >
                    {submitting ? 'Đang xử lý...' : 'Xác nhận'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default AdminWithdrawalsTab;
