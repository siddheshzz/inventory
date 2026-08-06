package com.siddhesh.inventoryManagement.controller;


import com.siddhesh.inventoryManagement.domain.dtos.AuthRequest;
import com.siddhesh.inventoryManagement.domain.dtos.AuthResponse;
import com.siddhesh.inventoryManagement.domain.dtos.OtpRequestDto;
import com.siddhesh.inventoryManagement.domain.dtos.OtpVerificationDto;
import com.siddhesh.inventoryManagement.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send-otp")
    public ResponseEntity<Void> sendotp(@RequestBody OtpRequestDto otpRequest){

        System.out.println("SEND-OTP");

        authService.sendOtp(otpRequest.getPhoneNumber());

//        AuthResponse authResponse = AuthResponse.builder().build();

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyotp(@RequestBody OtpVerificationDto otpVerificationDto) throws ChangeSetPersister.NotFoundException {

        System.out.println("VERIFY-OTP");
//
//        if (authService.verifyOtp(otpVerificationDto)) {
//
//            AuthResponse authResponse = AuthResponse.builder()
//                    .token(authService.generateToken(otpVerificationDto))
//                    .expiresIn(86400)
//                    .build();
//
//            return ResponseEntity.ok(authResponse);
//        }
//        return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return ResponseEntity.ok(authService.verifyOtp(otpVerificationDto));


    }



}
