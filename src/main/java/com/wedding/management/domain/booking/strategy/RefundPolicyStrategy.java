package com.wedding.management.domain.booking.strategy;

public interface RefundPolicyStrategy {

    boolean isApplicable(long daysBeforeWedding);

    double calculateRefund(double paidAmount);

    String getPolicyName();
}