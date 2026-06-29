package org.example.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class AuthResponse {
    @Schema(example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(example = "1")
    private String userId;

    @Schema(example = "user@example.com")
    private String email;

    @Schema(example = "user")
    private String username;

    @Schema(example = "USER")
    private String role;

    @Schema(example = "https://example.com/avatar.png")
    private String avatarUrl;

    @Schema(example = "Hello from Starlyvia")
    private String bio;
}
