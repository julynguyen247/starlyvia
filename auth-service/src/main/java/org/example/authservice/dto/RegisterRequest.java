package org.example.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
}
