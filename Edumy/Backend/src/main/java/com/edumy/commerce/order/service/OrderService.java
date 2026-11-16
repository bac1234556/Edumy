package com.edumy.commerce.order.service;

import com.edumy.commerce.order.dto.BuyNowRequest;
import com.edumy.commerce.order.dto.OrderResponseDto;

import java.util.Map;

public interface OrderService {
    OrderResponseDto createBuyNowOrder(Integer userId, BuyNowRequest request);
    Map<String, Object> checkoutCart(Integer userId, String couponCode);
}
