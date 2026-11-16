import api from '../axiosConfig';

export const qaApi = {
  /**
   * Lấy danh sách câu hỏi trong khóa học (hỗ trợ lọc theo bài học lessonId)
   */
  getCourseQuestions: (courseId, params = {}) => 
    api.get(`/v1/courses/${courseId}/questions`, { params }),

  /**
   * Xem chi tiết luồng câu hỏi và tất cả câu trả lời
   */
  getThread: (questionId) => 
    api.get(`/v1/questions/${questionId}`),

  /**
   * Học viên gửi câu hỏi mới
   */
  askQuestion: (courseId, payload) => 
    api.post(`/v1/courses/${courseId}/questions`, payload),

  /**
   * Phản hồi câu hỏi (giảng viên hoặc học viên)
   */
  postAnswer: (questionId, payload) => 
    api.post(`/v1/questions/${questionId}/answers`, payload),

  /**
   * Đánh dấu đã giải quyết / mở lại câu hỏi
   */
  toggleResolve: (questionId) => 
    api.put(`/v1/questions/${questionId}/resolve`),

  /**
   * UC-I22: Hàng đợi câu hỏi dành cho Giảng viên (ưu tiên cũ nhất chưa trả lời)
   * params: { status: 'unanswered' | 'all', courseId?: number, page?: number, size?: number }
   */
  getInstructorQuestions: (params = {}) => 
    api.get('/v1/instructor/questions', { params }),
};

export default qaApi;
