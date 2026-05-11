package com.wedding.management.domain.payment.service;

public interface TwoFactorVerificationService {
    boolean verify(String currentUserId, String inputCode);
}
