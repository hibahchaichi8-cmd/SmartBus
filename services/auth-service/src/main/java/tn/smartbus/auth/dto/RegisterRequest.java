package tn.smartbus.auth.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
    @NotBlank @Size(max = 100) String fullName,
    @NotBlank @Email @Size(max = 180) String email,
    @NotBlank @Size(min = 10, max = 72) String password) {}
