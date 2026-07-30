package org.github.clients.responses;

public record UserResponse(Long id, String name, String email, String gender, String status) {
}
