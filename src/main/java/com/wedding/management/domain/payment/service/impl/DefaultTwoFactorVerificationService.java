package com.wedding.management.domain.payment.service.impl;

import com.wedding.management.domain.payment.service.TwoFactorVerificationService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DefaultTwoFactorVerificationService implements TwoFactorVerificationService {
    public boolean verify(String currentUserId, String inputCode) {
        return StringUtils.hasText(currentUserId) && StringUtils.hasText(inputCode) && inputCode.trim().length() >= 4;
    }
}
