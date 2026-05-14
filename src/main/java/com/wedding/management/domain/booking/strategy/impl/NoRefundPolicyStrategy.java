package com.wedding.management.domain.booking.strategy.impl;

import com.wedding.management.domain.booking.strategy.RefundPolicyStrategy;
import org.springframework.stereotype.Component;

@Component
public class NoRefundPolicyStrategy implements RefundPolicyStrategy {

    @Override
    public boolean isApplicable(long daysBeforeWedding) {
        return daysBeforeWedding < 15;
    }

    @Override
    public double calculateRefund(double paidAmount) {
        return 0.0;
    }

    @Override
    public String getPolicyName() {
        return "Không hoàn tiền khi hủy dưới 14 ngày trước ngày cưới";
    }
}