package com.siddhesh.inventoryManagement.services.impl;

import com.siddhesh.inventoryManagement.domain.dtos.AdminUserUpdateRequest;
import com.siddhesh.inventoryManagement.domain.dtos.UserResponse;
import com.siddhesh.inventoryManagement.domain.dtos.UserUpdateRequest;
import com.siddhesh.inventoryManagement.domain.entities.User;
import com.siddhesh.inventoryManagement.repositories.UserRepository;
import com.siddhesh.inventoryManagement.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final com.siddhesh.inventoryManagement.repositories.OrderRepository orderRepository;
    private final com.siddhesh.inventoryManagement.repositories.StockTransactionRepository stockRepository;
    private final com.siddhesh.inventoryManagement.repositories.AddressRepository addressRepository;
    private final com.siddhesh.inventoryManagement.security.CurrentUser currentUser;

    @Transactional
    @Override
    public UserResponse createMember(AdminUserUpdateRequest payload) {
        validateMember(payload);
        if (userRepository.findByPhoneNumber(payload.getPhoneNumber().trim()).isPresent()) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "This phone number already has an account. Edit the existing member instead.");
        }
        User user = User.builder().name(payload.getName().trim()).phoneNumber(payload.getPhoneNumber().trim())
                .email(normalizeEmail(payload.getEmail())).role(payload.getRole()).build();
        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "This email is already in use");
        }
        userRepository.save(user);
        return getProfileById(user.getId());
    }

    private static String normalizeEmail(String email) { return email == null || email.isBlank() ? null : email.trim(); }
    private static void validateMember(AdminUserUpdateRequest p) {
        if (p.getName() == null || p.getName().isBlank() || p.getRole() == null || p.getPhoneNumber() == null
                || !p.getPhoneNumber().matches("\\+?[0-9]{10,15}")) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Provide a name, role and valid phone number");
        }
    }


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
    @Transactional
    @Override
    public UserResponse updateProfileById(UUID id,AdminUserUpdateRequest payload) {
        validateMember(payload);
        if (id.equals(currentUser.requireUser().getId()) && payload.getRole() != com.siddhesh.inventoryManagement.domain.entities.Role.ADMIN) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "You cannot remove your own admin access");
        }
        System.out.println("INSIDE THE service");

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User nnot found"));

        user.setName(payload.getName());
        user.setEmail(normalizeEmail(payload.getEmail()));
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
    @Transactional
    @Override
    public UserResponse updateOwnProfile(String phoneNumber,UserUpdateRequest payload) {
        if (payload.getName() != null && payload.getName().isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Your name cannot be blank");
        }
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("User nnot found"));

//        user.setEmail(payload.getEmail());
//        user.setName(payload.getName());
//        user
        System.out.println("PHONE: " + phoneNumber);
        System.out.println("NAME: " + payload.getName());
        System.out.println("EMAIL: " + payload.getEmail());
        if (payload.getEmail() != null) {
            user.setEmail(normalizeEmail(payload.getEmail()));
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
                user.getRole().name()
        );

    }

    @Override
    @Transactional
    public void deleteProfileById(UUID id) {
        if (orderRepository.existsByUser_Id(id) || stockRepository.existsByCreatedBy_Id(id)) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "This account has order or stock history and cannot be deleted. Change its role instead.");
        }
        addressRepository.deleteAll(addressRepository.findByUser_IdOrderByIsDefaultDescCreatedAtDesc(id));
        userRepository.deleteById(id);

    }


    @Override
    public List<UserResponse> getProfiles(){

        List<User> users = userRepository.findAll();

        List<UserResponse> res = new ArrayList<>();;


        for(User user : users){
            res.add(new UserResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    user.getName(),
                    user.getRole().toString()
            ));

        }

        return res;





    }
}
