package com.wedding.management.domain.booking.strategy;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class RefundPolicyResolver {

    private final List<RefundPolicyStrategy> strategies;

    public RefundPolicyResolver(List<RefundPolicyStrategy> strategies) {
        this.strategies = strategies;
    }

    public RefundPolicyStrategy resolve(long daysBeforeWedding) {
        return strategies.stream()
                .filter(strategy -> strategy.isApplicable(daysBeforeWedding))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy chính sách hoàn tiền phù hợp"
                ));
    }
}