import React, { useState, useEffect, useRef } from 'react';
import api from '../api/axiosConfig';
import { Sparkles, Check, BookOpen, Tag, ChevronDown, ChevronUp, Lightbulb, Zap } from 'lucide-react';
import './AiCategorySuggest.css';

const AiCategorySuggest = ({
  title,
  description,
  onApplyCategory,
  onApplyTags,
  onApplyCurriculum
}) => {
  const [loading, setLoading] = useState(false);
  const [suggestion, setSuggestion] = useState(null);
  const [expandedCurriculum, setExpandedCurriculum] = useState(false);
  const [applied, setApplied] = useState(false);
  const [curriculumApplied, setCurriculumApplied] = useState(false);
  const debounceTimer = useRef(null);

  useEffect(() => {
    if (!title || title.trim().length < 4) {
      setSuggestion(null);
      setApplied(false);
      return;
    }

    if (debounceTimer.current) {
      clearTimeout(debounceTimer.current);
    }

    debounceTimer.current = setTimeout(async () => {
      try {
        setLoading(true);
        const res = await api.post('/v1/ai/suggest-category', {
          title: title.trim(),
          description: description || ''
        });

        if (res.data && res.data.categoryName) {
          setSuggestion(res.data);
          setApplied(false);
          setCurriculumApplied(false);
        }
      } catch (err) {
        // Fallback silently if AI is busy
        console.warn('AI suggestion fallback active:', err);
      } finally {
        setLoading(false);
      }
    }, 500);

    return () => {
      if (debounceTimer.current) clearTimeout(debounceTimer.current);
    };
  }, [title, description]);

  if (!suggestion && !loading) return null;

  const handleApply = () => {
    if (suggestion) {
      if (onApplyCategory && suggestion.suggestedCategoryId) {
        onApplyCategory(suggestion.suggestedCategoryId, suggestion.categoryName);
      }
      if (onApplyTags && suggestion.suggestedTags?.length > 0) {
        onApplyTags(suggestion.suggestedTags);
      }
      setApplied(true);
    }
  };

  const handleApplyCurriculum = () => {
    if (suggestion?.curriculumOutline && onApplyCurriculum) {
      onApplyCurriculum(suggestion.curriculumOutline);
      setCurriculumApplied(true);
    }
  };

  return (
    <div className="ai-suggest-box rounded-xl shadow-sm">
      <div className="ai-suggest-header">
        <div className="ai-suggest-title-group">
          <div className="ai-icon-pulse">
            <Sparkles size={16} strokeWidth={1.75} aria-hidden="true" />
          </div>
          <div>
            <span className="ai-suggest-label">Trợ lý AI Đề xuất</span>
            {loading ? (
              <span className="ai-loading-text">Đang phân tích tiêu đề khóa học...</span>
            ) : suggestion ? (
              <div className="ai-recommend-pill">
                <Lightbulb size={15} strokeWidth={1.75} className="text-amber-500" style={{ color: '#d97706' }} aria-hidden="true" />
                <span>Danh mục phù hợp: <strong className="text-slate-900">{suggestion.categoryName}</strong></span>
                <span className="confidence-pill pill-badge">
                  {Math.round((suggestion.confidenceScore || 0.88) * 100)}% khớp
                </span>
              </div>
            ) : null}
          </div>
        </div>

        {suggestion && !loading && (
          <button
            type="button"
            onClick={handleApply}
            className={`btn-apply-suggestion cursor-pointer transition-all duration-200 active:scale-95 ${applied ? 'applied' : ''}`}
          >
            {applied ? (
              <>
                <Check size={14} strokeWidth={2} aria-hidden="true" /> Đã áp dụng danh mục
              </>
            ) : (
              'Áp dụng danh mục'
            )}
          </button>
        )}
      </div>

      {suggestion && !loading && (
        <div className="ai-suggest-content">
          {/* Suggested Tags */}
          {suggestion.suggestedTags?.length > 0 && (
            <div className="ai-tags-row">
              <span className="tags-label">
                <Tag size={12} strokeWidth={1.75} aria-hidden="true" /> Tags gợi ý:
              </span>
              <div className="tags-list">
                {suggestion.suggestedTags.map((tag, idx) => (
                  <span key={idx} className="ai-tag-pill pill-badge">
                    #{tag}
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* Curriculum Outline Generator Option */}
          {suggestion.curriculumOutline?.length > 0 && (
            <div className="ai-curriculum-toggle-box rounded-xl">
              <div className="d-flex justify-content-between align-items-center flex-wrap gap-2">
                <button
                  type="button"
                  className="btn-toggle-outline cursor-pointer transition-all duration-200"
                  onClick={() => setExpandedCurriculum(!expandedCurriculum)}
                >
                  <BookOpen size={14} strokeWidth={1.75} aria-hidden="true" />
                  <span>
                    Xem khung đề cương mẫu đề xuất ({suggestion.curriculumOutline.length} chương)
                  </span>
                  {expandedCurriculum ? <ChevronUp size={14} strokeWidth={1.75} /> : <ChevronDown size={14} strokeWidth={1.75} />}
                </button>

                {onApplyCurriculum && (
                  <button
                    type="button"
                    onClick={handleApplyCurriculum}
                    className="btn-apply-outline cursor-pointer transition-all duration-200 active:scale-95"
                  >
                    {curriculumApplied ? (
                      <>
                        <Check size={13} strokeWidth={2} aria-hidden="true" /> Đã tạo đề cương mẫu
                      </>
                    ) : (
                      <>
                        <Zap size={13} strokeWidth={1.75} aria-hidden="true" /> Áp dụng đề cương mẫu
                      </>
                    )}
                  </button>
                )}
              </div>

              {expandedCurriculum && (
                <div className="ai-curriculum-preview-list">
                  {suggestion.curriculumOutline.map((sec, sIdx) => (
                    <div key={sIdx} className="preview-section-item">
                      <div className="preview-section-title line-clamp-1">{sec.title}</div>
                      <ul className="preview-lessons-list">
                        {sec.lessons?.map((les, lIdx) => (
                          <li key={lIdx}>
                            <span className="line-clamp-1">{les.title}</span>
                            <span className="preview-duration">{les.durationMinutes} phút</span>
                          </li>
                        ))}
                      </ul>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default AiCategorySuggest;
