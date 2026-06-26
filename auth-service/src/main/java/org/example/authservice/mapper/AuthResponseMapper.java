package org.example.authservice.mapper;

import org.example.authservice.dto.AuthResponse;
import org.example.authservice.dto.RegisterResponse;
import org.example.authservice.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AuthResponseMapper {
    public AuthResponse toAuthResponse(User user, String token) {
        return new AuthResponse(
                token,
                user.getId().toString(),
                user.getEmail(),
                user.getRole()
        );
    }

    public RegisterResponse toRegisterResponse(User user) {
        return new RegisterResponse(user.getId().toString(),
                user.getEmail(),
                user.getRole());
    }
}
