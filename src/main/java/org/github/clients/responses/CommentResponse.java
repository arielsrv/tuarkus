package org.github.clients.responses;

public record CommentResponse(Long id, String name, String email, String body) {
}
