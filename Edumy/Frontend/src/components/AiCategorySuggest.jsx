import React, { useState, useEffect, useRef } from 'react';
import api from '../api/axiosConfig';
import { Sparkles, Check, BookOpen, Tag, ArrowRight, ChevronDown, ChevronUp } from 'lucide-react';
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
    <div className="ai-suggest-box">
      <div className="ai-suggest-header">
        <div className="ai-suggest-title-group">
          <div className="ai-icon-pulse">
            <Sparkles size={16} />
          </div>
          <div>
            <span className="ai-suggest-label">Trợ lý AI Đề xuất</span>
            {loading ? (
              <span className="ai-loading-text">Đang phân tích tiêu đề khóa học...</span>
            ) : suggestion ? (
              <div className="ai-recommend-pill">
                💡 Danh mục phù hợp: <strong>{suggestion.categoryName}</strong>
                <span className="confidence-pill">
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
            className={`btn-apply-suggestion ${applied ? 'applied' : ''}`}
          >
            {applied ? (
              <>
                <Check size={14} /> Đã áp dụng danh mục
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
                <Tag size={12} /> Tags gợi ý:
              </span>
              <div className="tags-list">
                {suggestion.suggestedTags.map((tag, idx) => (
                  <span key={idx} className="ai-tag-pill">
                    #{tag}
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* Curriculum Outline Generator Option */}
          {suggestion.curriculumOutline?.length > 0 && (
            <div className="ai-curriculum-toggle-box">
              <div className="d-flex justify-content-between align-items-center">
                <button
                  type="button"
                  className="btn-toggle-outline"
                  onClick={() => setExpandedCurriculum(!expandedCurriculum)}
                >
                  <BookOpen size={14} />
                  <span>
                    Xem khung đề cương mẫu đề xuất ({suggestion.curriculumOutline.length} chương)
                  </span>
                  {expandedCurriculum ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
                </button>

                {onApplyCurriculum && (
                  <button
                    type="button"
                    onClick={handleApplyCurriculum}
                    className="btn-apply-outline"
                  >
                    {curriculumApplied ? '✓ Đã tạo đề cương mẫu' : '⚡ Áp dụng đề cương mẫu'}
                  </button>
                )}
              </div>

              {expandedCurriculum && (
                <div className="ai-curriculum-preview-list">
                  {suggestion.curriculumOutline.map((sec, sIdx) => (
                    <div key={sIdx} className="preview-section-item">
                      <div className="preview-section-title">{sec.title}</div>
                      <ul className="preview-lessons-list">
                        {sec.lessons?.map((les, lIdx) => (
                          <li key={lIdx}>
                            <span>{les.title}</span>
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
