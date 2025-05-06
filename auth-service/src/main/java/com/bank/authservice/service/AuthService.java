package com.bank.authservice.service;

import com.bank.authservice.dto.LoginRequest;
import com.bank.authservice.dto.RegisterRequest;
import com.bank.authservice.entity.User;

public interface AuthService {
    String authenticate(LoginRequest loginRequest);
    User registerUser(RegisterRequest registerRequest);
}
