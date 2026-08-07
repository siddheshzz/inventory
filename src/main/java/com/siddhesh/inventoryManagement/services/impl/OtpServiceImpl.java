package com.siddhesh.inventoryManagement.services.impl;

import com.siddhesh.inventoryManagement.services.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final RedisTemplate<String,String> redisTemplate;

    private final SecureRandom random = new SecureRandom();



    @Override
    public String generateOtp() {
        int otp = 100000 + random.nextInt(900000); // 6 digit OTP
        return String.valueOf(otp);
    }
    private String otpKey(String phoneNumber){
        return "otp:" + phoneNumber;
    }

    @Override
    public void storeOtp(String phoneNumber, String otp) {

//        redisTemplate.opsForValue().set(otpKey(phoneNumber),otp, Duration.ofMinutes(5));
        String key = otpKey(phoneNumber);

        System.out.println("Saving Redis key: " + key);
        System.out.println("Saving Redis value: " + otp);

        redisTemplate.opsForValue()
                .set(key, otp, Duration.ofMinutes(5));


    }

    @Override
    public boolean verifyOtp(String phoneNumber, String otp) {

//        if(redisTemplate.opsForValue().get(phoneNumber).equals(otp)){
//            return true;
//        }
//
//        return false;
        String storedOtp =
                redisTemplate.opsForValue()
                        .get(otpKey(phoneNumber));


        if(storedOtp == null){
            return false;
        }

        if(storedOtp.equals(otp)) {

            redisTemplate.delete(otpKey(phoneNumber));

            return true;
        }


        return false;
    }
}
