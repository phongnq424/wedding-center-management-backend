package com.wedding.management.common.mail;

import com.wedding.management.common.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class BrevoEmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    public BrevoEmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendOtpEmail(String to, String fullName, String otpCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Wedding Center - Mã xác thực đăng nhập");

            String displayName = fullName == null || fullName.isBlank()
                    ? "người dùng"
                    : fullName;

            message.setText("""
                    Xin chào %s,

                    Mã xác thực đăng nhập của bạn là:

                    %s

                    Mã này có hiệu lực trong 5 phút.
                    Vui lòng không chia sẻ mã này cho bất kỳ ai.

                    Wedding Center Management
                    """.formatted(displayName, otpCode));

            mailSender.send(message);
        } catch (MailException ex) {
            throw new BadRequestException("MSG57: Không thể gửi mã xác thực qua email");
        }
    }
}