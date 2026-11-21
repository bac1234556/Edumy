import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import {
  CheckCircle2,
  AlertCircle,
  Loader2,
  Upload,
  X
} from 'lucide-react';
import CurriculumDraftBuilder from '../components/course-create/CurriculumDraftBuilder';
import CoInstructorDraftList from '../components/course-create/CoInstructorDraftList';
import { buildLessonPayload } from '../utils/lessonPayload';
import './CourseCreate.css';

const DEFAULT_THUMBNAIL_URL =
  'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800&q=80';

function CourseCreate() {
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
  const [sections, setSections] = useState([]);
  const [coInstructorEmails, setCoInstructorEmails] = useState([]);

  const [fieldErrors, setFieldErrors] = useState({});
  const [submitError, setSubmitError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [progressText, setProgressText] = useState('');

  const fileInputRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const res = await api.get('/categories');
        setCategories(Array.isArray(res.data) ? res.data : []);
      } catch (err) {
        console.error('Error fetching categories:', err);
      }
    };
    fetchCategories();
  }, []);

  // Revoke object URL when preview changes or component unmounts
  useEffect(() => {
    return () => {
      if (thumbnailPreview && thumbnailPreview.startsWith('blob:')) {
        URL.revokeObjectURL(thumbnailPreview);
      }
    };
  }, [thumbnailPreview]);

  const clearFieldError = (key) => {
    setFieldErrors((prev) => {
      if (!prev[key]) return prev;
      const next = { ...prev };
      delete next[key];
      return next;
    });
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value
    }));
    clearFieldError(name);
  };

  const handleFileChange = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setThumbnailFile(file);
    setThumbnailPreview(URL.createObjectURL(file));
  };

  const handleClearThumbnail = () => {
    setThumbnailFile(null);
    setThumbnailPreview('');
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleSectionsChange = (nextSections) => {
    setSections(nextSections);
    setFieldErrors((prev) => {
      const next = { ...prev };
      Object.keys(next).forEach((k) => {
        if (k.startsWith('section-') || k.startsWith('lesson-')) {
          delete next[k];
        }
      });
      return next;
    });
  };

  const isDirty = () => {
    const numericPrice = Number(formData.price);
    return Boolean(
      formData.title.trim() ||
        formData.description.trim() ||
        formData.categoryId ||
        (numericPrice && numericPrice > 0) ||
        formData.level !== 'Beginner' ||
        thumbnailFile ||
        sections.length > 0 ||
        coInstructorEmails.length > 0
    );
  };

  const handleCancel = () => {
    if (submitting) return;
    if (isDirty()) {
      const confirmed = window.confirm(
        'Bạn đã nhập thông tin khóa học. Bạn có chắc chắn muốn hủy bỏ và quay lại không?'
      );
      if (!confirmed) return;
    }
    navigate('/instructor');
  };

  const validateForm = () => {
    const errors = {};
    const orderedKeys = [];

    if (!formData.title.trim()) {
      errors.title = 'Vui lòng nhập tiêu đề khóa học.';
      orderedKeys.push('title');
    }

    const parsedPrice = Number(formData.price);
    if (formData.price === '' || Number.isNaN(parsedPrice) || parsedPrice < 0) {
      errors.price = 'Giá khóa học phải lớn hơn hoặc bằng 0.';
      orderedKeys.push('price');
    }

    if (!formData.categoryId) {
      errors.categoryId = 'Vui lòng chọn danh mục khóa học.';
      orderedKeys.push('categoryId');
    }

    sections.forEach((sec) => {
      if (!sec.title.trim()) {
        const key = `section-${sec.clientId}`;
        errors[key] = 'Vui lòng nhập tên chương.';
        orderedKeys.push(key);
      }
      sec.lessons.forEach((les) => {
        if (!les.title.trim()) {
          const key = `lesson-${sec.clientId}-${les.clientId}`;
          errors[key] = 'Vui lòng nhập tên bài học.';
          orderedKeys.push(key);
        }
      });
    });

    setFieldErrors(errors);

    if (orderedKeys.length > 0) {
      const firstKey = orderedKeys[0];
      requestAnimationFrame(() => {
        const targetEl = document.querySelector(`[data-error-key="${firstKey}"]`);
        if (targetEl) {
          targetEl.scrollIntoView({ behavior: 'smooth', block: 'center' });
          const inputEl = targetEl.querySelector('input, select, textarea');
          if (inputEl) inputEl.focus({ preventScroll: true });
        }
      });
      return false;
    }

    return true;
  };

  const extractServerMessage = (err, fallback) => {
    const data = err?.response?.data;
    if (typeof data === 'string' && data.trim()) return data;
    if (data?.message) return data.message;
    if (data?.error) return data.error;
    return err?.message || fallback;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitError('');

    if (!validateForm()) return;

    setSubmitting(true);
    const warnings = [];

    try {
      let finalThumbnailUrl = formData.thumbnailUrl || DEFAULT_THUMBNAIL_URL;

      // 1. Upload thumbnail if selected; on error, fallback to default image and record warning
      if (thumbnailFile) {
        setProgressText('Đang tải ảnh đại diện...');
        const fileData = new FormData();
        fileData.append('file', thumbnailFile);

        try {
          const uploadRes = await api.post('/media/upload', fileData, {
            headers: { 'Content-Type': 'multipart/form-data' }
          });
          if (uploadRes.data?.url) {
            finalThumbnailUrl = uploadRes.data.url;
          } else {
            warnings.push(
              'Tải lên ảnh đại diện không trả về đường dẫn hợp lệ, hệ thống đã dùng ảnh mặc định.'
            );
          }
        } catch (uploadErr) {
          console.warn('File upload failed, using default thumbnail:', uploadErr);
          warnings.push(
            `Không thể tải lên ảnh đại diện (${extractServerMessage(uploadErr, 'lỗi upload')}), hệ thống đã dùng ảnh mặc định.`
          );
        }
      }

      // 2. Create Course (if this fails, stay on page and keep all form data)
      setProgressText('Đang tạo khóa học...');
      const coursePayload = {
        title: formData.title.trim(),
        description: formData.description.trim(),
        level: formData.level,
        price: Number(formData.price) || 0,
        categoryId: parseInt(formData.categoryId, 10),
        thumbnailUrl: finalThumbnailUrl
      };

      const courseRes = await api.post('/courses', coursePayload);
      const newCourseId = courseRes.data?.courseId || courseRes.data?.id;

      if (!newCourseId) {
        throw new Error('Máy chủ không trả về mã khóa học mới.');
      }

      // 3. Create sections & lessons sequentially (do NOT recreate course if any fails)
      for (let i = 0; i < sections.length; i += 1) {
        const sec = sections[i];
        setProgressText(`Đang tạo chương ${i + 1}/${sections.length}...`);

        let sectionId = null;
        try {
          const secRes = await api.post(`/courses/${newCourseId}/sections`, {
            title: sec.title.trim(),
            orderIndex: i + 1
          });
          sectionId = secRes.data?.sectionId || secRes.data?.id;
          if (!sectionId) {
            throw new Error('Không nhận được mã chương từ máy chủ.');
          }
        } catch (secErr) {
          const reason = extractServerMessage(secErr, 'lỗi máy chủ');
          warnings.push(
            `Chương ${i + 1} ("${sec.title.trim()}"): tạo thất bại (${reason})${
              sec.lessons.length > 0
                ? ` — ${sec.lessons.length} bài học thuộc chương này chưa được tạo.`
                : '.'
            }`
          );
          continue;
        }

        for (let j = 0; j < sec.lessons.length; j += 1) {
          const les = sec.lessons[j];
          setProgressText(
            `Đang tạo bài học ${j + 1}/${sec.lessons.length} (Chương ${i + 1}/${sections.length})...`
          );
          try {
            const lessonPayload = buildLessonPayload(les, j + 1);
            await api.post(`/sections/${sectionId}/lessons`, lessonPayload);
          } catch (lesErr) {
            const reason = extractServerMessage(lesErr, 'lỗi máy chủ');
            warnings.push(
              `Chương ${i + 1} → Bài ${j + 1} ("${les.title.trim()}"): tạo thất bại (${reason}).`
            );
          }
        }
      }

      // 4. Invite co-instructors sequentially
      for (let k = 0; k < coInstructorEmails.length; k += 1) {
        const email = coInstructorEmails[k];
        setProgressText(
          `Đang gửi lời mời đồng giảng viên ${k + 1}/${coInstructorEmails.length}...`
        );
        try {
          await api.post(`/v1/instructor/courses/${newCourseId}/co-instructors`, { email });
        } catch (coErr) {
          const reason = extractServerMessage(coErr, 'không thể gửi lời mời');
          warnings.push(`Đồng giảng viên "${email}": mời thất bại (${reason}).`);
        }
      }

      // 5. Navigate based on complete vs partial success
      if (warnings.length === 0) {
        navigate('/instructor', {
          state: {
            successMessage: `Đã tạo khóa học "${formData.title.trim()}" thành công!`
          }
        });
      } else {
        navigate(`/instructor/courses/${newCourseId}/curriculum`, {
          state: {
            creationWarnings: warnings,
            createdCourseTitle: formData.title.trim()
          }
        });
      }
    } catch (err) {
      console.error('Failed to create course:', err);
      setSubmitError(extractServerMessage(err, 'Có lỗi xảy ra khi tạo khóa học.'));
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } finally {
      setSubmitting(false);
      setProgressText('');
    }
  };

  return (
    <div className="course-create-container">
      <div className="course-create-page-shell">
        <div className="create-wizard-header">
          <h1>Tạo Khóa học Mới</h1>
          <p>
            Điền thông tin khóa học, tùy chọn thiết lập giáo trình và mời đồng giảng viên ngay
            trên một trang duy nhất.
          </p>
        </div>

        {submitError && (
          <div className="alert alert-danger d-flex align-items-center gap-2 mb-4 rounded-3 shadow-sm">
            <AlertCircle size={18} className="flex-shrink-0" />
            <span>{submitError}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} noValidate className="course-create-sections-stack">
          {/* Card 1: Basic Information */}
          <section className="course-create-card">
            <h3 className="form-section-title">1. Thông tin cơ bản</h3>

            <div className="studio-form-group" data-error-key="title">
              <label htmlFor="course-title" className="studio-form-label">
                Tiêu đề Khóa học <span className="text-danger">*</span>
              </label>
              <input
                id="course-title"
                type="text"
                name="title"
                className={`studio-input ${fieldErrors.title ? 'is-invalid-field' : ''}`}
                placeholder="VD: Lập trình Full-Stack Spring Boot & React chuyên sâu"
                value={formData.title}
                onChange={handleInputChange}
                disabled={submitting}
                autoFocus
              />
              {fieldErrors.title && (
                <span className="field-error-text">{fieldErrors.title}</span>
              )}
            </div>

            <div className="studio-form-group">
              <label htmlFor="course-description" className="studio-form-label">
                Mô tả tóm tắt nội dung
              </label>
              <textarea
                id="course-description"
                name="description"
                className="studio-textarea"
                rows="4"
                placeholder="Giới thiệu mục tiêu khóa học, các công nghệ sẽ giảng dạy và đối tượng học viên phù hợp..."
                value={formData.description}
                onChange={handleInputChange}
                disabled={submitting}
              />
            </div>

            <div className="studio-form-row">
              <div className="studio-form-group mb-0">
                <label htmlFor="course-level" className="studio-form-label">
                  Trình độ khóa học
                </label>
                <select
                  id="course-level"
                  name="level"
                  className="studio-select"
                  value={formData.level}
                  onChange={handleInputChange}
                  disabled={submitting}
                >
                  <option value="Beginner">Người mới bắt đầu (Beginner)</option>
                  <option value="Intermediate">Trung cấp (Intermediate)</option>
                  <option value="Advanced">Chuyên sâu / Nâng cao (Advanced)</option>
                  <option value="All Levels">Mọi cấp độ (All Levels)</option>
                </select>
              </div>

              <div className="studio-form-group mb-0" data-error-key="price">
                <label htmlFor="course-price" className="studio-form-label">
                  Giá bán (VND)
                </label>
                <input
                  id="course-price"
                  type="number"
                  name="price"
                  className={`studio-input ${fieldErrors.price ? 'is-invalid-field' : ''}`}
                  min="0"
                  step="10000"
                  placeholder="399000"
                  value={formData.price}
                  onChange={handleInputChange}
                  disabled={submitting}
                />
                {fieldErrors.price ? (
                  <span className="field-error-text">{fieldErrors.price}</span>
                ) : (
                  <small className="text-muted d-block mt-1">Nhập 0 nếu miễn phí.</small>
                )}
              </div>
            </div>
          </section>

          {/* Card 2: Category & Thumbnail */}
          <section className="course-create-card">
            <h3 className="form-section-title">2. Danh mục &amp; Ảnh đại diện</h3>

            <div className="studio-form-group" data-error-key="categoryId">
              <label htmlFor="course-category" className="studio-form-label">
                Danh mục Khóa học <span className="text-danger">*</span>
              </label>
              <select
                id="course-category"
                name="categoryId"
                className={`studio-select ${fieldErrors.categoryId ? 'is-invalid-field' : ''}`}
                value={formData.categoryId}
                onChange={handleInputChange}
                disabled={submitting}
              >
                <option value="">-- Chọn danh mục --</option>
                {categories.map((cat) => (
                  <option key={cat.categoryId} value={cat.categoryId}>
                    {cat.name}
                  </option>
                ))}
              </select>
              {fieldErrors.categoryId && (
                <span className="field-error-text">{fieldErrors.categoryId}</span>
              )}
            </div>

            <div className="studio-form-group mb-0">
              <label htmlFor="course-thumbnail" className="studio-form-label">
                Ảnh đại diện khóa học (Thumbnail 16:9)
              </label>
              <div className="thumbnail-upload-row">
                <input
                  ref={fileInputRef}
                  id="course-thumbnail"
                  type="file"
                  accept="image/*"
                  className="form-control"
                  onChange={handleFileChange}
                  disabled={submitting}
                />
                {thumbnailPreview && (
                  <button
                    type="button"
                    className="btn-icon-studio danger"
                    onClick={handleClearThumbnail}
                    disabled={submitting}
                    title="Xóa ảnh đã chọn"
                    aria-label="Xóa ảnh đã chọn"
                  >
                    <X size={16} />
                  </button>
                )}
              </div>
              <small className="text-muted d-flex align-items-center gap-1 mt-1">
                <Upload size={13} /> Tỷ lệ khuyên dùng 16:9. Nếu để trống, hệ thống sẽ dùng ảnh mặc định.
              </small>

              {thumbnailPreview && (
                <div className="thumbnail-preview-frame">
                  <img
                    src={thumbnailPreview}
                    alt="Xem trước ảnh đại diện"
                    className="thumbnail-preview-img"
                  />
                </div>
              )}
            </div>
          </section>

          {/* Card 3: Curriculum Draft (Optional) */}
          <section className="course-create-card">
            <CurriculumDraftBuilder
              sections={sections}
              onChange={handleSectionsChange}
              errors={fieldErrors}
              disabled={submitting}
            />
          </section>

          {/* Card 4: Co-Instructors Draft (Optional) */}
          <section className="course-create-card">
            <CoInstructorDraftList
              emails={coInstructorEmails}
              onChange={setCoInstructorEmails}
              disabled={submitting}
            />
          </section>

          {/* Bottom Actions Bar */}
          <div className="course-create-actions-card">
            <div className="submit-progress-note">
              {submitting && progressText ? (
                <span className="d-inline-flex align-items-center gap-2 text-primary fw-semibold">
                  <Loader2 size={16} className="spin-icon" /> {progressText}
                </span>
              ) : (
                <span className="text-muted small">
                  Khóa học mới sẽ được lưu ở trạng thái <strong>Bản nháp (Draft)</strong>.
                </span>
              )}
            </div>

            <div className="wizard-actions-buttons">
              <button
                type="button"
                onClick={handleCancel}
                disabled={submitting}
                className="btn-wizard-back"
              >
                Hủy bỏ
              </button>
              <button
                type="submit"
                disabled={submitting}
                className="btn-wizard-next"
              >
                {submitting ? (
                  <>
                    <Loader2 size={17} className="spin-icon" />
                    <span>{progressText || 'Đang xử lý...'}</span>
                  </>
                ) : (
                  <>
                    <span>Tạo khóa học</span>
                    <CheckCircle2 size={18} />
                  </>
                )}
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}

export default CourseCreate;
