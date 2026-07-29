package org.github.controllers;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.github.dto.UserDTO;
import org.github.http.HttpStatus;
import org.github.services.UserService;

import java.util.List;

@Path("/users")
@Tag(name = "Users", description = "User aggregates enriched with posts, comments and todos")
public class UserController {

    @Inject
    UserService userService;

    // Return the Uni directly: Quarkus REST subscribes for us and writes the response
    // when the Uni emits, without blocking the event loop thread.
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List users",
            description = "Fetches every user from GoRest and, for each one, concurrently aggregates their "
                    + "posts (with the comments of each post) and their todos into a single response.")
    @APIResponse(responseCode = HttpStatus.OK, description = "The list of aggregated users",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.ARRAY, implementation = UserDTO.class)))
    public Uni<List<UserDTO>> getUsers() {
        return this.userService.getUsers();
    }
}
