package com.bank.accountservice.utils;

import com.bank.accountservice.dto.UserResponse;
import com.bank.accountservice.repository.httpclient.UserClient;
import com.bank.common.response.ApiResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserClientFallback implements UserClient {

    @Override
    public ApiResponse<UserResponse> getUserProfile(UUID userId) {
        UserResponse defaultUser = UserResponse.builder()
                .id(userId.toString())
                .username("UNKNOWN USER")
                .email("unknown@example.com")
                .build();

        return ApiResponse.<UserResponse>builder()
                .result(defaultUser)
                .message("identity-service is currently unavailable. Returning fallback response.")
                .build();
    }
}
