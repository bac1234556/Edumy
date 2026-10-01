import { createContext, useState, useEffect } from 'react';
import api from '../api/axiosConfig';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if token exists in local storage
    const token = localStorage.getItem('token');
    if (token) {
      // Decode JWT token to get user info (simple base64 decode for payload)
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        setUser({
          id: payload.sub,
          email: payload.email,
          fullName: payload.unique_name, // ClaimTypes.Name maps to unique_name in JWT
          role: payload.role
        });
      } catch (error) {
        console.error('Invalid token', error);
        localStorage.removeItem('token');
      }
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    const response = await api.post('/auth/login', { email, password });
    const { token, fullName, role, message } = response.data;
    
    localStorage.setItem('token', token);
    
    // Parse the token again to be sure or just use the response data
    setUser({ email, fullName, role });
    return message;
  };

  const googleLogin = async (googleToken) => {
    const response = await api.post('/auth/google-login', { token: googleToken });
    const { token, fullName, role, message } = response.data;
    
    localStorage.setItem('token', token);
    setUser({ email: 'google-user', fullName, role }); // Actual email can be parsed from payload if needed
    return message;
  };

  const logout = () => {
    localStorage.removeItem('token');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, googleLogin, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
