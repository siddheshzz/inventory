package com.siddhesh.inventoryManagement.services;

import com.siddhesh.inventoryManagement.domain.dtos.UserResponse;

import java.util.UUID;

public interface UserService {

    UserResponse getProfile(String phoneNumber);

    UserResponse getProfileById(UUID id);


}
