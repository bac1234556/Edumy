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
  Sparkles
} from 'lucide-react';
import { AuthContext } from '../context/AuthContext';
import { CartContext } from '../context/CartContext';
import './Navbar.css';

const Navbar = () => {
  const { user, logout } = useContext(AuthContext);
  const { cartCount } = useContext(CartContext);
  const [searchTerm, setSearchTerm] = useState('');
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
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

        {/* Search Input Bar */}
        <form className="navbar-search-box" onSubmit={handleSearchSubmit}>
          <button type="submit" className="search-icon-btn" aria-label="Search">
            <Search size={18} />
          </button>
          <input 
            type="text" 
            placeholder="Tìm kiếm khóa học, kỹ năng, giảng viên..." 
            className="search-input-field"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </form>

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
            <Link to="/instructor" className="nav-text-link hide-on-mobile">
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
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
