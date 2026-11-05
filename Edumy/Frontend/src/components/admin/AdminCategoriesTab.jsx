import React, { useState, useEffect } from 'react';
import api from '../../api/axiosConfig';

export default function AdminCategoriesTab({ showNotification }) {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  
  // Modal states
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState('create'); // 'create' or 'edit'
  const [selectedCat, setSelectedCat] = useState(null);
  const [formData, setFormData] = useState({ name: '', description: '', parentCategoryId: '' });
  const [submitting, setSubmitting] = useState(false);

  // Delete confirm modal
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);

  const fetchCategories = async () => {
    setLoading(true);
    try {
      const res = await api.get('/admin/categories/stats');
      setCategories(res.data || []);
    } catch (err) {
      console.error('Lỗi tải danh mục:', err);
      // Fallback
      try {
        const fallbackRes = await api.get('/categories');
        setCategories(fallbackRes.data || []);
      } catch (e) {
        showNotification?.('Không thể tải danh sách danh mục.', 'error');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCategories();
  }, []);

  const openCreateModal = () => {
    setModalMode('create');
    setSelectedCat(null);
    setFormData({ name: '', description: '', parentCategoryId: '' });
    setIsModalOpen(true);
  };

  const openEditModal = (cat) => {
    setModalMode('edit');
    setSelectedCat(cat);
    setFormData({
      name: cat.name || '',
      description: cat.description || '',
      parentCategoryId: cat.parentCategoryId || ''
    });
    setIsModalOpen(true);
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    if (!formData.name.trim()) {
      showNotification?.('Vui lòng nhập tên danh mục!', 'error');
      return;
    }

    setSubmitting(true);
    try {
      const payload = {
        name: formData.name.trim(),
        description: formData.description.trim(),
        parentCategoryId: formData.parentCategoryId ? parseInt(formData.parentCategoryId) : null
      };

      if (modalMode === 'create') {
        await api.post('/admin/categories', payload);
        showNotification?.('Thêm danh mục mới thành công!', 'success');
      } else {
        await api.put(`/admin/categories/${selectedCat.categoryId || selectedCat.id}`, payload);
        showNotification?.('Cập nhật danh mục thành công!', 'success');
      }

      setIsModalOpen(false);
      fetchCategories();
    } catch (err) {
      console.error('Lỗi lưu danh mục:', err);
      const msg = err.response?.data?.message || 'Có lỗi xảy ra khi lưu danh mục.';
      showNotification?.(msg, 'error');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    setDeleting(true);
    const catId = deleteTarget.categoryId || deleteTarget.id;
    try {
      await api.delete(`/admin/categories/${catId}`);
      showNotification?.(`Đã xóa danh mục "${deleteTarget.name}" thành công!`, 'success');
      setDeleteTarget(null);
      fetchCategories();
    } catch (err) {
      console.error('Lỗi xóa danh mục:', err);
      const msg = err.response?.data?.message || 'Không thể xóa danh mục này.';
      showNotification?.(msg, 'error');
    } finally {
      setDeleting(false);
    }
  };

  const filteredCategories = categories.filter(c => 
    !searchTerm || 
    c.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    c.description?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="admin-tab-content">
      {/* Top Controls Bar */}
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
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flex: '1 1 320px' }}>
          <div style={{ position: 'relative', width: '100%', maxWidth: '380px' }}>
            <i className="bi bi-search" style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: '#94a3b8' }}></i>
            <input
              type="text"
              placeholder="Tìm kiếm danh mục theo tên hoặc mô tả..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              style={{
                width: '100%',
                padding: '0.6rem 1rem 0.6rem 2.4rem',
                borderRadius: '8px',
                border: '1px solid #cbd5e1',
                fontSize: '0.9rem',
                outline: 'none',
                transition: 'border-color 0.2s'
              }}
            />
          </div>
          <span style={{ fontSize: '0.85rem', color: '#64748b', whiteSpace: 'nowrap' }}>
            Hiển thị <strong>{filteredCategories.length}</strong> danh mục
          </span>
        </div>

        <button
          onClick={openCreateModal}
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '0.5rem',
            padding: '0.65rem 1.25rem',
            background: 'linear-gradient(135deg, #2563eb, #1d4ed8)',
            color: '#ffffff',
            border: 'none',
            borderRadius: '8px',
            fontWeight: 600,
            fontSize: '0.9rem',
            cursor: 'pointer',
            boxShadow: '0 4px 10px rgba(37, 99, 235, 0.25)',
            transition: 'all 0.2s'
          }}
        >
          <i className="bi bi-folder-plus"></i>
          Thêm Danh mục Mới
        </button>
      </div>

      {/* Categories Table / Card Grid */}
      <div style={{
        background: '#ffffff',
        borderRadius: '12px',
        border: '1px solid #e2e8f0',
        overflow: 'hidden',
        boxShadow: '0 2px 8px rgba(15, 23, 42, 0.04)'
      }}>
        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
            <thead>
              <tr style={{ background: '#f8fafc', borderBottom: '1px solid #e2e8f0', color: '#475569', fontWeight: 700 }}>
                <th style={{ padding: '1rem 1.25rem' }}>ID</th>
                <th style={{ padding: '1rem 1.25rem' }}>Tên Danh mục</th>
                <th style={{ padding: '1rem 1.25rem' }}>Mô tả</th>
                <th style={{ padding: '1rem 1.25rem', textAlign: 'center' }}>Số Khóa học</th>
                <th style={{ padding: '1rem 1.25rem', textAlign: 'right' }}>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="5" style={{ padding: '3rem', textAlign: 'center', color: '#64748b' }}>
                    <i className="bi bi-arrow-repeat" style={{ animation: 'spin 1s linear infinite', fontSize: '1.5rem', display: 'inline-block', marginBottom: '0.5rem' }}></i>
                    <div>Đang tải danh sách danh mục...</div>
                  </td>
                </tr>
              ) : filteredCategories.length === 0 ? (
                <tr>
                  <td colSpan="5" style={{ padding: '3rem', textAlign: 'center', color: '#94a3b8' }}>
                    <i className="bi bi-folder2-open" style={{ fontSize: '2.5rem', display: 'block', marginBottom: '0.5rem' }}></i>
                    Chưa có danh mục nào phù hợp với tìm kiếm.
                  </td>
                </tr>
              ) : (
                filteredCategories.map((cat) => {
                  const id = cat.categoryId || cat.id;
                  const coursesCount = cat.coursesCount || 0;
                  return (
                    <tr 
                      key={id}
                      style={{ borderBottom: '1px solid #f1f5f9', transition: 'background-color 0.15s' }}
                      onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f8fafc'}
                      onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                    >
                      <td style={{ padding: '1rem 1.25rem', fontWeight: 600, color: '#64748b' }}>
                        #{id}
                      </td>
                      <td style={{ padding: '1rem 1.25rem' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                          <div style={{
                            width: '36px',
                            height: '36px',
                            borderRadius: '8px',
                            background: '#eff6ff',
                            color: '#2563eb',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            fontSize: '1.15rem'
                          }}>
                            <i className={`bi ${cat.icon || 'bi-folder-fill'}`}></i>
                          </div>
                          <div>
                            <div style={{ fontWeight: 700, color: '#0f172a' }}>{cat.name}</div>
                            {cat.parentCategoryId && (
                              <div style={{ fontSize: '0.75rem', color: '#64748b' }}>
                                Danh mục con (Cha: #{cat.parentCategoryId})
                              </div>
                            )}
                          </div>
                        </div>
                      </td>
                      <td style={{ padding: '1rem 1.25rem', color: '#475569', maxWidth: '320px' }}>
                        {cat.description || <span style={{ color: '#cbd5e1', fontStyle: 'italic' }}>Không có mô tả</span>}
                      </td>
                      <td style={{ padding: '1rem 1.25rem', textAlign: 'center' }}>
                        <span style={{
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: '0.35rem',
                          padding: '0.25rem 0.75rem',
                          borderRadius: '9999px',
                          fontSize: '0.82rem',
                          fontWeight: 700,
                          background: coursesCount > 0 ? '#e0f2fe' : '#f1f5f9',
                          color: coursesCount > 0 ? '#0284c7' : '#94a3b8',
                          border: `1px solid ${coursesCount > 0 ? '#bae6fd' : '#e2e8f0'}`
                        }}>
                          <i className="bi bi-mortarboard-fill"></i>
                          {coursesCount} khóa học
                        </span>
                      </td>
                      <td style={{ padding: '1rem 1.25rem', textAlign: 'right' }}>
                        <div style={{ display: 'inline-flex', gap: '0.5rem' }}>
                          <button
                            onClick={() => openEditModal(cat)}
                            title="Chỉnh sửa danh mục"
                            style={{
                              padding: '0.45rem 0.75rem',
                              background: '#f8fafc',
                              border: '1px solid #cbd5e1',
                              borderRadius: '6px',
                              color: '#334155',
                              cursor: 'pointer',
                              fontSize: '0.85rem',
                              fontWeight: 600,
                              display: 'inline-flex',
                              alignItems: 'center',
                              gap: '0.35rem',
                              transition: 'all 0.15s'
                            }}
                            onMouseEnter={(e) => { e.currentTarget.style.background = '#e2e8f0'; }}
                            onMouseLeave={(e) => { e.currentTarget.style.background = '#f8fafc'; }}
                          >
                            <i className="bi bi-pencil-square" style={{ color: '#2563eb' }}></i>
                            Sửa
                          </button>
                          <button
                            onClick={() => setDeleteTarget(cat)}
                            title="Xóa danh mục"
                            style={{
                              padding: '0.45rem 0.75rem',
                              background: '#fef2f2',
                              border: '1px solid #fecaca',
                              borderRadius: '6px',
                              color: '#dc2626',
                              cursor: 'pointer',
                              fontSize: '0.85rem',
                              fontWeight: 600,
                              display: 'inline-flex',
                              alignItems: 'center',
                              gap: '0.35rem',
                              transition: 'all 0.15s'
                            }}
                            onMouseEnter={(e) => { e.currentTarget.style.background = '#fee2e2'; }}
                            onMouseLeave={(e) => { e.currentTarget.style.background = '#fef2f2'; }}
                          >
                            <i className="bi bi-trash3-fill"></i>
                            Xóa
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* MODAL THÊM / CHỈNH SỬA DANH MỤC */}
      {isModalOpen && (
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
            maxWidth: '520px',
            padding: '2rem',
            boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
            border: '1px solid #e2e8f0'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
                <div style={{
                  width: '38px',
                  height: '38px',
                  borderRadius: '8px',
                  background: '#dbeafe',
                  color: '#2563eb',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '1.2rem'
                }}>
                  <i className={modalMode === 'create' ? 'bi bi-folder-plus' : 'bi bi-pencil-square'}></i>
                </div>
                <h4 style={{ margin: 0, fontSize: '1.25rem', fontWeight: 800, color: '#0f172a' }}>
                  {modalMode === 'create' ? 'Thêm Danh mục Mới' : 'Chỉnh sửa Danh mục'}
                </h4>
              </div>
              <button
                onClick={() => setIsModalOpen(false)}
                style={{ background: 'none', border: 'none', fontSize: '1.25rem', color: '#94a3b8', cursor: 'pointer' }}
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleFormSubmit}>
              <div style={{ marginBottom: '1.25rem' }}>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 700, color: '#334155', marginBottom: '0.4rem' }}>
                  Tên Danh mục <span style={{ color: '#ef4444' }}>*</span>
                </label>
                <input
                  type="text"
                  required
                  placeholder="Ví dụ: Lập trình Web, Trí tuệ nhân tạo..."
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  style={{
                    width: '100%',
                    padding: '0.65rem 0.85rem',
                    borderRadius: '8px',
                    border: '1px solid #cbd5e1',
                    fontSize: '0.92rem',
                    outline: 'none'
                  }}
                />
              </div>

              <div style={{ marginBottom: '1.25rem' }}>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 700, color: '#334155', marginBottom: '0.4rem' }}>
                  Mô tả ngắn
                </label>
                <textarea
                  rows="3"
                  placeholder="Mô tả tóm tắt nội dung các khóa học trong danh mục này..."
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  style={{
                    width: '100%',
                    padding: '0.65rem 0.85rem',
                    borderRadius: '8px',
                    border: '1px solid #cbd5e1',
                    fontSize: '0.92rem',
                    outline: 'none',
                    resize: 'vertical'
                  }}
                />
              </div>

              <div style={{ marginBottom: '1.75rem' }}>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 700, color: '#334155', marginBottom: '0.4rem' }}>
                  Danh mục cha (Tùy chọn)
                </label>
                <select
                  value={formData.parentCategoryId}
                  onChange={(e) => setFormData({ ...formData, parentCategoryId: e.target.value })}
                  style={{
                    width: '100%',
                    padding: '0.65rem 0.85rem',
                    borderRadius: '8px',
                    border: '1px solid #cbd5e1',
                    fontSize: '0.92rem',
                    outline: 'none',
                    background: '#ffffff'
                  }}
                >
                  <option value="">-- Danh mục gốc (Không có cha) --</option>
                  {categories
                    .filter(c => !selectedCat || (c.categoryId || c.id) !== (selectedCat.categoryId || selectedCat.id))
                    .map(c => (
                      <option key={c.categoryId || c.id} value={c.categoryId || c.id}>
                        {c.name}
                      </option>
                    ))}
                </select>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  style={{
                    padding: '0.6rem 1.25rem',
                    background: '#f1f5f9',
                    border: '1px solid #cbd5e1',
                    borderRadius: '8px',
                    fontWeight: 600,
                    color: '#475569',
                    cursor: 'pointer'
                  }}
                >
                  Hủy bỏ
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  style={{
                    padding: '0.6rem 1.5rem',
                    background: 'linear-gradient(135deg, #2563eb, #1d4ed8)',
                    border: 'none',
                    borderRadius: '8px',
                    fontWeight: 700,
                    color: '#ffffff',
                    cursor: submitting ? 'not-allowed' : 'pointer',
                    boxShadow: '0 4px 10px rgba(37, 99, 235, 0.3)'
                  }}
                >
                  {submitting ? 'Đang lưu...' : (modalMode === 'create' ? 'Tạo mới' : 'Lưu thay đổi')}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL XÁC NHẬN XÓA DANH MỤC */}
      {deleteTarget && (
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
            maxWidth: '460px',
            padding: '2rem',
            boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
            border: '1px solid #fecaca'
          }}>
            <div style={{ textAlign: 'center', marginBottom: '1.25rem' }}>
              <div style={{
                width: '56px',
                height: '56px',
                borderRadius: '50%',
                background: '#fee2e2',
                color: '#dc2626',
                display: 'inline-flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '1.75rem',
                marginBottom: '1rem'
              }}>
                <i className="bi bi-exclamation-triangle-fill"></i>
              </div>
              <h4 style={{ margin: '0 0 0.5rem 0', fontSize: '1.25rem', fontWeight: 800, color: '#0f172a' }}>
                Xác nhận xóa danh mục?
              </h4>
              <p style={{ margin: 0, fontSize: '0.9rem', color: '#64748b' }}>
                Bạn có chắc chắn muốn xóa danh mục <strong>"{deleteTarget.name}"</strong>?
              </p>
              {deleteTarget.coursesCount > 0 && (
                <div style={{
                  marginTop: '1rem',
                  padding: '0.75rem',
                  background: '#fef2f2',
                  border: '1px solid #fecaca',
                  borderRadius: '8px',
                  color: '#b91c1c',
                  fontSize: '0.85rem',
                  fontWeight: 600
                }}>
                  ⚠️ Cảnh báo: Danh mục này đang chứa {deleteTarget.coursesCount} khóa học. Hệ thống sẽ chặn xóa để bảo vệ dữ liệu khóa học!
                </div>
              )}
            </div>

            <div style={{ display: 'flex', justifyContent: 'center', gap: '0.75rem', marginTop: '1.5rem' }}>
              <button
                type="button"
                onClick={() => setDeleteTarget(null)}
                style={{
                  padding: '0.6rem 1.25rem',
                  background: '#f1f5f9',
                  border: '1px solid #cbd5e1',
                  borderRadius: '8px',
                  fontWeight: 600,
                  color: '#475569',
                  cursor: 'pointer'
                }}
              >
                Hủy
              </button>
              <button
                type="button"
                onClick={handleDelete}
                disabled={deleting}
                style={{
                  padding: '0.6rem 1.5rem',
                  background: '#dc2626',
                  border: 'none',
                  borderRadius: '8px',
                  fontWeight: 700,
                  color: '#ffffff',
                  cursor: deleting ? 'not-allowed' : 'pointer',
                  boxShadow: '0 4px 10px rgba(220, 38, 38, 0.3)'
                }}
              >
                {deleting ? 'Đang xóa...' : 'Xác nhận Xóa'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
