- Create an AuthService that handles the core logical operations of your OTP auth flow.


### requestOtp(String phoneNumber)
- Check if a user exists with that phone number. If not, create a new user profile shell (Registration).

- Generate a secure, random 4- or 6-digit numeric code.

- Save the OTP to your Redis cache/OTP table with an expiration timestamp.

- Trigger smsService.sendOtp(...).

### verifyOtp(String phoneNumber, String code)
- Retrieve the saved OTP code for that phone number.

- Check if it matches and ensure it hasn't expired.

- If valid, generate a stateless access token (like a JWT - JSON Web Token).

- Clear the OTP token out of the cache so it can't be reused.

```
POST /api/v1/auth/otp/request (Accepts phone number)

POST /api/v1/auth/otp/verify (Accepts phone number + code; returns JWT token)

```

# Authentication Flow Architecture

```text
┌──────────────────────────┐
│     1. Repositories      │
└──────────────────────────┘
              │
              ▼
┌──────────────────────────┐
│ 2. OTP Storage Strategy  │
└──────────────────────────┘
              │
              ▼
┌──────────────────────────┐
│ 3. Gateway Interfaces    │
└──────────────────────────┘
              │
              ▼
┌──────────────────────────┐
│ 4. Auth Service Logic    │
└──────────────────────────┘
              │
              ▼
┌──────────────────────────┐
│    5. Auth Controller    │
└──────────────────────────┘
```