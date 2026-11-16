import api from '../axiosConfig';

export const qaApi = {
  /**
   * UC-S18: Lấy danh sách câu hỏi trong khóa học
   * params: { lessonId?, mineOnly?, unansweredOnly?, q?, page?, size? }
   */
  getCourseQuestions: (courseId, params = {}) => 
    api.get(`/v1/courses/${courseId}/questions`, { params }),

  /**
   * UC-S18: Xem chi tiết luồng câu hỏi và tất cả câu trả lời
   */
  getThread: (questionId) => 
    api.get(`/v1/questions/${questionId}`),

  /**
   * UC-S17: Học viên gửi câu hỏi mới
   */
  askQuestion: (courseId, payload) => 
    api.post(`/v1/courses/${courseId}/questions`, payload),

  /**
   * UC-S19: Học viên sửa câu hỏi của mình
   */
  updateQuestion: (questionId, payload) =>
    api.put(`/v1/questions/${questionId}`, payload),

  /**
   * UC-S19: Học viên xóa câu hỏi của mình
   */
  deleteQuestion: (questionId) =>
    api.delete(`/v1/questions/${questionId}`),

  /**
   * UC-I20: Phản hồi câu hỏi (giảng viên hoặc học viên)
   */
  postAnswer: (questionId, payload) => 
    api.post(`/v1/questions/${questionId}/answers`, payload),

  /**
   * UC-I21 / UC-S19: Sửa câu trả lời của mình
   */
  updateAnswer: (answerId, payload) =>
    api.put(`/v1/answers/${answerId}`, payload),

  /**
   * UC-I21 / UC-S19: Xóa câu trả lời của mình
   */
  deleteAnswer: (answerId) =>
    api.delete(`/v1/answers/${answerId}`),

  /**
   * UC-S19: Đánh dấu đã giải quyết / mở lại câu hỏi
   */
  toggleResolve: (questionId) => 
    api.put(`/v1/questions/${questionId}/resolve`),

  /**
   * UC-I22: Hàng đợi câu hỏi dành cho Giảng viên (ưu tiên cũ nhất chưa trả lời)
   * params: { status: 'unanswered' | 'all', courseId?: number, page?: number, size?: number }
   */
  getInstructorQuestions: (params = {}) => 
    api.get('/v1/instructor/questions', { params }),

  /**
   * UC-A19: Quản trị viên kiểm duyệt Hỏi & Đáp (Q&A Moderation)
   */
  getAdminQuestions: () =>
    api.get('/v1/admin/questions'),

  hideQuestion: (questionId) =>
    api.post(`/v1/admin/questions/${questionId}/hide`),

  restoreQuestion: (questionId) =>
    api.post(`/v1/admin/questions/${questionId}/restore`),

  hideAnswer: (answerId) =>
    api.post(`/v1/admin/answers/${answerId}/hide`),

  restoreAnswer: (answerId) =>
    api.post(`/v1/admin/answers/${answerId}/restore`),
};

export default qaApi;
