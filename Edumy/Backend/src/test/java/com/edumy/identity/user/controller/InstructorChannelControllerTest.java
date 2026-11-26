package com.edumy.identity.user.controller;

import com.edumy.payout.model.EarningEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InstructorChannelControllerTest {

    @Test
    @DisplayName("computeGrossSales returns 0 for null or empty entries")
    void testComputeGrossSalesEmpty() {
        assertThat(InstructorChannelController.computeGrossSales(null)).isZero();
        assertThat(InstructorChannelController.computeGrossSales(Collections.emptyList())).isZero();
    }

    @Test
    @DisplayName("computeGrossSales sums pre-30%-fee sales using unitPrice and shareBps")
    void testComputeGrossSalesFromUnitPriceAndShareBps() {
        EarningEntry mainEntry = EarningEntry.builder()
                .unitPrice(1_000_000L)
                .platformFeeBps(3000)
                .shareBps(10000)
                .amount(700_000L)
                .build();

        EarningEntry coEntry = EarningEntry.builder()
                .unitPrice(2_000_000L)
                .platformFeeBps(3000)
                .shareBps(3000) // 30% co-instructor share -> 600,000 gross before 30% fee
                .amount(420_000L)
                .build();

        long gross = InstructorChannelController.computeGrossSales(List.of(mainEntry, coEntry));
        assertThat(gross).isEqualTo(1_600_000L);
    }

    @Test
    @DisplayName("computeGrossSales falls back to amount / (1 - platformFee) when unitPrice is missing")
    void testComputeGrossSalesFallbackWhenUnitPriceMissing() {
        EarningEntry legacyEntry = EarningEntry.builder()
                .unitPrice(null)
                .platformFeeBps(3000)
                .shareBps(null)
                .amount(700_000L)
                .build();

        long gross = InstructorChannelController.computeGrossSales(List.of(legacyEntry));
        assertThat(gross).isEqualTo(1_000_000L);
    }
}
