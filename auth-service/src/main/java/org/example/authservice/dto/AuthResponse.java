package org.example.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String userId;
    private String email;
    private String role;
}
