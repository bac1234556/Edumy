import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import HomePage from './pages/HomePage';
import CourseList from './pages/CourseList';
import CourseDetail from './pages/CourseDetail';
import Login from './pages/Login';
import Register from './pages/Register';
import VerifyEmail from './pages/VerifyEmail';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';
import InstructorDashboard from './pages/InstructorDashboard';
import CourseCreate from './pages/CourseCreate';
import Cart from './pages/Cart';
import OrderHistory from './pages/OrderHistory';
import MyLearning from './pages/MyLearning';
import CoursePlayer from './pages/CoursePlayer';
import AdminDashboard from './pages/AdminDashboard';
import PaymentSuccess from './pages/PaymentSuccess';
import PaymentCancel from './pages/PaymentCancel';
import PaymentQR from './pages/PaymentQR';
import CourseCurriculumEditor from './pages/CourseCurriculumEditor';
import UserProfile from './pages/UserProfile';
import ProtectedRoute from './components/ProtectedRoute';
import Navbar from './components/Navbar';
import Footer from './components/Footer';

function App() {
  return (
    <Router>
      <div className="App">
        <Navbar />

        <main className="main-content">
          <Routes>
            {/* Public / Guest Routes */}
            <Route path="/" element={<HomePage />} />
            <Route path="/courses" element={<CourseList />} />
            <Route path="/courses/:id" element={<CourseDetail />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/verify-email" element={<VerifyEmail />} />
            <Route path="/forgot-password" element={<ForgotPassword />} />
            <Route path="/reset-password" element={<ResetPassword />} />

            {/* Sales & Payment Routes */}
            <Route path="/cart" element={<Cart />} />
            <Route path="/orders" element={
              <ProtectedRoute allowedRoles={['Student', 'Instructor', 'Admin']}>
                <OrderHistory />
              </ProtectedRoute>
            } />
            <Route path="/payment" element={<PaymentQR />} />
            <Route path="/payment/qr" element={<PaymentQR />} />
            <Route path="/payment/mock" element={<Navigate to="/payment" replace />} />
            <Route path="/payment-success" element={<PaymentSuccess />} />
            <Route path="/payment-cancel" element={<PaymentCancel />} />

            {/* Learning Routes */}
            <Route path="/my-courses" element={
              <ProtectedRoute allowedRoles={['Student', 'Instructor', 'Admin']}>
                <MyLearning />
              </ProtectedRoute>
            } />
            <Route path="/my-learning" element={<Navigate to="/my-courses" replace />} />
            <Route path="/my-courses/:id/learn" element={
              <ProtectedRoute allowedRoles={['Student', 'Instructor', 'Admin']}>
                <CoursePlayer />
              </ProtectedRoute>
            } />

            {/* Personal Profile */}
            <Route path="/profile" element={
              <ProtectedRoute allowedRoles={['Student', 'Instructor', 'Admin']}>
                <UserProfile />
              </ProtectedRoute>
            } />

            {/* Instructor Routes */}
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
            <Route path="/instructor/courses/:id/curriculum" element={
              <ProtectedRoute allowedRoles={['Instructor', 'Admin']}>
                <CourseCurriculumEditor />
              </ProtectedRoute>
            } />

            {/* Admin Routes */}
            <Route path="/admin" element={
              <ProtectedRoute allowedRoles={['Admin']}>
                <AdminDashboard />
              </ProtectedRoute>
            } />
          </Routes>
        </main>

        <Footer />
      </div>
    </Router>
  );
}

export default App;
