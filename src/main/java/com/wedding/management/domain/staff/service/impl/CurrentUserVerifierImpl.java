package com.wedding.management.domain.staff.service.impl;

import com.wedding.management.domain.staff.service.CurrentUserVerifier;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserVerifierImpl implements CurrentUserVerifier {

    @Override
    public boolean verifyCurrentPassword(String currentUserId, String currentPassword) {
        return currentPassword != null && !currentPassword.isBlank();
    }
}