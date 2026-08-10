package com.siddhesh.inventoryManagement.controller;


import com.siddhesh.inventoryManagement.domain.dtos.UserResponse;
import com.siddhesh.inventoryManagement.services.UserService;

import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/user")
public class UserController {
    private final UserService userService;

    @GetMapping(name = "/me")
    public ResponseEntity<UserResponse> getProfile(
            @AuthenticationPrincipal Jwts jwt
    ){
        return ResponseEntity.ok(userService.getProfile(jwt..toString()));

    }
}
