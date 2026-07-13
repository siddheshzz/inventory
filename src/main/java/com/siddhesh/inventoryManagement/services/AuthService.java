package com.siddhesh.inventoryManagement.services;

import org.springframework.security.core.userdetails.UserDetails;

public interface AuthService {

    UserDetails authenticate(int phone_number, int otp);
    String generateToken(UserDetails userDetails);
    UserDetails validateToken(String token);
}
