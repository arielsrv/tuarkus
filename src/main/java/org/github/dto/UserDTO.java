package org.github.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = "A user with their aggregated posts, comments and todos")
public record UserDTO(
        @Schema(description = "Unique user id", examples = "1") Long userId,
        @Schema(description = "Full name", examples = "Alice") String name,
        @Schema(description = "Email address", examples = "alice@example.com") String email,
        @Schema(description = "The user's posts, each with its comments") List<PostDTO> posts,
        @Schema(description = "The user's todos") List<TodoDTO> todos) {
}
