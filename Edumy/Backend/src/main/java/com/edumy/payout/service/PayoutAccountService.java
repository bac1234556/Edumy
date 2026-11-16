package com.edumy.payout.service;

import com.edumy.payout.dto.PayoutAccountDto;
import com.edumy.payout.dto.UpdatePayoutAccountRequest;

public interface PayoutAccountService {
    PayoutAccountDto getAccount(Integer instructorId);
    PayoutAccountDto updateAccount(Integer instructorId, UpdatePayoutAccountRequest request);
}
