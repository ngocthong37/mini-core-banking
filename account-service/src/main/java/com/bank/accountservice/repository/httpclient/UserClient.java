package com.bank.accountservice.repository.httpclient;

import com.bank.accountservice.configuration.AuthenticationRequestInterceptor;
import com.bank.accountservice.dto.UserResponse;
import com.bank.accountservice.utils.UserClientFallback;
import com.bank.common.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

@FeignClient(
        name = "profile-service",
        url = "${app.services.identity}",
        configuration = {AuthenticationRequestInterceptor.class},
        fallback = UserClientFallback.class
)
public interface UserClient {
    @GetMapping(value = "/identity/internal/users/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<UserResponse> getUserProfile(@PathVariable UUID userId);
}

