package com.edumy.backend.service;

import com.edumy.backend.dto.CartResponseDto;
import java.util.Map;

public interface CartService {
    CartResponseDto getCart(Integer userId);
    Map<String, String> addToCart(Integer userId, Integer courseId);
    Map<String, String> removeFromCart(Integer userId, Integer courseId);
    Map<String, String> removeCartItem(Integer userId, Integer itemId);
}
