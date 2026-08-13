package com.siddhesh.inventoryManagement.services;

import com.siddhesh.inventoryManagement.domain.dtos.AdminUserUpdateRequest;
import com.siddhesh.inventoryManagement.domain.dtos.UserResponse;
import com.siddhesh.inventoryManagement.domain.dtos.UserUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse getProfile(String phoneNumber);

    UserResponse getProfileById(UUID id);
    UserResponse updateProfileById(UUID id,AdminUserUpdateRequest payload);

    UserResponse updateProfileByIdUser(String phone, UserUpdateRequest payload);

    void deleteProfileById(UUID id);

    List<UserResponse> getProfiles();


}
