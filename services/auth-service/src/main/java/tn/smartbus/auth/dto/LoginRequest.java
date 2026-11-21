package tn.smartbus.auth.dto;

import jakarta.validation.constraints.*;

public record LoginRequest(
    @NotBlank @Email @Size(max = 180) String email, @NotBlank @Size(max = 72) String password) {}
