import api from '../axiosConfig';

export const mlApi = {
  getSimilarCourses: (courseId, limit = 4) => api.get(`/v1/courses/${courseId}/similar`, { params: { limit } }),
  getBundleRecommendation: (courseId) => api.get(`/v1/courses/${courseId}/bundle-recommendation`),
  getStudentRecommendations: () => api.get('/courses/recommend'),
};

export default mlApi;
