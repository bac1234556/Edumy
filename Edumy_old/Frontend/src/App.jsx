import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { useContext } from 'react';
import { AuthContext } from './context/AuthContext';
import HomePage from './pages/HomePage';
import CourseList from './pages/CourseList';
import CourseDetail from './pages/CourseDetail';
import Login from './pages/Login';
import Register from './pages/Register';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';
import InstructorDashboard from './pages/InstructorDashboard';
import CourseCreate from './pages/CourseCreate';
import Cart from './pages/Cart';
import MyLearning from './pages/MyLearning';
import CoursePlayer from './pages/CoursePlayer';
import AdminDashboard from './pages/AdminDashboard';
import PaymentSuccess from './pages/PaymentSuccess';
import PaymentCancel from './pages/PaymentCancel';
import MockPaymentGateway from './pages/MockPaymentGateway';
import CertificateView from './pages/CertificateView';
import Wishlist from './pages/Wishlist';
import UserProfile from './pages/UserProfile';
import ProtectedRoute from './components/ProtectedRoute';
import Navbar from './components/Navbar';
import Footer from './components/Footer';

function App() {
  const { user, logout } = useContext(AuthContext);

  return (
    <Router>
      <div className="App">
        <Navbar />

        <main className="main-content">
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/courses" element={<CourseList />} />
            <Route path="/courses/:id" element={<CourseDetail />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/forgot-password" element={<ForgotPassword />} />
            <Route path="/reset-password" element={<ResetPassword />} />
            
            <Route path="/cart" element={<Cart />} />
            <Route path="/my-courses" element={
              <ProtectedRoute allowedRoles={['Student', 'Instructor', 'Admin']}>
                <MyLearning />
              </ProtectedRoute>
            } />
            <Route path="/my-courses/:id/learn" element={
              <ProtectedRoute allowedRoles={['Student', 'Instructor', 'Admin']}>
                <CoursePlayer />
              </ProtectedRoute>
            } />
            
            <Route path="/wishlist" element={
              <ProtectedRoute allowedRoles={['Student', 'Instructor', 'Admin']}>
                <Wishlist />
              </ProtectedRoute>
            } />
            <Route path="/profile" element={
              <ProtectedRoute allowedRoles={['Student', 'Instructor', 'Admin']}>
                <UserProfile />
              </ProtectedRoute>
            } />

            <Route path="/instructor" element={
              <ProtectedRoute allowedRoles={['Instructor', 'Admin']}>
                <InstructorDashboard />
              </ProtectedRoute>
            } />
            <Route path="/instructor/courses/new" element={
              <ProtectedRoute allowedRoles={['Instructor', 'Admin']}>
                <CourseCreate />
              </ProtectedRoute>
            } />
            <Route path="/admin" element={
              <ProtectedRoute allowedRoles={['Admin']}>
                <AdminDashboard />
              </ProtectedRoute>
            } />
            <Route path="/payment" element={<MockPaymentGateway />} />
            <Route path="/payment-success" element={<PaymentSuccess />} />
            <Route path="/payment-cancel" element={<PaymentCancel />} />
            <Route path="/certificates/:url" element={<CertificateView />} />
          </Routes>
        </main>
        
        <Footer />
      </div>
    </Router>
  );
}

export default App;

