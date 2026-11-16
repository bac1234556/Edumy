package com.edumy.commerce.order.service;

import com.edumy.commerce.order.dto.CartResponseDto;

import java.util.Map;

public interface CartService {
    CartResponseDto getCart(Integer userId);
    Map<String, String> addToCart(Integer userId, Integer courseId);
    Map<String, String> removeFromCart(Integer userId, Integer courseId);
    Map<String, String> removeCartItem(Integer userId, Integer itemId);
}
