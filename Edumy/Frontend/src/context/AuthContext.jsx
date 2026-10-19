import { createContext, useState, useEffect } from 'react';
import api from '../api/axiosConfig';

export const AuthContext = createContext();

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
        const payload = JSON.parse(atob(token.split('.')[1]));
        setUser({
          id: payload.sub || payload.nameid || payload.id || savedUser?.id,
          email: payload.email || savedUser?.email,
          fullName: payload.unique_name || payload.name || savedUser?.fullName || (payload.email ? payload.email.split('@')[0] : 'User'),
          role: payload.role || savedUser?.role || 'Student'
        });
      } catch (error) {
        console.error('Invalid token', error);
        localStorage.removeItem('token');
        localStorage.removeItem('user_profile');
      }
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    const response = await api.post('/auth/login', { email, password });
    const { token, fullName, role, message } = response.data;
    
    localStorage.setItem('token', token);
    
    let userId = null;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      userId = payload.sub || payload.nameid || payload.id;
    } catch (e) {
      console.error('Failed to parse token payload', e);
    }

    const userData = { id: userId, email, fullName, role };
    localStorage.setItem('user_profile', JSON.stringify(userData));
    setUser(userData);
    return message;
  };

  const googleLogin = async (googleToken) => {
    let googleName = null;
    let googleEmail = null;
    
    // Safely extract claims directly from Google ID Token
    try {
      if (googleToken && googleToken.includes('.')) {
        const parts = googleToken.split('.');
        if (parts.length >= 2) {
          const base64Url = parts[1];
          const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
          const jsonPayload = decodeURIComponent(
            atob(base64)
              .split('')
              .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
              .join('')
          );
          const gPayload = JSON.parse(jsonPayload);
          if (gPayload.name) googleName = gPayload.name;
          if (gPayload.email) googleEmail = gPayload.email;
        }
      }
    } catch (e) {
      console.warn('Client Google token decoding fallback:', e);
    }

    const response = await api.post('/auth/google-login', { token: googleToken });
    const { token, fullName, role, message } = response.data;
    
    localStorage.setItem('token', token);
    let userId = null;
    let email = googleEmail || response.data?.email || 'google-user';
    let resolvedName = googleName || fullName || 'Google User';

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      userId = payload.sub || payload.nameid || payload.id;
      if (payload.email) email = payload.email;
      if (payload.unique_name || payload.name) {
        resolvedName = payload.unique_name || payload.name;
      }
    } catch (e) {
      console.error('Failed to parse token payload', e);
    }

    const userData = { id: userId, email, fullName: resolvedName, role };
    localStorage.setItem('user_profile', JSON.stringify(userData));
    setUser(userData);
    return message;
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
    <AuthContext.Provider value={{ user, loading, login, googleLogin, logout, updateUserRole }}>
      {children}
    </AuthContext.Provider>
  );
};
