package org.github.controllers;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.github.dto.UserDTO;
import org.github.services.UserService;

import java.util.List;

@Path("/users")
public class UserController {

    @Inject
    UserService userService;

    // Return the Uni directly: Quarkus REST subscribes for us and writes the response
    // when the Uni emits, without blocking the event loop thread.
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<UserDTO>> getUsers() {
        return userService.getUsers();
    }
}
