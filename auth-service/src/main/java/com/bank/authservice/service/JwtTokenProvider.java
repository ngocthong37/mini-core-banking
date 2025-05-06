package com.bank.authservice.service;


import org.springframework.security.core.userdetails.UserDetails;

public interface JwtTokenProvider {
    String generateToken(UserDetails userDetails);
    boolean validateToken(String token);
    String getUserFromToken(String token);
}
