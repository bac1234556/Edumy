import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../api/axiosConfig';
import { Award, Download, Share2, Printer, ShieldCheck, CheckCircle2, ArrowLeft } from 'lucide-react';
import EmptyState from '../components/EmptyState';
import { SkeletonBox } from '../components/Skeleton';
import './CertificateView.css';

const BACKEND_URL = (import.meta.env.VITE_API_URL || '/api').replace(/\/api\/?$/, '');

function CertificateView() {
  const { url, code } = useParams();
  const certCode = code || url;
  const [cert, setCert] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    const fetchCert = async () => {
      try {
        setLoading(true);
        setError('');
        const response = await api.get(`/certificates/verify/${encodeURIComponent(certCode)}`);
        if (response.data && response.data.valid === false) {
          setError(response.data.message || 'Không tìm thấy chứng chỉ hoặc liên kết không hợp lệ.');
        } else {
          setCert(response.data);
        }
      } catch (err) {
        setError(
          err.response?.data?.message ||
            'Không tìm thấy chứng chỉ hoặc liên kết không hợp lệ.'
        );
      } finally {
        setLoading(false);
      }
    };
    if (certCode) {
      fetchCert();
    } else {
      setError('Mã chứng chỉ không hợp lệ.');
      setLoading(false);
    }
  }, [certCode]);

  if (loading) {
    return (
      <div className="container py-5 my-5" style={{ maxWidth: '880px' }}>
        <div className="card shadow-sm border p-5 text-center">
          <div className="mx-auto mb-3" style={{ width: '64px' }}>
            <SkeletonBox width="64px" height="64px" borderRadius="50%" />
          </div>
          <div className="mx-auto mb-2" style={{ width: '280px' }}>
            <SkeletonBox width="280px" height="32px" />
          </div>
          <div className="mx-auto mb-4" style={{ width: '180px' }}>
            <SkeletonBox width="180px" height="20px" />
          </div>
          <div className="mx-auto mb-3" style={{ width: '320px' }}>
            <SkeletonBox width="320px" height="26px" />
          </div>
          <div className="mx-auto" style={{ width: '400px' }}>
            <SkeletonBox width="400px" height="22px" />
          </div>
        </div>
      </div>
    );
  }

  if (error || !cert) {
    return (
      <div className="container py-5 my-5">
        <EmptyState
          icon={Award}
          title="Không tìm thấy chứng chỉ"
          description={error || 'Liên kết chứng chỉ không tồn tại hoặc đã bị thu hồi.'}
          actionText="Quay lại Trang chủ"
          actionLink="/"
        />
      </div>
    );
  }

  const displayCode = cert.certificateUrl || certCode;
  const studentName = cert.studentName || 'Học viên Edumy';
  const courseName = cert.courseTitle || cert.courseName || 'Khóa học trực tuyến Edumy';
  const instructorName = cert.instructorName || 'Ban Giảng huấn Edumy';
  const issuedDate = cert.issuedAt
    ? new Date(cert.issuedAt).toLocaleDateString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
      })
    : '—';
  const verifyUrl = `${window.location.origin}/certificates/verify/${encodeURIComponent(displayCode)}`;
  const pdfHref = `${BACKEND_URL}/api/certificates/${encodeURIComponent(displayCode)}/pdf`;

  const handleCopyLink = async () => {
    try {
      await navigator.clipboard.writeText(verifyUrl);
      setCopied(true);
      setTimeout(() => setCopied(false), 2500);
    } catch {
      alert('Đã sao chép liên kết chứng chỉ: ' + verifyUrl);
    }
  };

  return (
    <div className="certificate-page py-5">
      <div className="container">
        <div className="row justify-content-center">
          <div className="col-lg-10 col-xl-9">
            {/* Top Toolbar (hidden on print) */}
            <div className="cert-toolbar no-print">
              <div className="cert-toolbar-left">
                <Link to="/my-courses" className="btn btn-sm btn-outline-secondary d-inline-flex align-items-center gap-1">
                  <ArrowLeft size={15} /> Khóa học của tôi
                </Link>
                <span className="cert-verified-pill">
                  <ShieldCheck size={15} /> Chứng chỉ hợp lệ — Đã xác thực bởi Edumy
                </span>
              </div>

              <div className="cert-toolbar-actions">
                <a
                  href={pdfHref}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="btn btn-primary btn-sm d-inline-flex align-items-center gap-2 fw-semibold"
                >
                  <Download size={16} /> Tải PDF chính thức
                </a>
                <button
                  type="button"
                  className="btn btn-outline-secondary btn-sm d-inline-flex align-items-center gap-2"
                  onClick={() => window.print()}
                >
                  <Printer size={16} /> In chứng chỉ
                </button>
                <button
                  type="button"
                  className="btn btn-outline-primary btn-sm d-inline-flex align-items-center gap-2"
                  onClick={handleCopyLink}
                >
                  {copied ? <CheckCircle2 size={16} /> : <Share2 size={16} />}
                  {copied ? 'Đã sao chép link!' : 'Chia sẻ chứng chỉ'}
                </button>
              </div>
            </div>

            {/* Standard Certificate Paper */}
            <div className="certificate-wrapper shadow-lg position-relative overflow-hidden">
              <div className="certificate-border">
                <div className="certificate-inner-border">
                  <span className="cert-corner cert-corner-tl" aria-hidden="true" />
                  <span className="cert-corner cert-corner-tr" aria-hidden="true" />
                  <span className="cert-corner cert-corner-bl" aria-hidden="true" />
                  <span className="cert-corner cert-corner-br" aria-hidden="true" />

                  <div className="certificate-content text-center">
                    {/* Brand Emblem */}
                    <div className="cert-emblem-wrap mx-auto mb-2">
                      <Award size={36} />
                    </div>
                    <div className="cert-brand-name">EDUMY E-LEARNING PLATFORM</div>

                    {/* Titles */}
                    <h1 className="certificate-title mt-3 mb-1">CHỨNG CHỈ</h1>
                    <h3 className="certificate-subtitle mb-1">HOÀN THÀNH KHÓA HỌC</h3>
                    <div className="certificate-en-subtitle">CERTIFICATE OF COMPLETION</div>

                    <div className="cert-gold-divider mx-auto" />

                    {/* Recipient */}
                    <p className="cert-intro-text mb-2">Trân trọng chứng nhận học viên</p>
                    <h2 className="student-name mb-4">{studentName}</h2>

                    {/* Course */}
                    <p className="cert-intro-text mb-2">
                      Đã hoàn thành xuất sắc chương trình đào tạo trực tuyến
                    </p>
                    <h3 className="course-name mb-5">{courseName}</h3>

                    {/* Signatures & Official Seal */}
                    <div className="cert-signatures-grid">
                      <div className="cert-sign-col">
                        <div className="cert-sign-value cert-date-val">{issuedDate}</div>
                        <div className="cert-sign-line" />
                        <small className="cert-sign-label">NGÀY CẤP</small>
                      </div>

                      <div className="cert-seal-col">
                        <div className="cert-seal-outer">
                          <div className="cert-seal-inner">
                            <CheckCircle2 size={22} />
                            <span className="cert-seal-brand">EDUMY</span>
                            <span className="cert-seal-sub">VERIFIED</span>
                          </div>
                        </div>
                      </div>

                      <div className="cert-sign-col">
                        <div className="cert-sign-value cert-instructor-sign">
                          {instructorName}
                        </div>
                        <div className="cert-sign-line" />
                        <small className="cert-sign-label">GIẢNG VIÊN HƯỚNG DẪN</small>
                      </div>
                    </div>

                    {/* Footer Metadata */}
                    <div className="cert-footer-meta">
                      <div className="cert-code-line">
                        Mã số chứng chỉ: <strong>{displayCode}</strong>
                      </div>
                      <div className="cert-verify-line">
                        Tra cứu xác thực tại: <span>{verifyUrl}</span>
                      </div>
                    </div>
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
