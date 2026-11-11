import { useContext } from 'react';
import { Navigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { SkeletonBox } from './Skeleton';

function ProtectedRoute({ children, allowedRoles }) {
  const { user, loading } = useContext(AuthContext);

  if (loading) {
    return (
      <div className="container py-5 my-5 text-center" style={{ minHeight: '60vh', maxWidth: '600px' }}>
        <div
          className="bg-white rounded-2xl shadow-sm p-5 d-flex flex-column align-items-center gap-3"
          style={{ border: '1px solid rgba(226, 232, 240, 0.85)' }}
          aria-busy="true"
        >
          <SkeletonBox width="64px" height="64px" borderRadius="9999px" />
          <SkeletonBox width="260px" height="26px" borderRadius="0.5rem" />
          <SkeletonBox width="380px" height="18px" borderRadius="0.5rem" />
        </div>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/" replace />;
  }

  return children;
}

export default ProtectedRoute;
