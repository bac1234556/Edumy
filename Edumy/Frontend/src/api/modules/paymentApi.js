import api from '../axiosConfig';

export const paymentApi = {
  getVietQrByOrderId: (orderId) => api.get(`/v1/payments/vietqr/${orderId}`),
  createVietQr: (orderId) => api.post('/v1/payments/create-qr', { orderId }),
  getOrderStatus: (orderId) => api.get(`/v1/orders/${orderId}/status`),
  createPaymentUrl: (payload) => api.post('/v1/payments/create-url', payload),
};

export default paymentApi;
