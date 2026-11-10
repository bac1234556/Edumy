import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:5000/api';

/**
 * Helper to unwrap Spring Boot Modular Monolith ApiResponse<T> envelope:
 * {
 *   "success": true,
 *   "message": "...",
 *   "data": { ... },
 *   "errors": null
 * }
 */
export const unwrapApiResponse = (body) => {
  if (
    body &&
    typeof body === 'object' &&
    !Array.isArray(body) &&
    'success' in body &&
    'data' in body &&
    ('errors' in body || 'message' in body)
  ) {
    const payload = body.data;
    if (payload !== undefined && payload !== null) {
      if (typeof payload === 'object' && !Array.isArray(payload) && body.message && !('message' in payload)) {
        return { ...payload, message: body.message };
      }
      return payload;
    }
    return {
      success: body.success,
      message: body.message,
      errors: body.errors
    };
  }
  return body;
};

const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: Attach JWT Bearer token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Unwrap ApiResponse Envelope & Handle Token Refresh
api.interceptors.response.use(
  (response) => {
    if (response && response.data !== undefined) {
      response.envelope = response.data;
      response.data = unwrapApiResponse(response.data);
    }
    return response;
  },
  async (error) => {
    // Normalize standardized error envelope from GlobalExceptionHandler
    if (error.response && error.response.data && typeof error.response.data === 'object') {
      const errBody = error.response.data;
      if ('success' in errBody && errBody.success === false) {
        error.response.envelope = errBody;
        error.response.data = {
          ...errBody,
          message: errBody.message || 'Đã xảy ra lỗi từ máy chủ.',
          errors: errBody.errors || null,
        };
      }
    }

    const originalRequest = error.config;
    if (
      error.response?.status === 401 &&
      originalRequest &&
      !originalRequest._retry &&
      originalRequest.url !== '/auth/login' &&
      originalRequest.url !== '/auth/refresh-token'
    ) {
      originalRequest._retry = true;
      try {
        const res = await axios.post(
          `${API_BASE_URL}/auth/refresh-token`,
          {},
          { withCredentials: true }
        );
        const unwrapped = unwrapApiResponse(res.data);
        const newToken = unwrapped?.token;
        if (newToken) {
          localStorage.setItem('token', newToken);
          api.defaults.headers.common['Authorization'] = `Bearer ${newToken}`;
          originalRequest.headers['Authorization'] = `Bearer ${newToken}`;
          return api(originalRequest);
        }
      } catch (refreshError) {
        localStorage.removeItem('token');
        localStorage.removeItem('user_profile');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);

export default api;
