package com.edumy.payout.api;

import java.util.List;

public interface EarningApi {
    record OrderLine(Integer orderItemId, Integer courseId, String courseTitle, long unitPrice) {}
    record PaidOrderCommand(Integer orderId, String orderCode, List<OrderLine> lines) {}

    void recordForPaidOrder(PaidOrderCommand command);
}
