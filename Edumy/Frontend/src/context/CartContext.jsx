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

  const removeFromCart = async (courseId, itemId) => {
    // Optimistic local update so UI immediately transitions smoothly
    if (cart && cart.items) {
      const updatedItems = cart.items.filter(item => {
        if (itemId && item.id === itemId) return false;
        if (courseId && item.courseId === courseId) return false;
        return true;
      });
      const updatedCount = updatedItems.length;
      const updatedTotal = updatedItems.reduce((acc, item) => acc + (item.course?.price || item.price || 0), 0);
      const payableItems = updatedItems.filter(item => !item.isEnrolled && !item.isPaid);
      const payableTotal = payableItems.reduce((acc, item) => acc + (item.course?.price || item.price || 0), 0);
      const allPaid = updatedItems.length > 0 && payableItems.length === 0;

      setCart({
        ...cart,
        items: updatedItems,
        totalItems: updatedCount,
        totalPrice: updatedTotal,
        totalAmount: updatedTotal,
        payableTotal: payableTotal,
        payableItemsCount: payableItems.length,
        allPaid: allPaid
      });
      setCartCount(updatedCount);
    }

    try {
      // Prioritize deleting by cartItemId if available, otherwise courseId
      const targetEndpoint = itemId ? `/cart/items/${itemId}` : `/cart/remove/${courseId}`;
      const res = await api.delete(targetEndpoint);
      await fetchCart();
      return res.data;
    } catch (err) {
      // Fallback try by courseId if itemId failed
      if (itemId && courseId) {
        try {
          const fallbackRes = await api.delete(`/cart/remove/${courseId}`);
          await fetchCart();
          return fallbackRes.data;
        } catch (fallbackErr) {
          await fetchCart();
          throw fallbackErr;
        }
      }
      // Revert if API failed
      await fetchCart();
      throw err;
    }
  };

  const updateCartCount = (count) => {
    setCartCount(count);
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
        setCartCount,
        updateCartCount,
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
