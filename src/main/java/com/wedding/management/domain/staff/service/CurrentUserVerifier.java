package com.wedding.management.domain.staff.service;

public interface CurrentUserVerifier {
    boolean verifyCurrentPassword(String currentUserId, String currentPassword);
}