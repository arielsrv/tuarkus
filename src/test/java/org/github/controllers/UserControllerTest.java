package org.github.controllers;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.github.clients.GoRestClient;
import org.github.clients.responses.CommentResponse;
import org.github.clients.responses.PostResponse;
import org.github.clients.responses.TodoResponse;
import org.github.clients.responses.UserResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@QuarkusTest
class UserControllerTest {

    // @InjectMock + @RestClient replaces the REST client bean with a mock, so the
    // test exercises the real endpoint (/users) without hitting the network.
    @InjectMock
    @RestClient
    GoRestClient client;

    @Test
    void getUsers_returnsJson_withSnakeCaseAndNestedPostsCommentsTodos() {
        when(client.getUsers()).thenReturn(Uni.createFrom().item(List.of(
                new UserResponse(1L, "Alice", "alice@example.com"))));
        when(client.getPosts(1L)).thenReturn(Uni.createFrom().item(List.of(new PostResponse(10L, "Post 1"))));
        when(client.getTodos(1L)).thenReturn(Uni.createFrom().item(List.of(new TodoResponse(100L, "Todo 1", "Body 1", null))));
        when(client.getComments(10L)).thenReturn(Uni.createFrom().item(List.of(
                new CommentResponse(1000L, "Carol", "carol@example.com", "Comment on post 10"))));

        given()
                .when().get("/users")
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].user_id", equalTo(1))           // snake_case: userId -> user_id
                .body("[0].name", equalTo("Alice"))
                .body("[0].posts[0].title", equalTo("Post 1"))
                .body("[0].posts[0].comments[0].name", equalTo("Carol"))
                .body("[0].todos[0].title", equalTo("Todo 1"))
                .body("[0].todos[0]", not(hasKey("due_on")));  // null due_on is omitted (serialization-inclusion=non-null)
    }
}
