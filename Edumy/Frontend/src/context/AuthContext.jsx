import { createContext, useState, useEffect } from 'react';
import { jwtDecode } from 'jwt-decode';
import api from '../api/axiosConfig';
import { authApi } from '../api/modules/authApi';
import { courseApi } from '../api/modules/courseApi';

export const AuthContext = createContext();

// Helper an toàn giải mã JWT Token hỗ trợ tiếng Việt UTF-8 chuẩn xác
const parseJwtPayload = (token) => {
  if (!token || typeof token !== 'string') return null;
  try {
    // 1. Thử dùng thư viện jwtDecode chuyên dụng
    return jwtDecode(token);
  } catch (err) {
    try {
      // 2. Fallback Base64URL + decodeURIComponent UTF-8
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch (fallbackErr) {
      console.error('Failed to parse token payload:', fallbackErr);
      return null;
    }
  }
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const savedUserStr = localStorage.getItem('user_profile');
    let savedUser = null;
    if (savedUserStr) {
      try {
        savedUser = JSON.parse(savedUserStr);
      } catch (e) {
        // ignore
      }
    }

    if (token) {
      try {
        const payload = parseJwtPayload(token);
        if (payload) {
          setUser({
            id: payload.sub || payload.nameid || payload.id || savedUser?.id,
            email: payload.email || savedUser?.email,
            fullName: savedUser?.fullName || payload.unique_name || payload.name || (payload.email ? payload.email.split('@')[0] : 'User'),
            role: payload.role || savedUser?.role || 'Student'
          });
        } else if (savedUser) {
          setUser(savedUser);
        }
      } catch (error) {
        console.error('Invalid token', error);
        localStorage.removeItem('token');
        localStorage.removeItem('user_profile');
      }
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    const response = await authApi.login(email, password);
    const { token, fullName, role, message } = response.data;
    
    localStorage.setItem('token', token);
    
    let userId = null;
    let decodedName = fullName;
    try {
      const payload = parseJwtPayload(token);
      if (payload) {
        userId = payload.sub || payload.nameid || payload.id;
        if (!decodedName && (payload.unique_name || payload.name)) {
          decodedName = payload.unique_name || payload.name;
        }
      }
    } catch (e) {
      console.error('Failed to parse token payload', e);
    }

    const userData = { id: userId, email, fullName: decodedName || fullName, role };
    localStorage.setItem('user_profile', JSON.stringify(userData));
    setUser(userData);
    return message;
  };

  const googleLogin = async (googleToken) => {
    let googleName = null;
    let googleEmail = null;
    
    // Safely extract claims directly from Google ID Token
    try {
      const gPayload = parseJwtPayload(googleToken);
      if (gPayload) {
        if (gPayload.name) googleName = gPayload.name;
        if (gPayload.email) googleEmail = gPayload.email;
      }
    } catch (e) {
      console.warn('Client Google token decoding fallback:', e);
    }

    const response = await authApi.googleLogin(googleToken);
    const { token, fullName, role, message } = response.data;
    
    localStorage.setItem('token', token);
    let userId = null;
    let email = googleEmail || response.data?.email || 'google-user';
    let resolvedName = fullName || googleName || 'Google User';

    try {
      const payload = parseJwtPayload(token);
      if (payload) {
        userId = payload.sub || payload.nameid || payload.id;
        if (payload.email) email = payload.email;
        if (payload.unique_name || payload.name) {
          resolvedName = payload.unique_name || payload.name;
        }
      }
    } catch (e) {
      console.error('Failed to parse token payload', e);
    }

    const userData = { id: userId, email, fullName: resolvedName, role };
    localStorage.setItem('user_profile', JSON.stringify(userData));
    setUser(userData);
    return message;
  };

  const [enrolledCourseIds, setEnrolledCourseIds] = useState([]);

  const refreshEnrolledCourses = async () => {
    const token = localStorage.getItem('token');
    if (!token) {
      setEnrolledCourseIds([]);
      return [];
    }
    try {
      const res = await courseApi.getEnrolledIds();
      if (Array.isArray(res.data)) {
        const ids = res.data.map(id => Number(id)).filter(id => !isNaN(id));
        setEnrolledCourseIds(ids);
        return ids;
      }
    } catch (err) {
      console.warn('Could not fetch enrolled course ids:', err);
    }
    return [];
  };

  useEffect(() => {
    if (user) {
      refreshEnrolledCourses();
    } else {
      setEnrolledCourseIds([]);
    }
  }, [user]);

  const isCourseEnrolled = (courseId) => {
    if (!courseId) return false;
    return enrolledCourseIds.includes(Number(courseId));
  };

  const updateUserRole = (newRole) => {
    if (!user) return;
    const updated = { ...user, role: newRole };
    localStorage.setItem('user_profile', JSON.stringify(updated));
    setUser(updated);
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user_profile');
    localStorage.removeItem('edumy_token');
    sessionStorage.clear();
    setEnrolledCourseIds([]);
    try {
      document.cookie.split(';').forEach((c) => {
        document.cookie = c
          .replace(/^ +/, '')
          .replace(/=.*/, '=;expires=' + new Date().toUTCString() + ';path=/');
      });
    } catch (e) {
      console.warn('Could not clear cookies:', e);
    }
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ 
      user, 
      loading, 
      login, 
      googleLogin, 
      logout, 
      updateUserRole,
      enrolledCourseIds,
      isCourseEnrolled,
      refreshEnrolledCourses
    }}>
      {children}
    </AuthContext.Provider>
  );
};
