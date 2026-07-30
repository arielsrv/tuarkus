package org.github.clients.responses;

import java.time.ZonedDateTime;

// due_on is resolved by the ObjectMapper's global SNAKE_CASE strategy, no @JsonProperty needed.
// gorest todos have no body field; they carry a status (pending/completed) instead.
public record TodoResponse(Long id, String title, ZonedDateTime dueOn, String status) {
}
