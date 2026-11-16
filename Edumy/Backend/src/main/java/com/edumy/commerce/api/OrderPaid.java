package com.edumy.commerce.api;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record OrderPaid(Long orderId, String orderCode, Long studentId, long totalAmount, List<Long> courseIds,
                        Instant paidAt) {

    public OrderPaid {
        Objects.requireNonNull(orderId, "orderId");
        Objects.requireNonNull(orderCode, "orderCode");
        Objects.requireNonNull(studentId, "studentId");
        courseIds = List.copyOf(courseIds);
    }
}
