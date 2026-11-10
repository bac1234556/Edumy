package com.edumy.modules.order.service;

import com.edumy.modules.order.dto.BuyNowRequest;
import com.edumy.modules.order.dto.OrderResponseDto;

import java.util.Map;

public interface OrderService {
    OrderResponseDto createBuyNowOrder(Integer userId, BuyNowRequest request);
    Map<String, Object> checkoutCart(Integer userId, String couponCode);
}
