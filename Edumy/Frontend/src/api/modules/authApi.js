import api from '../axiosConfig';

export const authApi = {
  login: (email, password) => api.post('/auth/login', { email, password }),
  register: (payload) => api.post('/auth/register', payload),
  googleLogin: (token) => api.post('/auth/google-login', { token }),
  refreshToken: () => api.post('/auth/refresh-token'),
  revokeToken: () => api.post('/auth/revoke-token'),
  forgotPassword: (email) => api.post('/auth/forgot-password', { email }),
  resetPassword: (payload) => api.post('/auth/reset-password', payload),
};

export default authApi;
