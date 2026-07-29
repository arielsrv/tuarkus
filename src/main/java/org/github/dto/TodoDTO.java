package org.github.dto;

import java.time.ZonedDateTime;

// dueOn is serialized as due_on by the global SNAKE_CASE strategy (no @JsonProperty).
public record TodoDTO(Long id, String title, String body, ZonedDateTime dueOn) {
}
