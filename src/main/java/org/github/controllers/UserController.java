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

    // Devolver el Uni directamente: Quarkus REST se suscribe por nosotros y escribe
    // la respuesta cuando el Uni emite, sin bloquear el hilo del event loop.
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<UserDTO>> getUsers() {
        return userService.getUsers();
    }
}
