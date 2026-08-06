package com.siddhesh.inventoryManagement.services.impl;

import com.siddhesh.inventoryManagement.domain.dtos.AuthResponse;
import com.siddhesh.inventoryManagement.domain.dtos.OtpVerificationDto;
import com.siddhesh.inventoryManagement.domain.entities.Role;
import com.siddhesh.inventoryManagement.domain.entities.User;
import com.siddhesh.inventoryManagement.repositories.UserRepository;
import com.siddhesh.inventoryManagement.security.CustomUserDetails;
import com.siddhesh.inventoryManagement.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtServiceImpl jwtService;

    @Override
    public void sendOtp(String phoneNumber) {
        // redis db
        //generate a otp and send it to user and store it in redis
        String otp = "1234";

    }

    @Override
    public AuthResponse verifyOtp(OtpVerificationDto request) throws ChangeSetPersister.NotFoundException {
        //find he number in redis and corresponding otp
        //if request.otp == otp

//        User user =
//                userRepository
//                        .findByPhoneNumber(request.getPhoneNumber())
//                        .orElseThrow(ChangeSetPersister.NotFoundException::new);
//
//        User user1 =
//                userRepository
//                        .findByPhoneNumber(request.getPhoneNumber())
//                        .orElseGet(() -> {
//
//                            User newUser = User.builder()
//                                    .phoneNumber(request.getPhoneNumber())
//                                    .name("New User")
//                                    .role(Role.USER)
//                                    .build();
//
//                            return userRepository.save(newUser);
//
//                        });
        User user =
                userRepository
                        .findByPhoneNumber(request.getPhoneNumber())
                        .orElseThrow();

//        UserDetails details =
//                new CustomUserDetails(user);

        String token =
                jwtService.generateToken(user);
//        return new AuthResponse(
//            generateToken(details),
//            84000
//        );
        return AuthResponse.builder()
                .token(token)
                .expiresIn(86400)
                .build();


    }

//    @Override
//    public UserDetails authenticate(int phone_number, int otp) {
//        return null;
//    }


//    public String generateToken(UserDetails details) {
//
//        String token =
//                jwtService.generateToken(details);
//
//    }
//
//    @Override
//    public UserDetails validateToken(String token) {
//        return null;
//    }
}
