import React, { useContext, useState, useRef, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { 
  ShoppingCart, 
  BookOpen, 
  LayoutDashboard, 
  Shield, 
  User, 
  LogOut, 
  ChevronDown,
  Receipt,
  Menu,
  X
} from 'lucide-react';
import { AuthContext } from '../context/AuthContext';
import { CartContext } from '../context/CartContext';
import LiveCourseSearchBar from './LiveCourseSearchBar';
import api from '../api/axiosConfig';
import './Navbar.css';

const Navbar = () => {
  const { user, logout } = useContext(AuthContext);
  const { cartCount, clearCartState } = useContext(CartContext);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const dropdownRef = useRef(null);
  const navigate = useNavigate();

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = async () => {
    setIsDropdownOpen(false);
    setMobileMenuOpen(false);
    try {
      await api.post('/v1/auth/logout');
    } catch {
      // ignore if token already expired
    }
    if (clearCartState) {
      clearCartState();
    }
    logout();
    navigate('/');
  };

  return (
    <nav className="edumy-navbar">
      <div className="navbar-inner-wrapper">
        {/* Brand Logo */}
        <Link to="/" className="navbar-brand-link cursor-pointer transition-all duration-200 active:scale-95">
          <div className="brand-icon-box" aria-hidden="true">E</div>
          <span className="brand-name-text">
            Edu<span>my</span>
          </span>
        </Link>

        {/* Explore Courses Button */}
        <Link to="/courses" className="nav-explore-btn hide-on-mobile cursor-pointer transition-all duration-200 active:scale-95">
          <BookOpen size={16} strokeWidth={1.75} aria-hidden="true" />
          <span>Danh mục khóa học</span>
        </Link>

        {/* Live Search Input Bar */}
        <div className="navbar-search-wrapper hide-on-mobile" style={{ flex: 1, maxWidth: 540 }}>
          <LiveCourseSearchBar variant="navbar" placeholder="Tìm kiếm khóa học theo tên..." />
        </div>

        {/* Right Navigation Actions */}
        <div className="navbar-nav-right">
          {/* Role shortcuts */}
          {(user?.role === 'Instructor' || user?.role === 'Admin') && (
            <Link to="/instructor" className="nav-text-link hide-on-mobile cursor-pointer transition-all duration-200 active:scale-95">
              <LayoutDashboard size={15} strokeWidth={1.75} aria-hidden="true" /> 
              <span>Kênh Giảng viên</span>
            </Link>
          )}
          {user?.role === 'Admin' && (
            <Link to="/admin" className="nav-text-link hide-on-mobile cursor-pointer transition-all duration-200 active:scale-95">
              <Shield size={15} strokeWidth={1.75} aria-hidden="true" /> 
              <span>Quản trị hệ thống</span>
            </Link>
          )}

          {/* My Learning & Orders for logged in user */}
          {user && (
            <>
              <Link to="/my-courses" className="nav-text-link hide-on-mobile cursor-pointer transition-all duration-200 active:scale-95">
                <span>Khóa học của tôi</span>
              </Link>
              <Link to="/orders" className="nav-text-link hide-on-mobile cursor-pointer transition-all duration-200 active:scale-95">
                <Receipt size={15} strokeWidth={1.75} aria-hidden="true" />
                <span>Đơn hàng</span>
              </Link>
            </>
          )}

          {/* Cart Icon with Dynamic Badge */}
          <Link to="/cart" className="nav-icon-action-btn cursor-pointer transition-all duration-200 active:scale-95" title="Giỏ hàng" aria-label="Giỏ hàng">
            <ShoppingCart size={20} strokeWidth={1.75} aria-hidden="true" />
            {cartCount > 0 && (
              <span className="nav-cart-badge">{cartCount > 9 ? '9+' : cartCount}</span>
            )}
          </Link>

          {/* User Auth Section */}
          {user ? (
            <div className="user-menu-wrapper" ref={dropdownRef}>
              <button 
                type="button"
                className="user-avatar-trigger cursor-pointer transition-all duration-200 active:scale-95"
                onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                aria-expanded={isDropdownOpen}
                aria-label="Menu tài khoản"
              >
                <div className="user-initial-badge" aria-hidden="true">
                  {(user.fullName || user.email || 'U')[0].toUpperCase()}
                </div>
                <ChevronDown size={14} strokeWidth={2} color="#64748b" aria-hidden="true" />
              </button>

              {isDropdownOpen && (
                <div className="user-dropdown-card rounded-2xl shadow-sm">
                  <div className="dropdown-user-header">
                    <div className="dropdown-user-name line-clamp-1">
                      {user.fullName || user.email?.split('@')[0]}
                    </div>
                    <span className="dropdown-role-badge pill-badge">
                      {user.role === 'Admin' ? 'Quản trị viên (ADMIN)' :
                       user.role === 'Instructor' ? 'Giảng viên (INSTRUCTOR)' : 'Học viên (STUDENT)'}
                    </span>
                  </div>

                  <Link 
                    to="/my-courses" 
                    className="dropdown-item-link cursor-pointer transition-all duration-200"
                    onClick={() => setIsDropdownOpen(false)}
                  >
                    <BookOpen size={16} strokeWidth={1.75} aria-hidden="true" /> Khóa học đã ghi danh
                  </Link>

                  <Link 
                    to="/orders" 
                    className="dropdown-item-link cursor-pointer transition-all duration-200"
                    onClick={() => setIsDropdownOpen(false)}
                  >
                    <Receipt size={16} strokeWidth={1.75} aria-hidden="true" /> Lịch sử đơn hàng
                  </Link>

                  <Link 
                    to="/profile" 
                    className="dropdown-item-link cursor-pointer transition-all duration-200"
                    onClick={() => setIsDropdownOpen(false)}
                  >
                    <User size={16} strokeWidth={1.75} aria-hidden="true" /> Hồ sơ cá nhân
                  </Link>

                  {(user.role === 'Instructor' || user.role === 'Admin') && (
                    <Link 
                      to="/instructor" 
                      className="dropdown-item-link cursor-pointer transition-all duration-200"
                      onClick={() => setIsDropdownOpen(false)}
                    >
                      <LayoutDashboard size={16} strokeWidth={1.75} aria-hidden="true" /> Quản lý khóa giảng dạy
                    </Link>
                  )}

                  {user.role === 'Admin' && (
                    <Link 
                      to="/admin" 
                      className="dropdown-item-link cursor-pointer transition-all duration-200"
                      onClick={() => setIsDropdownOpen(false)}
                    >
                      <Shield size={16} strokeWidth={1.75} aria-hidden="true" /> Bảng điều khiển Quản trị
                    </Link>
                  )}

                  <div className="dropdown-divider"></div>

                  <button type="button" className="dropdown-logout-btn cursor-pointer transition-all duration-200" onClick={handleLogout}>
                    <LogOut size={16} strokeWidth={1.75} aria-hidden="true" /> Đăng xuất
                  </button>
                </div>
              )}
            </div>
          ) : (
            <div className="auth-buttons-group">
              <Link to="/login" className="btn-nav-login cursor-pointer transition-all duration-200 active:scale-95">
                Đăng nhập
              </Link>
              <Link to="/register" className="btn-nav-signup cursor-pointer transition-all duration-200 active:scale-95 hover:opacity-90">
                Đăng ký
              </Link>
            </div>
          )}

          {/* Mobile Hamburger Toggle Button */}
          <button 
            type="button"
            className="navbar-mobile-toggle-btn cursor-pointer transition-all duration-200 active:scale-95"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            aria-label="Mở menu điều hướng"
            aria-expanded={mobileMenuOpen}
          >
            {mobileMenuOpen ? <X size={24} strokeWidth={1.75} /> : <Menu size={24} strokeWidth={1.75} />}
          </button>
        </div>
      </div>

      {/* Mobile Navigation Slide-out Drawer */}
      {mobileMenuOpen && (
        <div className="mobile-drawer-overlay" onClick={() => setMobileMenuOpen(false)}>
          <div className="mobile-drawer-content" onClick={(e) => e.stopPropagation()}>
            <div className="mobile-drawer-header">
              <div className="brand-name-text">
                Edu<span>my</span>
              </div>
              <button 
                type="button"
                className="mobile-drawer-close-btn cursor-pointer transition-all duration-200 active:scale-95"
                onClick={() => setMobileMenuOpen(false)}
                aria-label="Đóng menu"
              >
                <X size={22} strokeWidth={1.75} />
              </button>
            </div>

            {/* Mobile Live Search */}
            <div className="mobile-drawer-search-wrap" style={{ margin: '0.75rem 0 1.25rem' }}>
              <LiveCourseSearchBar 
                variant="mobile" 
                placeholder="Tìm khóa học..."
                onCloseMobile={() => setMobileMenuOpen(false)}
              />
            </div>

            {/* Mobile Navigation Links */}
            <div className="mobile-drawer-nav-list">
              <Link 
                to="/courses" 
                className="mobile-drawer-link cursor-pointer transition-all duration-200"
                onClick={() => setMobileMenuOpen(false)}
              >
                <BookOpen size={20} strokeWidth={1.75} aria-hidden="true" /> Danh mục khóa học
              </Link>

              {user && (
                <>
                  <Link 
                    to="/my-courses" 
                    className="mobile-drawer-link cursor-pointer transition-all duration-200"
                    onClick={() => setMobileMenuOpen(false)}
                  >
                    <BookOpen size={20} strokeWidth={1.75} aria-hidden="true" /> Khóa học của tôi
                  </Link>
                  <Link 
                    to="/orders" 
                    className="mobile-drawer-link cursor-pointer transition-all duration-200"
                    onClick={() => setMobileMenuOpen(false)}
                  >
                    <Receipt size={20} strokeWidth={1.75} aria-hidden="true" /> Đơn hàng của tôi
                  </Link>
                </>
              )}

              {(user?.role === 'Instructor' || user?.role === 'Admin') && (
                <Link 
                  to="/instructor" 
                  className="mobile-drawer-link cursor-pointer transition-all duration-200"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  <LayoutDashboard size={20} strokeWidth={1.75} aria-hidden="true" /> Kênh Giảng viên
                </Link>
              )}

              {user?.role === 'Admin' && (
                <Link 
                  to="/admin" 
                  className="mobile-drawer-link cursor-pointer transition-all duration-200"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  <Shield size={20} strokeWidth={1.75} aria-hidden="true" /> Bảng điều khiển Quản trị
                </Link>
              )}

              {user && (
                <Link 
                  to="/profile" 
                  className="mobile-drawer-link cursor-pointer transition-all duration-200"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  <User size={20} strokeWidth={1.75} aria-hidden="true" /> Hồ sơ cá nhân
                </Link>
              )}
            </div>

            {/* Mobile Auth Actions */}
            <div className="mobile-drawer-footer">
              {user ? (
                <div className="mobile-user-profile-bar">
                  <div className="mobile-user-info">
                    <span className="mobile-user-name line-clamp-1">{user.fullName || user.email}</span>
                    <span className="mobile-user-role pill-badge">{user.role}</span>
                  </div>
                  <button
                    type="button"
                    className="mobile-logout-btn cursor-pointer transition-all duration-200 active:scale-95"
                    onClick={() => {
                      setMobileMenuOpen(false);
                      handleLogout();
                    }}
                  >
                    <LogOut size={18} strokeWidth={1.75} aria-hidden="true" /> Đăng xuất
                  </button>
                </div>
              ) : (
                <div className="mobile-auth-cta-grid">
                  <Link 
                    to="/login" 
                    className="mobile-btn-login cursor-pointer transition-all duration-200 active:scale-95"
                    onClick={() => setMobileMenuOpen(false)}
                  >
                    Đăng nhập
                  </Link>
                  <Link 
                    to="/register" 
                    className="mobile-btn-signup cursor-pointer transition-all duration-200 active:scale-95 hover:opacity-90"
                    onClick={() => setMobileMenuOpen(false)}
                  >
                    Đăng ký tài khoản
                  </Link>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </nav>
  );
};

export default Navbar;
