package com.spring.notes.app.service;

import com.spring.notes.app.config.JwtUtil;
import com.spring.notes.app.dto.request.LoginRequest;
import com.spring.notes.app.dto.request.RegisterRequest;
import com.spring.notes.app.dto.response.AuthResponse;
import com.spring.notes.app.entity.User;
import com.spring.notes.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());
        
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(User.Role.USER)
                .enabled(true)
                .build();
        
        User savedUser = userRepository.save(user);
        
        // Generate JWT token
        String jwtToken = jwtUtil.generateToken(savedUser);
        
        return AuthResponse.builder()
                .token(jwtToken)
                .user(AuthResponse.UserInfo.builder()
                        .id(savedUser.getId())
                        .username(savedUser.getUsername())
                        .email(savedUser.getEmail())
                        .firstName(savedUser.getFirstName())
                        .lastName(savedUser.getLastName())
                        .role(savedUser.getRole())
                        .createdAt(savedUser.getCreatedAt())
                        .build())
                .build();
    }
    
    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt: {}", request.getUsername());
        
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        
        User user = (User) authentication.getPrincipal();
        
        // Generate JWT token
        String jwtToken = jwtUtil.generateToken(user);
        
        return AuthResponse.builder()
                .token(jwtToken)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .role(user.getRole())
                        .createdAt(user.getCreatedAt())
                        .build())
                .build();
    }
}
