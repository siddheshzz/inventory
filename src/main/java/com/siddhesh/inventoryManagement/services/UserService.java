package com.siddhesh.inventoryManagement.services;

import com.siddhesh.inventoryManagement.domain.dtos.UserResponse;

public interface UserService {

    UserResponse getProfile(String phoneNumber);


}
