import React, { useState, useEffect, useContext } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';
import { PlayCircle, CheckCircle, ArrowLeft, HelpCircle } from 'lucide-react';
import QuizTaker from '../components/QuizTaker';
import QuizResults from '../components/QuizResults';
import './CoursePlayer.css';

const BACKEND_URL = (import.meta.env.VITE_API_URL || 'http://localhost:5150/api').replace('/api', '');

function CoursePlayer() {
  const { id } = useParams();
  const [course, setCourse] = useState(null);
  const [curriculum, setCurriculum] = useState([]);
  const [activeLesson, setActiveLesson] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [completedLessons, setCompletedLessons] = useState([]);
  const [progressPercent, setProgressPercent] = useState(0);
  const [certificateUrl, setCertificateUrl] = useState(null);
  const [quizResult, setQuizResult] = useState(null);
  
  const { user } = useContext(AuthContext);

  useEffect(() => {
    const fetchData = async () => {
      try {
        // We could verify enrollment here or just let the API return 403 if they don't have access.
        // For now, assume they have access to reach this page based on ProtectedRoute and UI flow.
        const courseRes = await api.get(`/courses/${id}`);
        setCourse(courseRes.data);

        const currRes = await api.get(`/courses/${id}/curriculum`);
        setCurriculum(currRes.data);

        fetchProgress();

        // Set first lesson as active
        if (currRes.data.length > 0 && currRes.data[0].lessons?.length > 0) {
          setActiveLesson(currRes.data[0].lessons[0]);
        }

      } catch (err) {
        setError('Failed to load course content. Make sure you are enrolled.');
      } finally {
        setLoading(false);
      }
    };
    
    fetchData();
  }, [id]);

  const fetchProgress = async () => {
    try {
      const res = await api.get(`/courses/${id}/progress`);
      setCompletedLessons(res.data.completedLessonIds || []);
      setProgressPercent(res.data.progressPercentage || 0);
      if (res.data.certificateUrl) {
        setCertificateUrl(res.data.certificateUrl);
      }
    } catch (err) {
      console.error('Failed to fetch progress');
    }
  };

  const handleMarkCompleted = async () => {
    if (!activeLesson) return;
    try {
      await api.post(`/courses/${id}/lessons/${activeLesson.lessonId}/complete`);
      fetchProgress();
    } catch (err) {
      console.error('Failed to mark lesson as completed');
    }
  };

  if (loading) return <div className="player-loading">Loading Player...</div>;
  if (error) return <div className="player-error">{error}</div>;

  return (
    <div className="course-player-container">
      <div className="player-header d-flex justify-content-between align-items-center">
        <div className="d-flex align-items-center gap-3">
          <Link to="/my-courses" className="back-link"><ArrowLeft size={18} /> Dashboard</Link>
          <h2 className="mb-0">{course?.title}</h2>
        </div>
        <div className="d-flex align-items-center gap-2">
          <div className="progress" style={{ width: '150px', height: '10px' }}>
            <div className="progress-bar bg-success" role="progressbar" style={{ width: `${progressPercent}%` }}></div>
          </div>
          <span className="text-light small">{progressPercent}%</span>
          {certificateUrl && (
            <Link to={`/certificates/${certificateUrl}`} target="_blank" className="btn btn-sm btn-warning ms-3 fw-bold">
              View Certificate
            </Link>
          )}
        </div>
      </div>

      <div className="player-layout">
        {/* Main Video Area */}
        <div className="player-main">
          {activeLesson?.isQuiz ? (
            quizResult ? (
              <QuizResults 
                result={quizResult} 
                quizTitle={activeLesson.title} 
                onRetry={() => setQuizResult(null)} 
              />
            ) : (
              <QuizTaker 
                quizId={activeLesson.quizId} 
                onComplete={(result) => setQuizResult(result)} 
              />
            )
          ) : activeLesson ? (
            <div className="video-wrapper">
              {activeLesson.videoUrl ? (
                <video 
                  controls 
                  src={`${BACKEND_URL}${activeLesson.videoUrl}`} 
                  className="main-video"
                  autoPlay
                >
                  Your browser does not support HTML5 video.
                </video>
              ) : (
                <div className="no-video-placeholder">
                  <PlayCircle size={64} color="#64748b" />
                  <p>Video not available</p>
                </div>
              )}
            </div>
          ) : (
            <div className="no-video-placeholder">No content found.</div>
          )}
          
          {!activeLesson?.isQuiz && activeLesson && (
            <div className="lesson-details d-flex justify-content-between align-items-center">
              <h3>{activeLesson?.title}</h3>
              <button 
                className={`btn btn-sm ${completedLessons.includes(activeLesson.lessonId) ? 'btn-success' : 'btn-outline-primary'}`}
                onClick={handleMarkCompleted}
                disabled={completedLessons.includes(activeLesson.lessonId)}
              >
                {completedLessons.includes(activeLesson.lessonId) ? 'Completed' : 'Mark as completed'}
              </button>
            </div>
          )}
        </div>

        {/* Sidebar Curriculum */}
        <div className="player-sidebar">
          <div className="sidebar-header">
            <h4>Course Content</h4>
          </div>
          <div className="curriculum-accordion">
            {curriculum.map((section, idx) => (
              <div className="player-section" key={section.sectionId}>
                <div className="section-header">
                  <strong>Section {idx + 1}: {section.title}</strong>
                </div>
                <div className="section-lessons">
                  {section.lessons?.map((lesson, lIdx) => (
                    <div 
                      key={lesson.lessonId}
                      className={`player-lesson-item ${activeLesson?.lessonId === lesson.lessonId ? 'active' : ''}`}
                      onClick={() => setActiveLesson(lesson)}
                    >
                      <div className="lesson-icon">
                        <CheckCircle size={14} color={completedLessons.includes(lesson.lessonId) ? "#22c55e" : "#94a3b8"} />
                      </div>
                      <div className="lesson-info">
                        <span className="lesson-title">{lIdx + 1}. {lesson.title}</span>
                        <span className="lesson-time">{lesson.duration ? `${Math.floor(lesson.duration/60)}:${lesson.duration%60}` : '10:00'}</span>
                      </div>
                    </div>
                  ))}
                  {section.quizzes?.map((quiz, qIdx) => (
                    <div 
                      key={`quiz-${quiz.quizId}`}
                      className={`player-lesson-item ${activeLesson?.quizId === quiz.quizId ? 'active' : ''}`}
                      onClick={() => {
                        setActiveLesson({ ...quiz, isQuiz: true });
                        setQuizResult(null);
                      }}
                    >
                      <div className="lesson-icon">
                        <HelpCircle size={14} color="#f59e0b" />
                      </div>
                      <div className="lesson-info">
                        <span className="lesson-title">Quiz: {quiz.title}</span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

export default CoursePlayer;
