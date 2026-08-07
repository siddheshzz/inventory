package com.siddhesh.inventoryManagement.services;

import com.siddhesh.inventoryManagement.domain.entities.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    String generateToken(User user);
    String extractSubject(String token);
    boolean isTokenValid(String token);
}
