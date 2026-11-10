import api from '../axiosConfig';

export const mlApi = {
  suggestCategory: (title, description) => api.post('/v1/ai/suggest-category', { title, description }),
  analyzeSentiment: (rating, comment) => api.post('/v1/ai/sentiment-analysis', { rating, comment }),
  getSimilarCourses: (courseId, limit = 4) => api.get(`/v1/courses/${courseId}/similar`, { params: { limit } }),
  getBundleRecommendation: (courseId) => api.get(`/v1/courses/${courseId}/bundle-recommendation`),
  getStudentRecommendations: () => api.get('/courses/recommend'),
};

export default mlApi;
