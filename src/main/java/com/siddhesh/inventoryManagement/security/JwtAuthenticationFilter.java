package com.siddhesh.inventoryManagement.security;

import com.siddhesh.inventoryManagement.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request,response);
            return;

        }

        String token = authHeader.substring(7);

        String phoneNumber =
                jwtService.extractSubject(token);
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(phoneNumber);

        log.info("========== AUTH DEBUG ==========");
        log.info("Request: {} {}", request.getMethod(), request.getRequestURI());
        log.info("Phone: {}", phoneNumber);
        log.info("User: {}", userDetails.getUsername());
        log.info("Authorities: {}", userDetails.getAuthorities());
        log.info("================================");

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        filterChain.doFilter(request,response);
    }



}
