package com.siddhesh.inventoryManagement.services;

import com.siddhesh.inventoryManagement.domain.dtos.AuthResponse;
import com.siddhesh.inventoryManagement.domain.dtos.OtpVerificationDto;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthService {

    void sendOtp(String phoneNumber);
    AuthResponse verifyOtp(OtpVerificationDto request) throws ChangeSetPersister.NotFoundException;

}
