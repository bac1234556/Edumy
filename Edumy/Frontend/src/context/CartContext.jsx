import React, { createContext, useState, useEffect, useContext, useCallback } from 'react';
import api from '../api/axiosConfig';
import { AuthContext } from './AuthContext';

export const CartContext = createContext();

export const CartProvider = ({ children }) => {
  const { user } = useContext(AuthContext);
  const [cart, setCart] = useState(null);
  const [cartCount, setCartCount] = useState(0);
  const [loadingCart, setLoadingCart] = useState(false);

  const fetchCart = useCallback(async () => {
    if (!user) {
      setCart(null);
      setCartCount(0);
      return;
    }

    try {
      setLoadingCart(true);
      const res = await api.get('/cart');
      setCart(res.data);
      setCartCount(res.data?.totalItems || res.data?.items?.length || 0);
    } catch (err) {
      console.warn('Failed to fetch cart:', err);
      setCart(null);
      setCartCount(0);
    } finally {
      setLoadingCart(false);
    }
  }, [user]);

  useEffect(() => {
    fetchCart();
  }, [fetchCart]);

  const addToCart = async (courseId) => {
    if (!user) {
      throw new Error('AUTH_REQUIRED');
    }
    const res = await api.post(`/cart/add/${courseId}`);
    await fetchCart();
    return res.data;
  };

  const removeFromCart = async (courseId) => {
    const res = await api.delete(`/cart/remove/${courseId}`);
    await fetchCart();
    return res.data;
  };

  const clearCartState = () => {
    setCart(null);
    setCartCount(0);
  };

  return (
    <CartContext.Provider
      value={{
        cart,
        cartCount,
        loadingCart,
        fetchCart,
        addToCart,
        removeFromCart,
        clearCartState
      }}
    >
      {children}
    </CartContext.Provider>
  );
};

export default CartContext;
