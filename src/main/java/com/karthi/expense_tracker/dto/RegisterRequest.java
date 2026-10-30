package com.karthi.expense_tracker.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank(message = "Name is required") @Size(max = 100) String name,
        @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email,
        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 72, message = "Password must be 6 to 72 characters") String password
) {}