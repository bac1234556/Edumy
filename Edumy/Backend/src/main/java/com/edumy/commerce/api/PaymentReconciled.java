package com.edumy.commerce.api;

import java.util.List;
import java.util.Objects;

public record PaymentReconciled(Long paymentId, String orderCode, Long studentId, String action,
                                List<Long> grantedCourseIds) {

    public PaymentReconciled {
        Objects.requireNonNull(paymentId, "paymentId");
        Objects.requireNonNull(orderCode, "orderCode");
        Objects.requireNonNull(studentId, "studentId");
        Objects.requireNonNull(action, "action");
        grantedCourseIds = List.copyOf(grantedCourseIds);
    }

    public boolean activated() {
        return "activated".equals(action);
    }
}
