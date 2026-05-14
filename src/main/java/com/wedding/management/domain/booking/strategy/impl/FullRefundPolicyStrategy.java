package com.wedding.management.domain.booking.strategy.impl;

import com.wedding.management.domain.booking.strategy.RefundPolicyStrategy;
import org.springframework.stereotype.Component;

@Component
public class FullRefundPolicyStrategy implements RefundPolicyStrategy {

    @Override
    public boolean isApplicable(long daysBeforeWedding) {
        return daysBeforeWedding >= 30;
    }

    @Override
    public double calculateRefund(double paidAmount) {
        return paidAmount;
    }

    @Override
    public String getPolicyName() {
        return "Hoàn 100% số tiền đã thanh toán khi hủy trước ngày cưới từ 30 ngày trở lên";
    }
}