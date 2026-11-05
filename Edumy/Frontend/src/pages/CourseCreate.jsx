import React, { useState, useContext, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';
import {
  Sparkles,
  ArrowRight,
  ArrowLeft,
  CheckCircle2,
  Upload,
  AlertCircle,
  BookOpen,
  DollarSign,
  Layers,
  HelpCircle
} from 'lucide-react';
import AiCategorySuggest from '../components/AiCategorySuggest';
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
  const [thumbnailPreview, setThumbnailPreview] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // AI Classification suggestion
  const [suggestLoading, setSuggestLoading] = useState(false);
  const [suggestion, setSuggestion] = useState(null);
  const [suggestError, setSuggestError] = useState('');

  const navigate = useNavigate();

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const res = await api.get('/categories');
        setCategories(res.data || []);
      } catch (err) {
        console.error('Error fetching categories:', err);
      }
    };
    fetchCategories();
  }, []);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value
    }));
  };

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setThumbnailFile(file);
      setThumbnailPreview(URL.createObjectURL(file));
    }
  };

  // Trigger AI Category classification
  const handleSuggestCategory = async () => {
    if (!formData.title.trim()) {
      setSuggestError('Vui lòng nhập Tiêu đề khóa học trước khi yêu cầu AI phân loại.');
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
      if (res.data?.success) {
        setSuggestion(res.data.suggestion);
      } else {
        setSuggestError(res.data?.message || 'Không thể lấy đề xuất từ AI lúc này.');
      }
    } catch (err) {
      setSuggestError('Dịch vụ AI đang bận. Bạn có thể chọn danh mục thủ công.');
    } finally {
      setSuggestLoading(false);
    }
  };

  // Apply suggested category
  const applySuggestion = () => {
    if (!suggestion) return;
    const matched = categories.find(
      (c) => c.name.toLowerCase() === suggestion.predictedCategory.toLowerCase()
    );
    if (matched) {
      setFormData((prev) => ({
        ...prev,
        categoryId: matched.categoryId.toString()
      }));
    } else {
      setSuggestError(`Không tìm thấy danh mục '${suggestion.predictedCategory}' khớp trong cơ sở dữ liệu.`);
    }
  };

  const nextStep = (e) => {
    e.preventDefault();
    if (!formData.title.trim()) {
      setError('Vui lòng nhập tiêu đề khóa học.');
      return;
    }
    setError('');
    setStep(2);
  };

  const prevStep = () => {
    setError('');
    setStep(1);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      let finalThumbnailUrl = formData.thumbnailUrl || 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800&q=80';

      // 1. Upload thumbnail file if selected
      if (thumbnailFile) {
        const fileData = new FormData();
        fileData.append('file', thumbnailFile);

        try {
          const uploadRes = await api.post('/media/upload', fileData, {
            headers: {
              'Content-Type': 'multipart/form-data'
            }
          });
          if (uploadRes.data?.url) {
            finalThumbnailUrl = uploadRes.data.url;
          }
        } catch (uploadErr) {
          console.warn('File upload failed, using default thumbnail:', uploadErr);
        }
      }

      // 2. Create Course (Draft)
      const payload = {
        title: formData.title.trim(),
        description: formData.description.trim(),
        level: formData.level,
        price: parseFloat(formData.price) || 0,
        categoryId: formData.categoryId ? parseInt(formData.categoryId, 10) : null,
        thumbnailUrl: finalThumbnailUrl
      };

      const res = await api.post('/courses', payload);
      const newCourseId = res.data?.courseId || res.data?.id;

      // 3. Navigate directly to Curriculum Builder so instructor can add lessons immediately!
      if (newCourseId) {
        navigate(`/instructor/courses/${newCourseId}/curriculum`);
      } else {
        navigate('/instructor');
      }
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || 'Có lỗi xảy ra khi tạo khóa học.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="course-create-container">
      <div className="course-create-wrapper">
        <div className="create-wizard-header">
          <h1>Tạo Khóa học Mới</h1>
          <p>Khởi tạo giáo trình của bạn theo tiêu chuẩn đào tạo chuyên nghiệp của Edumy.</p>
        </div>

        {/* Stepper Indicator */}
        <div className="studio-stepper-row">
          <div className="stepper-node">
            <div className={`stepper-circle ${step >= 1 ? 'active' : ''}`}>1</div>
            <span className={`stepper-label ${step >= 1 ? 'active' : ''}`}>Thông tin cơ bản</span>
          </div>

          <div className={`stepper-divider-line ${step >= 2 ? 'active' : ''}`}></div>

          <div className="stepper-node">
            <div className={`stepper-circle ${step >= 2 ? 'active' : ''}`}>2</div>
            <span className={`stepper-label ${step >= 2 ? 'active' : ''}`}>Media & Danh mục</span>
          </div>
        </div>

        {error && (
          <div className="alert alert-danger d-flex align-items-center gap-2 mb-4 rounded-3">
            <AlertCircle size={18} /> {error}
          </div>
        )}

        <form onSubmit={step === 1 ? nextStep : handleSubmit}>
          {/* ----------------------------------------------------------------
              STEP 1: BASIC INFORMATION
              ---------------------------------------------------------------- */}
          {step === 1 && (
            <div>
              <h3 className="form-section-title">Bước 1: Thông tin cơ bản</h3>

              <div className="studio-form-group">
                <label className="studio-form-label">
                  Tiêu đề Khóa học <span className="text-danger">*</span>
                </label>
                <input
                  type="text"
                  name="title"
                  className="studio-input"
                  placeholder="VD: Lập trình Full-Stack Spring Boot & React chuyên sâu"
                  value={formData.title}
                  onChange={handleInputChange}
                  required
                  autoFocus
                />
                {/* Feature 1: AI Category & Curriculum Suggestion Chip */}
                <AiCategorySuggest
                  title={formData.title}
                  description={formData.description}
                  onApplyCategory={(catId) => {
                    setFormData((prev) => ({ ...prev, categoryId: catId.toString() }));
                  }}
                  onApplyTags={(tags) => {
                    console.log('AI suggested tags applied:', tags);
                  }}
                  onApplyCurriculum={(outline) => {
                    alert(`Đã áp dụng khung đề cương ${outline.length} chương do AI gợi ý vào khóa học.`);
                  }}
                />
              </div>

              <div className="studio-form-group">
                <label className="studio-form-label">Mô tả tóm tắt nội dung</label>
                <textarea
                  name="description"
                  className="studio-textarea"
                  rows="4"
                  placeholder="Giới thiệu mục tiêu khóa học, các công nghệ sẽ giảng dạy và đối tượng học viên phù hợp..."
                  value={formData.description}
                  onChange={handleInputChange}
                ></textarea>
              </div>

              <div className="studio-form-row">
                <div className="studio-form-group">
                  <label className="studio-form-label">Trình độ khóa học</label>
                  <select
                    name="level"
                    className="studio-select"
                    value={formData.level}
                    onChange={handleInputChange}
                  >
                    <option value="Beginner">Người mới bắt đầu (Beginner)</option>
                    <option value="Intermediate">Trung cấp (Intermediate)</option>
                    <option value="Advanced">Chuyên sâu / Nâng cao (Advanced)</option>
                    <option value="All Levels">Mọi cấp độ (All Levels)</option>
                  </select>
                </div>

                <div className="studio-form-group">
                  <label className="studio-form-label">Giá bán (VND)</label>
                  <input
                    type="number"
                    name="price"
                    className="studio-input"
                    min="0"
                    step="10000"
                    placeholder="399000"
                    value={formData.price}
                    onChange={handleInputChange}
                    required
                  />
                  <small className="text-muted">Nhập 0 nếu là khóa học miễn phí.</small>
                </div>
              </div>

              <div className="wizard-actions-bar">
                <button
                  type="button"
                  onClick={() => navigate('/instructor')}
                  className="btn-wizard-back"
                >
                  Hủy bỏ
                </button>
                <button type="submit" className="btn-wizard-next">
                  Tiếp tục Bước 2 <ArrowRight size={18} />
                </button>
              </div>
            </div>
          )}

          {/* ----------------------------------------------------------------
              STEP 2: MEDIA & AI CLASSIFICATION
              ---------------------------------------------------------------- */}
          {step === 2 && (
            <div>
              <h3 className="form-section-title">Bước 2: Media & Phân loại Danh mục</h3>

              <div className="studio-form-group">
                <label className="studio-form-label">Danh mục Khóa học</label>
                <select
                  name="categoryId"
                  className="studio-select"
                  value={formData.categoryId}
                  onChange={handleInputChange}
                >
                  <option value="">-- Chọn danh mục thủ công hoặc dùng AI gợi ý --</option>
                  {categories.map((cat) => (
                    <option key={cat.categoryId} value={cat.categoryId}>
                      {cat.name}
                    </option>
                  ))}
                </select>

                {/* AI Assistant Suggester Box */}
                <div className="ai-suggester-panel">
                  <div className="ai-suggester-header">
                    <span className="fw-bold small text-dark d-flex align-items-center gap-1">
                      <Sparkles size={16} className="text-primary" /> Trợ lý AI gợi ý Danh mục:
                    </span>
                    <button
                      type="button"
                      onClick={handleSuggestCategory}
                      disabled={suggestLoading}
                      className="btn-trigger-ai"
                    >
                      {suggestLoading ? 'AI đang phân tích...' : 'Phân tích tự động'}
                    </button>
                  </div>

                  {suggestError && (
                    <p className="text-danger small mb-0 mt-1">{suggestError}</p>
                  )}

                  {suggestion && (
                    <div className="ai-recommendation-result">
                      <div className="d-flex justify-content-between align-items-center mb-1">
                        <div>
                          <strong>Gợi ý:</strong> {suggestion.predictedCategory}
                          {suggestion.confidenceAvailable && suggestion.confidence !== null && (
                            <span className="badge bg-success bg-opacity-10 text-success ms-2">
                              Độ tin cậy {(suggestion.confidence * 100).toFixed(0)}%
                            </span>
                          )}
                        </div>
                        <button
                          type="button"
                          onClick={applySuggestion}
                          className="btn-apply-suggestion"
                        >
                          Áp dụng ngay
                        </button>
                      </div>
                      <small className="text-muted d-block">
                        Mô hình: {suggestion.modelType} ({suggestion.modelVersion})
                      </small>
                    </div>
                  )}
                </div>
              </div>

              {/* Thumbnail upload */}
              <div className="studio-form-group">
                <label className="studio-form-label">Ảnh đại diện khóa học (Thumbnail 16:9)</label>
                <input
                  type="file"
                  accept="image/*"
                  className="form-control"
                  onChange={handleFileChange}
                />

                {thumbnailPreview && (
                  <div className="thumbnail-preview-frame">
                    <img
                      src={thumbnailPreview}
                      alt="Thumbnail Preview"
                      className="thumbnail-preview-img"
                    />
                  </div>
                )}
              </div>

              <div className="wizard-actions-bar">
                <button
                  type="button"
                  onClick={prevStep}
                  disabled={loading}
                  className="btn-wizard-back"
                >
                  <ArrowLeft size={16} className="me-1" /> Quay lại
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="btn-wizard-next"
                >
                  {loading ? 'Đang tạo khóa học...' : 'Hoàn tất & Soạn bài giảng'}
                  <CheckCircle2 size={18} />
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
