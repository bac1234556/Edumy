import React from 'react';
import PaymentQR from './PaymentQR';

// Adapter ensuring any existing references to MockPaymentGateway route cleanly to the real VietQR payment screen
function MockPaymentGateway() {
  return <PaymentQR />;
}

export default MockPaymentGateway;
