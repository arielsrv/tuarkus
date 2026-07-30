package org.github.services;

import io.smallrye.mutiny.Uni;
import org.github.clients.GoRestClient;
import org.github.clients.responses.CommentResponse;
import org.github.clients.responses.PostResponse;
import org.github.clients.responses.TodoResponse;
import org.github.clients.responses.UserResponse;
import org.github.dto.UserDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    GoRestClient client;

    @InjectMocks
    UserService userService;

    @Test
    void getUsers_mapsUserResponsesToUserDTOs_withPostsAndTodos() {
        when(client.getUsers(anyInt())).thenReturn(Uni.createFrom().item(List.of(
                new UserResponse(1L, "Alice", "alice@example.com"),
                new UserResponse(2L, "Bob", "bob@example.com"))));

        when(client.getPosts(1L)).thenReturn(Uni.createFrom().item(List.of(new PostResponse(10L, "Post 1"))));
        when(client.getPosts(2L)).thenReturn(Uni.createFrom().item(List.of(new PostResponse(20L, "Post 2"))));

        when(client.getTodos(1L)).thenReturn(Uni.createFrom().item(List.of(new TodoResponse(100L, "Todo 1", "Body 1", null))));
        when(client.getTodos(2L)).thenReturn(Uni.createFrom().item(List.of(new TodoResponse(200L, "Todo 2", "Body 2", null))));

        // Each post fetches its comments (nested level of concurrency).
        when(client.getComments(10L)).thenReturn(Uni.createFrom().item(List.of(
                new CommentResponse(1000L, "Carol", "carol@example.com", "Comment on post 10"))));
        when(client.getComments(20L)).thenReturn(Uni.createFrom().item(List.of(
                new CommentResponse(2000L, "Dave", "dave@example.com", "Comment on post 20"))));

        List<UserDTO> result = userService.getUsers().await().indefinitely();

        assertThat(result).hasSize(2);
        UserDTO alice = result.get(0);
        UserDTO bob = result.get(1);

        assertThat(alice.userId()).isEqualTo(1L);
        assertThat(alice.name()).isEqualTo("Alice");
        assertThat(alice.email()).isEqualTo("alice@example.com");
        assertThat(alice.posts()).hasSize(1);
        assertThat(alice.posts().getFirst().id()).isEqualTo(10L);
        assertThat(alice.posts().getFirst().title()).isEqualTo("Post 1");
        assertThat(alice.posts().getFirst().comments()).hasSize(1);
        assertThat(alice.posts().getFirst().comments().getFirst().id()).isEqualTo(1000L);
        assertThat(alice.posts().getFirst().comments().getFirst().name()).isEqualTo("Carol");
        assertThat(alice.posts().getFirst().comments().getFirst().email()).isEqualTo("carol@example.com");
        assertThat(alice.posts().getFirst().comments().getFirst().body()).isEqualTo("Comment on post 10");
        assertThat(alice.todos()).hasSize(1);
        assertThat(alice.todos().getFirst().id()).isEqualTo(100L);
        assertThat(alice.todos().getFirst().title()).isEqualTo("Todo 1");
        assertThat(alice.todos().getFirst().body()).isEqualTo("Body 1");

        assertThat(bob.userId()).isEqualTo(2L);
        assertThat(bob.name()).isEqualTo("Bob");
        assertThat(bob.email()).isEqualTo("bob@example.com");
        assertThat(bob.posts()).hasSize(1);
        assertThat(bob.posts().getFirst().id()).isEqualTo(20L);
        assertThat(bob.posts().getFirst().title()).isEqualTo("Post 2");
        assertThat(bob.posts().getFirst().comments()).hasSize(1);
        assertThat(bob.posts().getFirst().comments().getFirst().id()).isEqualTo(2000L);
        assertThat(bob.posts().getFirst().comments().getFirst().body()).isEqualTo("Comment on post 20");
        assertThat(bob.todos()).hasSize(1);
        assertThat(bob.todos().getFirst().id()).isEqualTo(200L);
        assertThat(bob.todos().getFirst().title()).isEqualTo("Todo 2");
        assertThat(bob.todos().getFirst().body()).isEqualTo("Body 2");
    }
}
