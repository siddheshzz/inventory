package com.siddhesh.inventoryManagement.services.impl;

import com.siddhesh.inventoryManagement.domain.dtos.UserResponse;
import com.siddhesh.inventoryManagement.domain.entities.User;
import com.siddhesh.inventoryManagement.repositories.UserRepository;
import com.siddhesh.inventoryManagement.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;


    @Override
    public UserResponse getProfile(String phoneNumber) {

        try{
            User user = userRepository.findByPhoneNumber(phoneNumber)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return new UserResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    user.getName(),
                    user.getRole().toString()
            );


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public UserResponse getProfileById(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("User not found"));

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getName(),
                user.getRole().toString()
        );

    }
}
