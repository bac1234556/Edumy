import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import 'bootstrap/dist/css/bootstrap.min.css';
import './index.css'
import App from './App.jsx'
import { AuthProvider } from './context/AuthContext'
import { CartProvider } from './context/CartContext'
import { GoogleOAuthProvider } from '@react-oauth/google';

// Use environment variable if provided, or a safe default ID to prevent SDK crashes
const rawClientId = import.meta.env.VITE_GOOGLE_CLIENT_ID || '';
const googleClientId = (rawClientId && rawClientId !== 'YOUR_GOOGLE_CLIENT_ID_HERE') 
  ? rawClientId 
  : 'edumy-local-dev-client.apps.googleusercontent.com';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <GoogleOAuthProvider clientId={googleClientId}>
      <AuthProvider>
        <CartProvider>
          <App />
        </CartProvider>
      </AuthProvider>
    </GoogleOAuthProvider>
  </StrictMode>,
)
