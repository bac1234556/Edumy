import React, { useContext, useState, useRef, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { 
  Search, 
  ShoppingCart, 
  Heart, 
  BookOpen, 
  LayoutDashboard, 
  Shield, 
  User, 
  LogOut, 
  ChevronDown,
  Sparkles,
  Menu,
  X
} from 'lucide-react';
import { AuthContext } from '../context/AuthContext';
import { CartContext } from '../context/CartContext';
import LiveCourseSearchBar from './LiveCourseSearchBar';
import './Navbar.css';

const Navbar = () => {
  const { user, logout } = useContext(AuthContext);
  const { cartCount, clearCartState } = useContext(CartContext);
  const [searchTerm, setSearchTerm] = useState('');
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

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    if (searchTerm.trim()) {
      navigate(`/courses?search=${encodeURIComponent(searchTerm.trim())}`);
    }
  };

  const handleLogout = () => {
    setIsDropdownOpen(false);
    setMobileMenuOpen(false);
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
        <Link to="/" className="navbar-brand-link">
          <div className="brand-icon-box">E</div>
          <span className="brand-name-text">
            Edu<span>my</span>
          </span>
        </Link>

        {/* Explore Courses Button */}
        <Link to="/courses" className="nav-explore-btn hide-on-mobile">
          <BookOpen size={16} /> Khám phá khóa học
        </Link>

        {/* Live Search Input Bar */}
        <div className="navbar-search-wrapper hide-on-mobile" style={{ flex: 1, maxWidth: 540 }}>
          <LiveCourseSearchBar variant="navbar" placeholder="Tìm kiếm khóa học, kỹ năng, giảng viên..." />
        </div>

        {/* Right Navigation Actions */}
        <div className="navbar-nav-right">
          {/* Instructor shortcut */}
          {user?.role === 'Instructor' ? (
            <Link to="/instructor" className="nav-text-link hide-on-mobile">
              <LayoutDashboard size={15} style={{ display: 'inline', marginRight: 4, verticalAlign: -1 }} /> 
              Kênh Giảng viên
            </Link>
          ) : user?.role === 'Admin' ? (
            <Link to="/admin" className="nav-text-link hide-on-mobile">
              <Shield size={15} style={{ display: 'inline', marginRight: 4, verticalAlign: -1 }} /> 
              Quản trị viên
            </Link>
          ) : (
            <Link to={user ? "/profile" : "/register?role=instructor"} className="nav-text-link hide-on-mobile">
              Dạy học trên Edumy
            </Link>
          )}

          {/* My Learning for logged in user */}
          {user && (
            <Link to="/my-courses" className="nav-text-link hide-on-mobile">
              Khóa học của tôi
            </Link>
          )}

          {/* Wishlist Icon */}
          {user && (
            <Link to="/wishlist" className="nav-icon-action-btn" title="Danh sách yêu thích">
              <Heart size={20} />
            </Link>
          )}

          {/* Cart Icon with Dynamic Badge */}
          <Link to="/cart" className="nav-icon-action-btn" title="Giỏ hàng">
            <ShoppingCart size={20} />
            {cartCount > 0 && (
              <span className="nav-cart-badge">{cartCount > 9 ? '9+' : cartCount}</span>
            )}
          </Link>

          {/* User Auth Section */}
          {user ? (
            <div className="user-menu-wrapper" ref={dropdownRef}>
              <button 
                className="user-avatar-trigger"
                onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                aria-expanded={isDropdownOpen}
              >
                <div className="user-initial-badge">
                  {(user.fullName || user.email || 'U')[0].toUpperCase()}
                </div>
                <ChevronDown size={14} color="#94a3b8" />
              </button>

              {isDropdownOpen && (
                <div className="user-dropdown-card">
                  <div className="dropdown-user-header">
                    <div className="dropdown-user-name">
                      {user.fullName || user.email?.split('@')[0]}
                    </div>
                    <span className="dropdown-role-badge">
                      {user.role === 'Admin' ? 'Admin Quản trị' :
                       user.role === 'Instructor' ? 'Giảng viên' : 'Học viên'}
                    </span>
                  </div>

                  <Link 
                    to="/my-courses" 
                    className="dropdown-item-link"
                    onClick={() => setIsDropdownOpen(false)}
                  >
                    <BookOpen size={16} /> Khóa học của tôi
                  </Link>

                  <Link 
                    to="/wishlist" 
                    className="dropdown-item-link"
                    onClick={() => setIsDropdownOpen(false)}
                  >
                    <Heart size={16} /> Danh sách yêu thích
                  </Link>

                  <Link 
                    to="/profile" 
                    className="dropdown-item-link"
                    onClick={() => setIsDropdownOpen(false)}
                  >
                    <User size={16} /> Hồ sơ cá nhân
                  </Link>

                  {(user.role === 'Instructor' || user.role === 'Admin') && (
                    <Link 
                      to="/instructor" 
                      className="dropdown-item-link"
                      onClick={() => setIsDropdownOpen(false)}
                    >
                      <LayoutDashboard size={16} /> Quản lý khóa giảng dạy
                    </Link>
                  )}

                  {user.role === 'Admin' && (
                    <Link 
                      to="/admin" 
                      className="dropdown-item-link"
                      onClick={() => setIsDropdownOpen(false)}
                    >
                      <Shield size={16} /> Bảng điều khiển Quản trị
                    </Link>
                  )}

                  <div className="dropdown-divider"></div>

                  <button className="dropdown-logout-btn" onClick={handleLogout}>
                    <LogOut size={16} /> Đăng xuất
                  </button>
                </div>
              )}
            </div>
          ) : (
            <div className="auth-buttons-group">
              <Link to="/login" className="btn-nav-login">
                Đăng nhập
              </Link>
              <Link to="/register" className="btn-nav-signup">
                Đăng ký
              </Link>
            </div>
          )}

          {/* Mobile Hamburger Toggle Button */}
          <button 
            type="button"
            className="navbar-mobile-toggle-btn"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            aria-label="Toggle navigation menu"
            aria-expanded={mobileMenuOpen}
          >
            {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
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
                className="mobile-drawer-close-btn"
                onClick={() => setMobileMenuOpen(false)}
                aria-label="Close menu"
              >
                <X size={22} />
              </button>
            </div>

            {/* Mobile Live Search */}
            <div className="mobile-drawer-search-wrap" style={{ margin: '0.75rem 0' }}>
              <LiveCourseSearchBar 
                variant="mobile" 
                placeholder="Tìm khóa học, giảng viên..."
                onCloseMobile={() => setMobileMenuOpen(false)}
              />
            </div>

            {/* Mobile Navigation Links */}
            <div className="mobile-drawer-nav-list">
              <Link 
                to="/courses" 
                className="mobile-drawer-link"
                onClick={() => setMobileMenuOpen(false)}
              >
                <BookOpen size={20} /> Khám phá khóa học
              </Link>

              {user && (
                <>
                  <Link 
                    to="/my-courses" 
                    className="mobile-drawer-link"
                    onClick={() => setMobileMenuOpen(false)}
                  >
                    <BookOpen size={20} /> Khóa học của tôi
                  </Link>

                  <Link 
                    to="/wishlist" 
                    className="mobile-drawer-link"
                    onClick={() => setMobileMenuOpen(false)}
                  >
                    <Heart size={20} /> Danh sách yêu thích
                  </Link>
                </>
              )}

              {user?.role === 'Instructor' ? (
                <Link 
                  to="/instructor" 
                  className="mobile-drawer-link"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  <LayoutDashboard size={20} /> Kênh Giảng viên
                </Link>
              ) : user?.role === 'Admin' ? (
                <Link 
                  to="/admin" 
                  className="mobile-drawer-link"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  <Shield size={20} /> Bảng điều khiển Quản trị
                </Link>
              ) : (
                <Link 
                  to={user ? "/profile" : "/register?role=instructor"} 
                  className="mobile-drawer-link"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  <LayoutDashboard size={20} /> Dạy học trên Edumy
                </Link>
              )}

              {user && (
                <Link 
                  to="/profile" 
                  className="mobile-drawer-link"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  <User size={20} /> Hồ sơ cá nhân
                </Link>
              )}
            </div>

            {/* Mobile Auth Actions */}
            <div className="mobile-drawer-footer">
              {user ? (
                <div className="mobile-user-profile-bar">
                  <div className="mobile-user-info">
                    <span className="mobile-user-name">{user.fullName || user.email}</span>
                    <span className="mobile-user-role">{user.role}</span>
                  </div>
                  <button className="mobile-logout-btn" onClick={() => {
                    setMobileMenuOpen(false);
                    handleLogout();
                  }}>
                    <LogOut size={18} /> Đăng xuất
                  </button>
                </div>
              ) : (
                <div className="mobile-auth-cta-grid">
                  <Link 
                    to="/login" 
                    className="mobile-btn-login"
                    onClick={() => setMobileMenuOpen(false)}
                  >
                    Đăng nhập
                  </Link>
                  <Link 
                    to="/register" 
                    className="mobile-btn-signup"
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
