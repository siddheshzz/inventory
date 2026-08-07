package com.siddhesh.inventoryManagement.services;

public interface OtpService {

    String generateOtp();

    void storeOtp(String phoneNumber, String otp);

    boolean verifyOtp(String phoneNumber, String otp);
}
