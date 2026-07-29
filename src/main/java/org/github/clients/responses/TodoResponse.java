package org.github.clients.responses;

import java.time.ZonedDateTime;

// due_on lo resuelve la estrategia SNAKE_CASE global del ObjectMapper, sin @JsonProperty.
public record TodoResponse(Long id, String title, String body, ZonedDateTime dueOn) {
}
