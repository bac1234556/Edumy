import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../api/axiosConfig';
import { Award, Download, Share2 } from 'lucide-react';
import './CertificateView.css';

function CertificateView() {
  const { url } = useParams();
  const [cert, setCert] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchCert = async () => {
      try {
        const response = await api.get(`/certificates/${url}`);
        setCert(response.data);
      } catch (err) {
        setError('Không tìm thấy chứng chỉ hoặc liên kết không hợp lệ.');
      } finally {
        setLoading(false);
      }
    };
    fetchCert();
  }, [url]);

  if (loading) {
    return <div className="text-center p-5">Đang tải chứng chỉ tốt nghiệp...</div>;
  }

  if (error || !cert) {
    return (
      <div className="text-center p-5">
        <h2>{error}</h2>
        <Link to="/" className="btn btn-primary mt-3">Quay lại Trang chủ</Link>
      </div>
    );
  }

  return (
    <div className="certificate-page bg-light py-5">
      <div className="container">
        <div className="row justify-content-center">
          <div className="col-lg-10">
            <div className="d-flex justify-content-end gap-2 mb-3">
              <button className="btn btn-outline-secondary" onClick={() => window.print()}>
                <Download size={18} className="me-2" /> Tải bản PDF
              </button>
              <button className="btn btn-outline-primary" onClick={() => {
                navigator.clipboard.writeText(window.location.href);
                alert("Đã sao chép liên kết chứng chỉ vào bộ nhớ tạm!");
              }}>
                <Share2 size={18} className="me-2" /> Chia sẻ chứng chỉ
              </button>
            </div>
            
            <div className="certificate-wrapper shadow-lg bg-white position-relative overflow-hidden">
              <div className="certificate-border">
                <div className="certificate-content text-center">
                  <div className="mb-4 text-warning">
                    <Award size={64} />
                  </div>
                  
                  <h1 className="certificate-title mb-1">CHỨNG NHẬN</h1>
                  <h3 className="certificate-subtitle mb-5">HOÀN THÀNH KHÓA HỌC</h3>
                  
                  <p className="text-muted mb-2">Trân trọng chứng nhận học viên</p>
                  <h2 className="student-name mb-4 text-primary">{cert.studentName}</h2>
                  
                  <p className="text-muted mb-2">Đã hoàn thành xuất sắc khóa học trực tuyến</p>
                  <h3 className="course-name mb-5">{cert.courseName}</h3>
                  
                  <div className="d-flex justify-content-between align-items-end mt-5 pt-4 px-5">
                    <div className="text-center">
                      <div className="border-bottom border-dark border-2 mb-2 px-4 pb-1">
                        {new Date(cert.issuedAt).toLocaleDateString('vi-VN')}
                      </div>
                      <small className="text-muted fw-bold">NGÀY CẤP</small>
                    </div>
                    
                    <div className="text-center">
                      <div className="border-bottom border-dark border-2 mb-2 px-4 pb-1" style={{fontFamily: 'cursive', fontSize: '1.2rem'}}>
                        {cert.instructorName}
                      </div>
                      <small className="text-muted fw-bold">GIẢNG VIÊN HƯỚNG DẪN</small>
                    </div>
                  </div>
                  
                  <div className="mt-5 text-muted small text-center w-100 position-absolute bottom-0 start-0 mb-4">
                    Verify at: EduMy.com/certificates/{cert.certificateUrl}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default CertificateView;
