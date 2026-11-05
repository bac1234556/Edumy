import { useContext } from 'react';
import { Navigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { SkeletonBox } from './Skeleton';

function ProtectedRoute({ children, allowedRoles }) {
  const { user, loading } = useContext(AuthContext);

  if (loading) {
    return (
      <div className="container py-5 my-5 text-center" style={{ minHeight: '60vh', maxWidth: '600px' }}>
        <div className="d-flex flex-column align-items-center gap-3">
          <SkeletonBox width="64px" height="64px" borderRadius="50%" />
          <SkeletonBox width="280px" height="28px" />
          <SkeletonBox width="400px" height="18px" />
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
