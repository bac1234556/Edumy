import React, { useState, useEffect } from 'react';
import {
  Search,
  FolderPlus,
  FolderOpen,
  Folder,
  GraduationCap,
  Edit3,
  Trash2,
  X,
  AlertTriangle,
  Loader2
} from 'lucide-react';
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
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flex: '1 1 320px' }}>
          <div style={{ position: 'relative', width: '100%', maxWidth: '380px' }}>
            <Search
              size={17}
              strokeWidth={1.75}
              style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)', color: '#64748b' }}
              aria-hidden="true"
            />
            <input
              type="text"
              placeholder="Tìm kiếm danh mục theo tên hoặc mô tả..."
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
                outline: 'none',
                transition: 'all 0.2s ease'
              }}
            />
          </div>
          <span style={{ fontSize: '0.85rem', color: '#64748b', whiteSpace: 'nowrap' }}>
            Hiển thị <strong className="text-slate-900">{filteredCategories.length}</strong> danh mục
          </span>
        </div>

        <button
          type="button"
          onClick={openCreateModal}
          className="cursor-pointer transition-all duration-200 active:scale-95 hover:opacity-90"
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '0.5rem',
            height: '44px',
            padding: '0 1.25rem',
            background: '#4f46e5',
            color: '#ffffff',
            border: 'none',
            borderRadius: '0.75rem',
            fontWeight: 600,
            fontSize: '0.875rem',
            boxShadow: '0 4px 12px rgba(79, 70, 229, 0.25)'
          }}
        >
          <FolderPlus size={17} strokeWidth={1.75} aria-hidden="true" />
          Thêm Danh mục Mới
        </button>
      </div>

      {/* Categories Table / Card Grid */}
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
                    <Loader2 size={24} strokeWidth={1.75} className="animate-spin mb-2" style={{ display: 'inline-block', color: '#4f46e5' }} />
                    <div>Đang tải danh sách danh mục...</div>
                  </td>
                </tr>
              ) : filteredCategories.length === 0 ? (
                <tr>
                  <td colSpan="5" style={{ padding: '3rem', textAlign: 'center', color: '#64748b' }}>
                    <FolderOpen size={36} strokeWidth={1.5} style={{ display: 'inline-block', marginBottom: '0.5rem', color: '#94a3b8' }} />
                    <div>Chưa có danh mục nào phù hợp với tìm kiếm.</div>
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
                            width: '38px',
                            height: '38px',
                            borderRadius: '0.75rem',
                            background: '#eef2ff',
                            border: '1px solid #e0e7ff',
                            color: '#4f46e5',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            flexShrink: 0
                          }}>
                            <Folder size={18} strokeWidth={1.75} aria-hidden="true" />
                          </div>
                          <div>
                            <div className="line-clamp-1" style={{ fontWeight: 700, color: '#0f172a' }}>{cat.name}</div>
                            {cat.parentCategoryId && (
                              <div style={{ fontSize: '0.75rem', color: '#64748b' }}>
                                Danh mục con (Cha: #{cat.parentCategoryId})
                              </div>
                            )}
                          </div>
                        </div>
                      </td>
                      <td style={{ padding: '1rem 1.25rem', color: '#475569', maxWidth: '320px' }}>
                        {cat.description ? (
                          <span className="line-clamp-2">{cat.description}</span>
                        ) : (
                          <span style={{ color: '#94a3b8', fontStyle: 'italic' }}>Không có mô tả</span>
                        )}
                      </td>
                      <td style={{ padding: '1rem 1.25rem', textAlign: 'center' }}>
                        <span className="pill-badge" style={{
                          background: coursesCount > 0 ? '#eef2ff' : '#f1f5f9',
                          color: coursesCount > 0 ? '#4f46e5' : '#64748b',
                          border: `1px solid ${coursesCount > 0 ? '#e0e7ff' : '#e2e8f0'}`
                        }}>
                          <GraduationCap size={13} strokeWidth={1.75} aria-hidden="true" />
                          {coursesCount} khóa học
                        </span>
                      </td>
                      <td style={{ padding: '1rem 1.25rem', textAlign: 'right' }}>
                        <div style={{ display: 'inline-flex', gap: '0.5rem' }}>
                          <button
                            type="button"
                            onClick={() => openEditModal(cat)}
                            title="Chỉnh sửa danh mục"
                            className="cursor-pointer transition-all duration-200 active:scale-95"
                            style={{
                              padding: '0.45rem 0.75rem',
                              background: '#f8fafc',
                              border: '1px solid #cbd5e1',
                              borderRadius: '0.65rem',
                              color: '#334155',
                              fontSize: '0.825rem',
                              fontWeight: 600,
                              display: 'inline-flex',
                              alignItems: 'center',
                              gap: '0.35rem'
                            }}
                          >
                            <Edit3 size={14} strokeWidth={1.75} style={{ color: '#4f46e5' }} aria-hidden="true" />
                            Sửa
                          </button>
                          <button
                            type="button"
                            onClick={() => setDeleteTarget(cat)}
                            title="Xóa danh mục"
                            className="cursor-pointer transition-all duration-200 active:scale-95"
                            style={{
                              padding: '0.45rem 0.75rem',
                              background: '#fef2f2',
                              border: '1px solid #fecaca',
                              borderRadius: '0.65rem',
                              color: '#b91c1c',
                              fontSize: '0.825rem',
                              fontWeight: 600,
                              display: 'inline-flex',
                              alignItems: 'center',
                              gap: '0.35rem'
                            }}
                          >
                            <Trash2 size={14} strokeWidth={1.75} aria-hidden="true" />
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
              maxWidth: '520px',
              padding: '1.75rem',
              boxShadow: '0 20px 40px -12px rgba(15, 23, 42, 0.2)',
              border: '1px solid rgba(226, 232, 240, 0.9)'
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
                <div style={{
                  width: '40px',
                  height: '40px',
                  borderRadius: '0.75rem',
                  background: '#eef2ff',
                  border: '1px solid #e0e7ff',
                  color: '#4f46e5',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center'
                }}>
                  {modalMode === 'create' ? <FolderPlus size={20} strokeWidth={1.75} /> : <Edit3 size={20} strokeWidth={1.75} />}
                </div>
                <h4 style={{ margin: 0, fontSize: '1.2rem', fontWeight: 800, color: '#0f172a' }}>
                  {modalMode === 'create' ? 'Thêm Danh mục Mới' : 'Chỉnh sửa Danh mục'}
                </h4>
              </div>
              <button
                type="button"
                onClick={() => setIsModalOpen(false)}
                className="cursor-pointer transition-all duration-200 active:scale-95"
                style={{ background: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: '0.5rem', width: '34px', height: '34px', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', color: '#64748b' }}
                aria-label="Đóng"
              >
                <X size={18} strokeWidth={1.75} />
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
                  className="focus:ring-primary"
                  style={{
                    width: '100%',
                    height: '44px',
                    padding: '0 0.85rem',
                    borderRadius: '0.75rem',
                    border: '1px solid #cbd5e1',
                    fontSize: '0.9rem',
                    color: '#0f172a',
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
                  className="focus:ring-primary"
                  style={{
                    width: '100%',
                    padding: '0.65rem 0.85rem',
                    borderRadius: '0.75rem',
                    border: '1px solid #cbd5e1',
                    fontSize: '0.9rem',
                    color: '#0f172a',
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
                  className="focus:ring-primary"
                  style={{
                    width: '100%',
                    height: '44px',
                    padding: '0 0.85rem',
                    borderRadius: '0.75rem',
                    border: '1px solid #cbd5e1',
                    fontSize: '0.9rem',
                    color: '#0f172a',
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
                  className="cursor-pointer transition-all duration-200 active:scale-95"
                  style={{
                    height: '44px',
                    padding: '0 1.25rem',
                    background: '#f8fafc',
                    border: '1px solid #cbd5e1',
                    borderRadius: '0.75rem',
                    fontWeight: 600,
                    color: '#475569'
                  }}
                >
                  Hủy bỏ
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="cursor-pointer transition-all duration-200 active:scale-95 hover:opacity-90"
                  style={{
                    height: '44px',
                    padding: '0 1.5rem',
                    background: '#4f46e5',
                    border: 'none',
                    borderRadius: '0.75rem',
                    fontWeight: 700,
                    color: '#ffffff',
                    boxShadow: '0 4px 12px rgba(79, 70, 229, 0.25)'
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
              maxWidth: '460px',
              padding: '1.75rem',
              boxShadow: '0 20px 40px -12px rgba(15, 23, 42, 0.2)',
              border: '1px solid #fecaca'
            }}
          >
            <div style={{ textAlign: 'center', marginBottom: '1.25rem' }}>
              <div style={{
                width: '56px',
                height: '56px',
                borderRadius: '9999px',
                background: '#fef2f2',
                border: '1px solid #fecaca',
                color: '#dc2626',
                display: 'inline-flex',
                alignItems: 'center',
                justifyContent: 'center',
                marginBottom: '1rem'
              }}>
                <AlertTriangle size={26} strokeWidth={1.75} aria-hidden="true" />
              </div>
              <h4 style={{ margin: '0 0 0.5rem 0', fontSize: '1.2rem', fontWeight: 800, color: '#0f172a' }}>
                Xác nhận xóa danh mục?
              </h4>
              <p style={{ margin: 0, fontSize: '0.9rem', color: '#64748b' }}>
                Bạn có chắc chắn muốn xóa danh mục <strong className="text-slate-900">"{deleteTarget.name}"</strong>?
              </p>
              {deleteTarget.coursesCount > 0 && (
                <div style={{
                  marginTop: '1rem',
                  padding: '0.75rem 1rem',
                  background: '#fef2f2',
                  border: '1px solid #fecaca',
                  borderRadius: '0.75rem',
                  color: '#b91c1c',
                  fontSize: '0.825rem',
                  fontWeight: 600,
                  display: 'flex',
                  alignItems: 'flex-start',
                  gap: '0.5rem',
                  textAlign: 'left'
                }}>
                  <AlertTriangle size={16} strokeWidth={2} style={{ flexShrink: 0, marginTop: 2 }} aria-hidden="true" />
                  <span>Cảnh báo: Danh mục này đang chứa {deleteTarget.coursesCount} khóa học. Hệ thống sẽ chặn xóa để bảo vệ dữ liệu khóa học!</span>
                </div>
              )}
            </div>

            <div style={{ display: 'flex', justifyContent: 'center', gap: '0.75rem', marginTop: '1.5rem' }}>
              <button
                type="button"
                onClick={() => setDeleteTarget(null)}
                className="cursor-pointer transition-all duration-200 active:scale-95"
                style={{
                  height: '44px',
                  padding: '0 1.25rem',
                  background: '#f8fafc',
                  border: '1px solid #cbd5e1',
                  borderRadius: '0.75rem',
                  fontWeight: 600,
                  color: '#475569'
                }}
              >
                Hủy
              </button>
              <button
                type="button"
                onClick={handleDelete}
                disabled={deleting}
                className="cursor-pointer transition-all duration-200 active:scale-95 hover:opacity-90"
                style={{
                  height: '44px',
                  padding: '0 1.5rem',
                  background: '#dc2626',
                  border: 'none',
                  borderRadius: '0.75rem',
                  fontWeight: 700,
                  color: '#ffffff',
                  boxShadow: '0 4px 10px rgba(220, 38, 38, 0.25)'
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
