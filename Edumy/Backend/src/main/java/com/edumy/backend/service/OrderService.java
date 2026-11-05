package com.edumy.backend.service;

import com.edumy.backend.dto.BuyNowRequest;
import com.edumy.backend.dto.OrderResponseDto;
import java.util.Map;

public interface OrderService {
    OrderResponseDto createBuyNowOrder(Integer userId, BuyNowRequest request);
    Map<String, Object> checkoutCart(Integer userId, String couponCode);
}
