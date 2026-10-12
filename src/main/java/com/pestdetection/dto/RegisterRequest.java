package com.pestdetection.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "is required") @Size(min = 3, max = 60) String username,
        @Email(message = "must be a valid email address") String email,
        @NotBlank(message = "is required") @Size(min = 6, max = 100, message = "must be at least 6 characters") String password) {
}
