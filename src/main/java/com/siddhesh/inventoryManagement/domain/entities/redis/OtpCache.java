package com.siddhesh.inventoryManagement.domain.entities.redis;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Senior Standard: Stored in a distinct Redis namespace with a 5-minute (300 seconds) auto-expiry
@RedisHash(value = "otp_verification")
public class OtpCache implements Serializable {

    @Id
    private String phoneNumber; // Acts as the primary lookup key

    private String otpCode;

    @TimeToLive
    private Long expirationInSeconds; // Automatically handles record deletion out of Redis!
}
