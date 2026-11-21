import React from 'react';
import {
  Plus,
  Trash2,
  ArrowUp,
  ArrowDown,
  Layers,
  Video,
  FileText,
  BookOpen
} from 'lucide-react';

const createClientId = () =>
  `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 8)}`;

function createEmptyLesson() {
  return {
    clientId: createClientId(),
    title: '',
    contentType: 'VIDEO',
    videoUrl: '',
    content: '',
    durationMinutes: 10,
    isPreview: false
  };
}

function createEmptySection() {
  return {
    clientId: createClientId(),
    title: '',
    lessons: []
  };
}

function CurriculumDraftBuilder({ sections, onChange, errors = {}, disabled = false }) {
  const handleAddSection = () => {
    onChange([...sections, createEmptySection()]);
  };

  const handleUpdateSectionTitle = (sectionIndex, value) => {
    const next = sections.map((sec, idx) =>
      idx === sectionIndex ? { ...sec, title: value } : sec
    );
    onChange(next);
  };

  const handleDeleteSection = (sectionIndex) => {
    onChange(sections.filter((_, idx) => idx !== sectionIndex));
  };

  const handleMoveSection = (sectionIndex, direction) => {
    const targetIndex = sectionIndex + direction;
    if (targetIndex < 0 || targetIndex >= sections.length) return;
    const next = [...sections];
    const [moved] = next.splice(sectionIndex, 1);
    next.splice(targetIndex, 0, moved);
    onChange(next);
  };

  const handleAddLesson = (sectionIndex) => {
    const next = sections.map((sec, idx) =>
      idx === sectionIndex
        ? { ...sec, lessons: [...sec.lessons, createEmptyLesson()] }
        : sec
    );
    onChange(next);
  };

  const handleUpdateLesson = (sectionIndex, lessonIndex, field, value) => {
    const next = sections.map((sec, sIdx) => {
      if (sIdx !== sectionIndex) return sec;
      const updatedLessons = sec.lessons.map((les, lIdx) =>
        lIdx === lessonIndex ? { ...les, [field]: value } : les
      );
      return { ...sec, lessons: updatedLessons };
    });
    onChange(next);
  };

  const handleDeleteLesson = (sectionIndex, lessonIndex) => {
    const next = sections.map((sec, sIdx) => {
      if (sIdx !== sectionIndex) return sec;
      return {
        ...sec,
        lessons: sec.lessons.filter((_, lIdx) => lIdx !== lessonIndex)
      };
    });
    onChange(next);
  };

  const handleMoveLesson = (sectionIndex, lessonIndex, direction) => {
    const section = sections[sectionIndex];
    const targetIndex = lessonIndex + direction;
    if (!section || targetIndex < 0 || targetIndex >= section.lessons.length) return;

    const next = sections.map((sec, sIdx) => {
      if (sIdx !== sectionIndex) return sec;
      const reordered = [...sec.lessons];
      const [moved] = reordered.splice(lessonIndex, 1);
      reordered.splice(targetIndex, 0, moved);
      return { ...sec, lessons: reordered };
    });
    onChange(next);
  };

  return (
    <div className="curriculum-draft-builder">
      <div className="section-card-header-row">
        <div>
          <h3 className="form-section-title mb-1 border-0 pb-0">
            3. Giáo trình (Chương &amp; Bài học)
          </h3>
          <p className="section-card-subtitle">
            Tùy chọn — bạn có thể soạn sẵn các chương và bài học ngay tại đây hoặc bổ sung sau.
          </p>
        </div>
        <button
          type="button"
          className="btn-outline-studio"
          onClick={handleAddSection}
          disabled={disabled}
        >
          <Plus size={16} /> Thêm chương
        </button>
      </div>

      {sections.length === 0 ? (
        <div className="draft-empty-state">
          <Layers size={28} className="draft-empty-icon" />
          <p className="mb-2">Chưa có chương nào - bạn có thể thêm ngay hoặc bổ sung sau.</p>
          <button
            type="button"
            className="btn-outline-studio"
            onClick={handleAddSection}
            disabled={disabled}
          >
            <Plus size={15} /> Thêm chương đầu tiên
          </button>
        </div>
      ) : (
        <div className="draft-sections-list">
          {sections.map((section, secIdx) => {
            const sectionErrorKey = `section-${section.clientId}`;
            const sectionError = errors[sectionErrorKey];

            return (
              <div key={section.clientId} className="draft-section-box">
                <div className="draft-section-header" data-error-key={sectionErrorKey}>
                  <span className="draft-section-badge">Chương {secIdx + 1}</span>
                  <div className="draft-section-title-wrap">
                    <input
                      type="text"
                      className={`studio-input studio-input-sm ${sectionError ? 'is-invalid-field' : ''}`}
                      placeholder="VD: Giới thiệu & Cài đặt môi trường..."
                      value={section.title}
                      onChange={(e) => handleUpdateSectionTitle(secIdx, e.target.value)}
                      disabled={disabled}
                    />
                    {sectionError && <span className="field-error-text">{sectionError}</span>}
                  </div>
                  <div className="draft-item-controls">
                    <button
                      type="button"
                      className="btn-icon-studio"
                      onClick={() => handleMoveSection(secIdx, -1)}
                      disabled={disabled || secIdx === 0}
                      title="Di chuyển chương lên"
                      aria-label="Di chuyển chương lên"
                    >
                      <ArrowUp size={15} />
                    </button>
                    <button
                      type="button"
                      className="btn-icon-studio"
                      onClick={() => handleMoveSection(secIdx, 1)}
                      disabled={disabled || secIdx === sections.length - 1}
                      title="Di chuyển chương xuống"
                      aria-label="Di chuyển chương xuống"
                    >
                      <ArrowDown size={15} />
                    </button>
                    <button
                      type="button"
                      className="btn-icon-studio danger"
                      onClick={() => handleDeleteSection(secIdx)}
                      disabled={disabled}
                      title="Xóa chương"
                      aria-label="Xóa chương"
                    >
                      <Trash2 size={15} />
                    </button>
                  </div>
                </div>

                <div className="draft-lessons-container">
                  {section.lessons.length === 0 ? (
                    <div className="draft-lessons-empty">
                      Chưa có bài học nào trong chương này.
                    </div>
                  ) : (
                    <div className="draft-lessons-list">
                      {section.lessons.map((lesson, lesIdx) => {
                        const lessonErrorKey = `lesson-${section.clientId}-${lesson.clientId}`;
                        const lessonError = errors[lessonErrorKey];

                        return (
                          <div key={lesson.clientId} className="draft-lesson-card">
                            <div className="draft-lesson-top" data-error-key={lessonErrorKey}>
                              <span className="draft-lesson-index">Bài {lesIdx + 1}</span>
                              <div className="draft-lesson-title-wrap">
                                <input
                                  type="text"
                                  className={`studio-input studio-input-sm ${lessonError ? 'is-invalid-field' : ''}`}
                                  placeholder="VD: Cài đặt JDK và Spring Initializr..."
                                  value={lesson.title}
                                  onChange={(e) =>
                                    handleUpdateLesson(secIdx, lesIdx, 'title', e.target.value)
                                  }
                                  disabled={disabled}
                                />
                                {lessonError && (
                                  <span className="field-error-text">{lessonError}</span>
                                )}
                              </div>
                              <div className="draft-item-controls">
                                <button
                                  type="button"
                                  className="btn-icon-studio"
                                  onClick={() => handleMoveLesson(secIdx, lesIdx, -1)}
                                  disabled={disabled || lesIdx === 0}
                                  title="Di chuyển bài học lên"
                                  aria-label="Di chuyển bài học lên"
                                >
                                  <ArrowUp size={14} />
                                </button>
                                <button
                                  type="button"
                                  className="btn-icon-studio"
                                  onClick={() => handleMoveLesson(secIdx, lesIdx, 1)}
                                  disabled={disabled || lesIdx === section.lessons.length - 1}
                                  title="Di chuyển bài học xuống"
                                  aria-label="Di chuyển bài học xuống"
                                >
                                  <ArrowDown size={14} />
                                </button>
                                <button
                                  type="button"
                                  className="btn-icon-studio danger"
                                  onClick={() => handleDeleteLesson(secIdx, lesIdx)}
                                  disabled={disabled}
                                  title="Xóa bài học"
                                  aria-label="Xóa bài học"
                                >
                                  <Trash2 size={14} />
                                </button>
                              </div>
                            </div>

                            <div className="draft-lesson-grid">
                              <div>
                                <label className="draft-mini-label">Loại bài học</label>
                                <select
                                  className="studio-select studio-input-sm"
                                  value={lesson.contentType}
                                  onChange={(e) =>
                                    handleUpdateLesson(secIdx, lesIdx, 'contentType', e.target.value)
                                  }
                                  disabled={disabled}
                                >
                                  <option value="VIDEO">Video bài giảng</option>
                                  <option value="PDF">Tài liệu PDF</option>
                                  <option value="TEXT">Bài đọc (Text)</option>
                                </select>
                              </div>

                              <div>
                                <label className="draft-mini-label">Thời lượng (phút)</label>
                                <input
                                  type="number"
                                  min="1"
                                  className="studio-input studio-input-sm"
                                  value={lesson.durationMinutes}
                                  onChange={(e) =>
                                    handleUpdateLesson(
                                      secIdx,
                                      lesIdx,
                                      'durationMinutes',
                                      e.target.value
                                    )
                                  }
                                  disabled={disabled}
                                />
                              </div>

                              <div className="draft-preview-toggle">
                                <label className="draft-checkbox-label">
                                  <input
                                    type="checkbox"
                                    checked={Boolean(lesson.isPreview)}
                                    onChange={(e) =>
                                      handleUpdateLesson(
                                        secIdx,
                                        lesIdx,
                                        'isPreview',
                                        e.target.checked
                                      )
                                    }
                                    disabled={disabled}
                                  />
                                  <span>Cho học thử (Preview)</span>
                                </label>
                              </div>
                            </div>

                            {(lesson.contentType === 'VIDEO' || lesson.contentType === 'PDF') && (
                              <div className="mt-2">
                                <label className="draft-mini-label d-flex align-items-center gap-1">
                                  {lesson.contentType === 'VIDEO' ? (
                                    <>
                                      <Video size={13} /> URL Video (MP4 / YouTube)
                                    </>
                                  ) : (
                                    <>
                                      <FileText size={13} /> URL Tài liệu PDF
                                    </>
                                  )}
                                </label>
                                <input
                                  type="url"
                                  className="studio-input studio-input-sm"
                                  placeholder={
                                    lesson.contentType === 'VIDEO'
                                      ? 'https://... (để trống để dùng video mẫu)'
                                      : 'https://...file.pdf (để trống để dùng PDF mẫu)'
                                  }
                                  value={lesson.videoUrl}
                                  onChange={(e) =>
                                    handleUpdateLesson(secIdx, lesIdx, 'videoUrl', e.target.value)
                                  }
                                  disabled={disabled}
                                />
                              </div>
                            )}

                            <div className="mt-2">
                              <label className="draft-mini-label d-flex align-items-center gap-1">
                                <BookOpen size={13} />
                                {lesson.contentType === 'TEXT'
                                  ? 'Nội dung bài đọc'
                                  : 'Mô tả / Ghi chú bài học'}
                              </label>
                              <textarea
                                rows="2"
                                className="studio-textarea studio-input-sm"
                                placeholder={
                                  lesson.contentType === 'TEXT'
                                    ? 'Nhập nội dung chi tiết cho bài đọc...'
                                    : 'Tóm tắt nội dung hoặc ghi chú thêm cho học viên...'
                                }
                                value={lesson.content}
                                onChange={(e) =>
                                  handleUpdateLesson(secIdx, lesIdx, 'content', e.target.value)
                                }
                                disabled={disabled}
                              />
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  )}

                  <div className="draft-add-lesson-row">
                    <button
                      type="button"
                      className="btn-add-lesson-inline"
                      onClick={() => handleAddLesson(secIdx)}
                      disabled={disabled}
                    >
                      <Plus size={14} /> Thêm bài học vào Chương {secIdx + 1}
                    </button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}

export default CurriculumDraftBuilder;
