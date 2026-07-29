package org.github.clients.responses;

import java.time.ZonedDateTime;

// due_on is resolved by the ObjectMapper's global SNAKE_CASE strategy, no @JsonProperty needed.
public record TodoResponse(Long id, String title, String body, ZonedDateTime dueOn) {
}
