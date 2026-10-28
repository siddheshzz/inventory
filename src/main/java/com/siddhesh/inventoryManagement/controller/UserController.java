package com.siddhesh.inventoryManagement.controller;


import com.siddhesh.inventoryManagement.domain.dtos.AdminUserUpdateRequest;
import com.siddhesh.inventoryManagement.domain.dtos.UserResponse;
import com.siddhesh.inventoryManagement.domain.dtos.UserUpdateRequest;
import com.siddhesh.inventoryManagement.services.UserService;

import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/user")
public class UserController {
    private final UserService userService;
    private final com.siddhesh.inventoryManagement.security.CurrentUser currentUser;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UserResponse> createMember(@jakarta.validation.Valid @RequestBody AdminUserUpdateRequest request) {
        return ResponseEntity.status(201).body(userService.createMember(request));
    }
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
    @PatchMapping(value="/me")
    public ResponseEntity<UserResponse> updateProfileByIdUserRoute(
            Authentication authentication,
            @jakarta.validation.Valid @RequestBody UserUpdateRequest payload
    ){
        System.out.println("INSIDE THE CONTROLLER");

        String phoneNumber = authentication.getName();
        return ResponseEntity.ok(
                userService.updateOwnProfile(phoneNumber, payload
                )
        );
    }
//    @PreAuthorize("hasAuthority('ADMIN')")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value="/{id}")
    public ResponseEntity<UserResponse> getProfileByIdAdminRoute(
            @PathVariable UUID id
    ){
        return ResponseEntity.ok(
                userService.getProfileById(id)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping()
    public ResponseEntity<List<UserResponse>> getProfilesAdminRoute(){
        return ResponseEntity.ok(
                userService.getProfiles()
        );
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value="/{id}")
    public ResponseEntity<UserResponse> updateProfileByIdAdminRoute(
            @PathVariable UUID id,
            @jakarta.validation.Valid @RequestBody AdminUserUpdateRequest payload,
            Authentication authentication
    ){

        System.out.println("USER: " + authentication.getName());
        System.out.println("AUTHORITIES: " + authentication.getAuthorities());
        return ResponseEntity.ok(
                userService.updateProfileById(id, payload
                )
        );
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(value="/{id}")
    public ResponseEntity<Void> deleteProfileByIdAdminRoute(
            @PathVariable UUID id
    ){
        if (id.equals(currentUser.requireUser().getId())) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "You cannot remove your own account");
        }
        userService.deleteProfileById(id);
        return ResponseEntity.noContent().build();
    }


}
