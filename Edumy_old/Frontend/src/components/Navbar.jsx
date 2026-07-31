import React, { useContext } from 'react';
import { Link } from 'react-router-dom';
import { Search, ShoppingCart, Bell, Globe, Heart } from 'lucide-react';
import { AuthContext } from '../context/AuthContext';
import './Navbar.css';

const Navbar = () => {
  const { user, logout } = useContext(AuthContext);

  return (
    <nav className="navbar">
      <div className="navbar-container container">
        <Link to="/" className="navbar-logo">
          <span className="logo-text">Edumy</span>
        </Link>

        <div className="navbar-categories">
          <span>Categories</span>
        </div>

        <div className="navbar-search">
          <button className="search-btn">
            <Search size={18} color="var(--text-muted)" />
          </button>
          <input 
            type="text" 
            placeholder="Search for anything" 
            className="search-input"
          />
        </div>

        <div className="navbar-right">
          <Link to="/instructor" className="navbar-link hidden-mobile">
            Teach on Edumy
          </Link>
          
          <Link to="/cart" className="navbar-icon-btn text-dark">
            <ShoppingCart size={20} />
          </Link>
          
          {user && (
            <Link to="/wishlist" className="navbar-icon-btn text-dark">
              <Heart size={20} />
            </Link>
          )}

          <div className="navbar-icon-btn">
            <Bell size={20} />
          </div>
          
          <div className="navbar-actions">
            {user ? (
              <div className="d-flex align-items-center gap-3">
                <Link to="/profile" className="text-decoration-none text-dark fw-medium" title="Profile">
                  Chào, {user.email?.split('@')[0]}
                </Link>
                <button onClick={logout} className="btn-edumy-outline login-btn">Đăng xuất</button>
              </div>
            ) : (
              <>
                <Link to="/login" className="btn-edumy-outline login-btn">Log in</Link>
                <Link to="/register" className="btn-edumy signup-btn">Sign up</Link>
              </>
            )}
            <div className="navbar-icon-btn lang-btn hidden-mobile">
              <Globe size={20} />
            </div>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
