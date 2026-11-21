package tn.smartbus.auth.dto;

import tn.smartbus.auth.entity.UserRole;

public record UserResponse(String id, String fullName, String email, UserRole role) {}
