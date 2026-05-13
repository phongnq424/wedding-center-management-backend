package com.wedding.management.common.mail;

public interface EmailService {
    void sendOtpEmail(String to, String fullName, String otpCode);
}