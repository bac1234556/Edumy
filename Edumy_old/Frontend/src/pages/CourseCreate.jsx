import React, { useState, useContext, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';
import './CourseCreate.css';

function CourseCreate() {
  const [step, setStep] = useState(1);
  const [categories, setCategories] = useState([]);
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    level: 'Beginner',
    price: 0,
    categoryId: '',
    thumbnailUrl: ''
  });
  const [thumbnailFile, setThumbnailFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  const [suggestLoading, setSuggestLoading] = useState(false);
  const [suggestion, setSuggestion] = useState(null);
  const [suggestError, setSuggestError] = useState('');

  const handleSuggestCategory = async () => {
    if (!formData.title.trim()) {
      setSuggestError('Vui lòng nhập tiêu đề khoá học ở Bước 1.');
      return;
    }
    setSuggestLoading(true);
    setSuggestError('');
    setSuggestion(null);
    try {
      const res = await api.post('/MLTest/course-classification', {
        title: formData.title,
        description: formData.description
      });
      if (res.data.success) {
        setSuggestion(res.data.suggestion);
      } else {
        setSuggestError(res.data.message || 'Không thể lấy đề xuất từ AI.');
      }
    } catch (err) {
      console.error(err);
      setSuggestError('Không thể gợi ý danh mục lúc này. Vui lòng chọn danh mục thủ công.');
    } finally {
      setSuggestLoading(false);
    }
  };

  const applySuggestion = () => {
    if (!suggestion) return;
    const matched = categories.find(c => c.name.toLowerCase() === suggestion.predictedCategory.toLowerCase());
    if (matched) {
      setFormData(prev => ({
        ...prev,
        categoryId: matched.categoryId.toString()
      }));
    } else {
      setSuggestError(`Không tìm thấy danh mục '${suggestion.predictedCategory}' khớp trong cơ sở dữ liệu.`);
    }
  };
  
  const { token } = useContext(AuthContext);
  const navigate = useNavigate();

  useEffect(() => {
    // Fetch categories
    const fetchCategories = async () => {
      try {
        const res = await api.get('/categories');
        setCategories(res.data);
      } catch (err) {
        console.error("Error fetching categories", err);
      }
    };
    fetchCategories();
  }, []);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleFileChange = (e) => {
    setThumbnailFile(e.target.files[0]);
  };

  const nextStep = () => setStep(s => s + 1);
  const prevStep = () => setStep(s => s - 1);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      let finalThumbnailUrl = formData.thumbnailUrl;

      // Upload thumbnail first if selected
      if (thumbnailFile) {
        const fileData = new FormData();
        fileData.append('file', thumbnailFile);
        
        const uploadRes = await api.post('/media/upload', fileData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        });
        finalThumbnailUrl = uploadRes.data.url;
      }

      // Create course (Draft)
      const payload = {
        ...formData,
        categoryId: formData.categoryId ? parseInt(formData.categoryId) : null,
        price: parseFloat(formData.price),
        thumbnailUrl: finalThumbnailUrl
      };

      const res = await api.post('/courses', payload);

      // Redirect to dashboard or course edit page
      navigate('/instructor');
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || 'Có lỗi xảy ra khi tạo khoá học.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="course-create-container container">
      <div className="form-wrapper">
        <h1>Tạo Khoá học mới</h1>
        
        <div className="steps-indicator">
          <div className={`step-dot ${step >= 1 ? 'active' : ''}`}>1</div>
          <div className="step-line"></div>
          <div className={`step-dot ${step >= 2 ? 'active' : ''}`}>2</div>
        </div>

        {error && <div className="error-alert">{error}</div>}

        <form onSubmit={step === 2 ? handleSubmit : (e) => { e.preventDefault(); nextStep(); }}>
          {step === 1 && (
            <div className="step-content">
              <h2>Thông tin cơ bản</h2>
              <div className="form-group">
                <label>Tiêu đề Khoá học</label>
                <input type="text" name="title" value={formData.title} onChange={handleInputChange} required placeholder="VD: Lập trình React từ con số 0" />
              </div>
              <div className="form-group">
                <label>Mô tả ngắn</label>
                <textarea name="description" value={formData.description} onChange={handleInputChange} rows="4" placeholder="Nội dung khoá học..."></textarea>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Trình độ</label>
                  <select name="level" value={formData.level} onChange={handleInputChange}>
                    <option value="Beginner">Người mới bắt đầu</option>
                    <option value="Intermediate">Trung cấp</option>
                    <option value="Advanced">Nâng cao</option>
                    <option value="All Levels">Mọi cấp độ</option>
                  </select>
                </div>
                <div className="form-group">
                  <label>Giá (VND)</label>
                  <input type="number" name="price" value={formData.price} onChange={handleInputChange} min="0" step="1000" required />
                </div>
              </div>
              
              <div className="form-actions">
                <button type="button" onClick={() => navigate('/instructor')} className="btn btn-secondary">Huỷ</button>
                <button type="submit" className="btn btn-primary">Tiếp tục</button>
              </div>
            </div>
          )}

          {step === 2 && (
            <div className="step-content">
              <h2>Media & Phân loại</h2>
              <div className="form-group">
                <label>Danh mục (Để trống để AI tự phân loại)</label>
                <select name="categoryId" value={formData.categoryId} onChange={handleInputChange}>
                  <option value="">-- Chọn danh mục --</option>
                  {categories.map(cat => (
                    <option key={cat.categoryId} value={cat.categoryId}>{cat.name}</option>
                  ))}
                </select>
                <div className="ai-suggestion-wrapper mt-2">
                  <button type="button" className="btn btn-sm btn-info" onClick={handleSuggestCategory} disabled={suggestLoading}>
                    {suggestLoading ? 'Đang phân tích...' : 'Gợi ý danh mục bằng AI'}
                  </button>
                  {suggestError && <p className="text-danger small mt-1" style={{ fontSize: '0.85rem' }}>{suggestError}</p>}
                  {suggestion && (
                    <div className="ai-suggestion-result mt-2 p-2 border rounded" style={{ backgroundColor: '#f0f8ff' }}>
                      <p className="mb-1" style={{ fontSize: '0.9rem' }}>
                        <strong>Đề xuất AI:</strong> {suggestion.predictedCategory}
                        {suggestion.confidenceAvailable && suggestion.confidence !== null ? ` (Độ tin cậy: ${(suggestion.confidence * 100).toFixed(0)}%)` : ' (Độ tin cậy: Chưa khả dụng)'}
                      </p>
                      <p className="text-muted mb-2" style={{ fontSize: '0.75rem' }}>
                        Model: {suggestion.modelType} ({suggestion.modelVersion})
                      </p>
                      <button type="button" className="btn btn-sm btn-success" onClick={applySuggestion}>
                        Áp dụng đề xuất
                      </button>
                    </div>
                  )}
                </div>
              </div>
              <div className="form-group">
                <label>Ảnh đại diện (Thumbnail)</label>
                <input type="file" accept="image/*" onChange={handleFileChange} />
                {thumbnailFile && <p className="file-name">Đã chọn: {thumbnailFile.name}</p>}
              </div>

              <div className="form-actions">
                <button type="button" onClick={prevStep} className="btn btn-secondary">Quay lại</button>
                <button type="submit" className="btn btn-primary" disabled={loading}>
                  {loading ? 'Đang xử lý...' : 'Tạo Khoá học (Bản nháp)'}
                </button>
              </div>
            </div>
          )}
        </form>
      </div>
    </div>
  );
}

export default CourseCreate;
