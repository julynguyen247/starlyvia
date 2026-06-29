package org.example.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    @Email
    @NotBlank
    @Schema(example = "user@example.com")
    private String email;

    @NotBlank
    @Schema(example = "password123")
    private String password;

    @NotBlank
    @Schema(example = "user")
    private String username;

    @Schema(example = "https://example.com/avatar.png")
    private String avatarUrl;

    @Size(max = 500)
    @Schema(example = "Hello from Starlyvia")
    private String bio;
}
