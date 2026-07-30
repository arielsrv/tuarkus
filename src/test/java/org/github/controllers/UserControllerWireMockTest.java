package org.github.controllers;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

// Exercises the real /users endpoint end-to-end with GoRest stubbed at the network layer
// (WireMock) instead of via @InjectMock. Because there is no Mockito, the exact same test
// runs against the native binary — see UserControllerIT in src/native-test.
@QuarkusTest
@QuarkusTestResource(GoRestWireMockResource.class)
class UserControllerWireMockTest {

    @Test
    void getUsers_returnsJson_withSnakeCaseAndNestedPostsCommentsTodos() {
        given()
                .when().get("/users")
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].user_id", equalTo(1))           // snake_case: userId -> user_id
                .body("[0].name", equalTo("Alice"))
                .body("[0].gender", equalTo("female"))
                .body("[0].status", equalTo("active"))
                .body("[0].posts[0].title", equalTo("Post 1"))
                .body("[0].posts[0].body", equalTo("Body of post 1"))
                .body("[0].posts[0].comments[0].name", equalTo("Carol"))
                .body("[0].todos[0].title", equalTo("Todo 1"))
                .body("[0].todos[0].status", equalTo("pending"))
                .body("[0].todos[0]", not(hasKey("due_on")));  // null due_on is omitted (serialization-inclusion=non-null)
    }
}