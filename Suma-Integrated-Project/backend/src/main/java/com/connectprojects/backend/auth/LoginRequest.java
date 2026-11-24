package com.connectprojects.backend.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Enter your username or email address.")
        @Size(max = 254, message = "Username or email address is too long.")
        String username,
        @NotBlank(message = "Enter your password.")
        @Size(max = 72, message = "Password is too long.")
        String password
) {
}
