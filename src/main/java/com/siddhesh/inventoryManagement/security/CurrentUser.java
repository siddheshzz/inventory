package com.siddhesh.inventoryManagement.security;

import com.siddhesh.inventoryManagement.domain.entities.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves the caller from the JWT principal instead of trusting
 * client-supplied user IDs. Every authenticated request carries a
 * server-verified identity (see JwtAuthenticationFilter); controllers
 * pass the resolved User down to services, which enforce ownership.
 */
@Component
public class CurrentUser {

    public User requireUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails details)) {
            throw new RuntimeException("Not authenticated");
        }
        return details.getUser();
    }
}
