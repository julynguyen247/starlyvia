package org.example.authservice.service;

import lombok.RequiredArgsConstructor;
import org.example.authservice.dto.*;
import org.example.authservice.entity.User;
import org.example.authservice.event.DomainEventPublisher;
import org.example.authservice.event.UserRegisteredEvent;
import org.example.authservice.mapper.AuthResponseMapper;
import org.example.authservice.repository.UserRepository;
import org.example.authservice.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final String DEFAULT_ROLE = "USER";
    private static final int USER_REGISTERED_EVENT_VERSION = 1;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthResponseMapper authMapper;
    private final DomainEventPublisher eventPublisher;

    @Value("${app.kafka.topics.auth-events:auth.events}")
    private String authEventsTopic;

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(DEFAULT_ROLE)
                .avatarUrl(request.getAvatarUrl())
                .bio(request.getBio())
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        publishUserRegistered(savedUser);
        return authMapper.toRegisterResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );

        return authMapper.toAuthResponse(user,token);
    }


    private void publishUserRegistered(User user) {
        UserRegisteredEvent payload = new UserRegisteredEvent(
                UUID.randomUUID(),
                "user.registered",
                USER_REGISTERED_EVENT_VERSION,
                LocalDateTime.now().toString(),
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole(),
                user.getAvatarUrl(),
                user.getBio()
        );
        eventPublisher.publish(authEventsTopic, user.getId().toString(), payload);
    }
}
