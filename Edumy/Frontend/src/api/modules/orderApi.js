import api from '../axiosConfig';

export const orderApi = {
  getCart: () => api.get('/cart'),
  addToCart: (courseId) => api.post(`/cart/add/${courseId}`),
  removeCartItemById: (itemId) => api.delete(`/cart/items/${itemId}`),
  removeCartItemByCourse: (courseId) => api.delete(`/cart/remove/${courseId}`),
  validateCoupon: (code) => api.post('/coupons/validate', { code }),
  checkout: (couponCode = null) => api.post('/orders/checkout', { couponCode }),
};

export default orderApi;
