package com.bank.authservice.service.impl;

import com.bank.authservice.dto.LoginRequest;
import com.bank.authservice.dto.RegisterRequest;
import com.bank.authservice.entity.User;
import com.bank.authservice.kafka.UserEventProducer;
import com.bank.authservice.repository.UserRepository;
import com.bank.authservice.service.AuthService;
import com.bank.authservice.service.JwtTokenProvider;
import com.bank.common.event.UserCreatedEvent;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
    private AuthenticationManager authenticationManager;
    private JwtTokenProvider jwtTokenProvider;
    private UserRepository userRepository;
    private UserEventProducer userEventProducer;

    @Override
    public String authenticate(LoginRequest loginRequest) {
        // Authenticate the user and generate JWT token
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), loginRequest.getPassword());

        authenticationManager.authenticate(authenticationToken);

        Optional<User> userDetails = userRepository.findByUsername(loginRequest.getUsername());
        if (userDetails.isPresent()) {
            User user = userDetails.get();
            return jwtTokenProvider.generateToken(user);
        }
        return null;
    }

    @Override
    public User registerUser(RegisterRequest registerRequest) {
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(new BCryptPasswordEncoder().encode(registerRequest.getPassword()));
        user.setRoles(registerRequest.getRole());

        User savedUser = userRepository.save(user);
        UserCreatedEvent event = new UserCreatedEvent(
                savedUser.getId(),
                user.getUsername(),
                user.getRoles(),
                new Date()
        );

        userEventProducer.sendUserCreatedEvent(event);
        return savedUser;
    }


}
