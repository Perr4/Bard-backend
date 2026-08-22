package it.chronos.auth.dto;

import java.util.List;
import java.util.UUID;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(String email, String password) {
    }

    public record RegisterRequest(String email, String firstName, String lastName, String password, String role) {
    }

    public record AuthResponse(String token, UUID userId, String email, String fullName, List<String> roles) {
    }

    public record UserProfile(UUID userId, String email, String fullName, List<String> roles) {
    }
}
