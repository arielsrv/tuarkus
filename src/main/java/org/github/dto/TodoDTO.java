package org.github.dto;

import java.time.ZonedDateTime;

// dueOn se serializa como due_on por la estrategia SNAKE_CASE global (sin @JsonProperty).
public record TodoDTO(Long id, String title, String body, ZonedDateTime dueOn) {
}
