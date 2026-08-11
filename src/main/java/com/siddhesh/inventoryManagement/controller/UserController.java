package com.siddhesh.inventoryManagement.controller;


import com.siddhesh.inventoryManagement.domain.dtos.UserResponse;
import com.siddhesh.inventoryManagement.services.UserService;

import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/user")
public class UserController {
    private final UserService userService;
//
//    GET    /api/v1/users
//    GET    /api/v1/users/{id}
//
//    PATCH  /api/v1/users/{id}
//
//    DELETE /api/v1/users/{id}
//    /api/v1/profile

    @GetMapping(value = "/me")
    public ResponseEntity<UserResponse> getProfile(
            Authentication authentication
    ){

        String phoneNumber = authentication.getName();

        return ResponseEntity.ok(userService.getProfile(phoneNumber));

    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value="/{id}")
    public ResponseEntity<UserResponse> getProfileByIdAdminRoute(
            @PathVariable UUID id
    ){
        return ResponseEntity.ok(
                userService.getProfileById(id)
        );

    }
}
