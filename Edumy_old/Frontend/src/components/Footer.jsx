import React from 'react';
import { Globe } from 'lucide-react';
import './Footer.css';

const Footer = () => {
  return (
    <footer className="footer">
      <div className="container">
        <div className="footer-top">
          <div className="footer-links-grid">
            <div className="footer-col">
              <a href="#">Edumy Business</a>
              <a href="#">Teach on Edumy</a>
              <a href="#">Get the app</a>
              <a href="#">About us</a>
              <a href="#">Contact us</a>
            </div>
            <div className="footer-col">
              <a href="#">Careers</a>
              <a href="#">Blog</a>
              <a href="#">Help and Support</a>
              <a href="#">Affiliate</a>
              <a href="#">Investors</a>
            </div>
            <div className="footer-col">
              <a href="#">Terms</a>
              <a href="#">Privacy policy</a>
              <a href="#">Cookie settings</a>
              <a href="#">Sitemap</a>
              <a href="#">Accessibility statement</a>
            </div>
          </div>
          <div className="footer-lang">
            <button className="lang-btn-footer">
              <Globe size={18} />
              <span>English</span>
            </button>
          </div>
        </div>
        
        <div className="footer-bottom">
          <div className="footer-logo">
            <span className="logo-text">Edumy</span>
          </div>
          <div className="footer-copyright">
            © 2026 Edumy, Inc.
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
