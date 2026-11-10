import api from '../axiosConfig';

export const courseApi = {
  getCourses: (params = {}) => api.get('/courses', { params }),
  getCourseById: (id) => api.get(`/courses/${id}`),
  getCategories: () => api.get('/categories'),
  instantSearch: (keyword) => api.get('/courses/instant-search', { params: { q: keyword } }),
  getRecommendations: () => api.get('/courses/recommend'),
  getEnrolledCourses: () => api.get('/courses/enrolled'),
  getEnrolledIds: () => api.get('/courses/enrolled-ids'),
  getEnrollmentStatus: (id) => api.get(`/courses/${id}/enrollment-status`),
  getCourseProgress: (courseId) => api.get(`/courses/${courseId}/progress`),
  completeLesson: (courseId, lessonId) => api.post(`/courses/${courseId}/lessons/${lessonId}/complete`),
  addReview: (courseId, payload) => api.post(`/courses/${courseId}/reviews`, payload),
};

export default courseApi;
