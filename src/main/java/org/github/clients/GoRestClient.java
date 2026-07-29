package org.github.clients;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.github.clients.responses.CommentResponse;
import org.github.clients.responses.PostResponse;
import org.github.clients.responses.TodoResponse;
import org.github.clients.responses.UserResponse;

import java.util.List;

// Single reactive REST client against gorest: every method returns a Uni, so the
// HTTP calls are non-blocking and run on the event loop (Vert.x).
@RegisterRestClient(configKey = "gorest")
@Path("/public/v2")
public interface GoRestClient {

    @GET
    @Path("/users")
    Uni<List<UserResponse>> getUsers();

    @GET
    @Path("/users/{userId}/posts")
    Uni<List<PostResponse>> getPosts(@PathParam("userId") Long userId);

    @GET
    @Path("/users/{userId}/todos")
    Uni<List<TodoResponse>> getTodos(@PathParam("userId") Long userId);

    @GET
    @Path("/posts/{postId}/comments")
    Uni<List<CommentResponse>> getComments(@PathParam("postId") Long postId);
}
