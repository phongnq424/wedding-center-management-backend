package com.wedding.management.domain.booking.strategy.impl;

import com.wedding.management.domain.booking.strategy.RefundPolicyStrategy;
import org.springframework.stereotype.Component;

@Component
public class HalfRefundPolicyStrategy implements RefundPolicyStrategy {

    @Override
    public boolean isApplicable(long daysBeforeWedding) {
        return daysBeforeWedding >= 15 && daysBeforeWedding < 30;
    }

    @Override
    public double calculateRefund(double paidAmount) {
        return paidAmount * 0.5;
    }

    @Override
    public String getPolicyName() {
        return "Hoàn 50% số tiền đã thanh toán khi hủy trước ngày cưới từ 14 đến dưới 30 ngày";
    }
}