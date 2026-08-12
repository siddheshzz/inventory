package com.siddhesh.inventoryManagement.services.impl;

import com.siddhesh.inventoryManagement.domain.dtos.AdminUserUpdateRequest;
import com.siddhesh.inventoryManagement.domain.dtos.UserResponse;
import com.siddhesh.inventoryManagement.domain.dtos.UserUpdateRequest;
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

    @Override
    public UserResponse updateProfileById(UUID id,AdminUserUpdateRequest payload) {
        System.out.println("INSIDE THE service");

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User nnot found"));

        user.setName(payload.getName());
        user.setEmail(payload.getEmail());
        user.setRole(payload.getRole());

        userRepository.save(user);

        UserResponse u1 = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getName(),
                user.getRole().toString()
        );

        System.out.println("END OF SERVICE - "+u1);
        return u1;


    }

    @Override
    public UserResponse updateProfileByIdUser(String phoneNumber,UserUpdateRequest payload) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("User nnot found"));

//        user.setEmail(payload.getEmail());
//        user.setName(payload.getName());
//        user
        System.out.println("PHONE: " + phoneNumber);
        System.out.println("NAME: " + payload.getName());
        System.out.println("EMAIL: " + payload.getEmail());
        if (payload.getEmail() != null) {
            user.setEmail(payload.getEmail());
        }

        if (payload.getName() != null) {
            user.setName(payload.getName());
        }

        System.out.println("BEFORE SAVE: " + user.getName());

        userRepository.save(user);

        System.out.println("AFTER SAVE: " + user.getName());

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getName(),
                user.getRole().toString()
        );

    }

    @Override
    public void deleteProfileById(UUID id) {
        userRepository.deleteById(id);

    }
}
