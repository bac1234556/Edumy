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
    // Optimistic local update so UI immediately transitions
    if (cart && cart.items) {
      const updatedItems = cart.items.filter(item => item.courseId !== courseId);
      const updatedCount = updatedItems.length;
      const updatedTotal = updatedItems.reduce((acc, item) => acc + (item.price || 0), 0);
      setCart({
        ...cart,
        items: updatedItems,
        totalItems: updatedCount,
        totalPrice: updatedTotal
      });
      setCartCount(updatedCount);
    }

    try {
      const res = await api.delete(`/cart/remove/${courseId}`);
      await fetchCart();
      return res.data;
    } catch (err) {
      // Revert if API failed
      fetchCart();
      throw err;
    }
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
