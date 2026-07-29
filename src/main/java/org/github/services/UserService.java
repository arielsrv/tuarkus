package org.github.services;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.github.clients.GoRestClient;
import org.github.clients.responses.CommentResponse;
import org.github.clients.responses.PostResponse;
import org.github.clients.responses.TodoResponse;
import org.github.clients.responses.UserResponse;
import org.github.dto.CommentDTO;
import org.github.dto.PostDTO;
import org.github.dto.TodoDTO;
import org.github.dto.UserDTO;

import java.util.List;

@ApplicationScoped
public class UserService {

    @RestClient
    GoRestClient client;

    // users -> for each user, in parallel: (posts -> for each post its comments) + todos.
    // Uni.join().all(...).andFailFast() subscribes to all the Unis at once (real fan-out)
    // and returns a Uni<List<...>> preserving the input order. It is the Mutiny equivalent
    // of RxJava's concatMapEager.
    public Uni<List<UserDTO>> getUsers() {
        return this.client.getUsers()
                .onItem().transformToUni(users -> joinAll(users.stream().map(this::toUserDTO).toList()));
    }

    private Uni<UserDTO> toUserDTO(UserResponse user) {
        Uni<List<PostDTO>> posts = postsWithComments(user.id());
        Uni<List<TodoResponse>> todos = this.client.getTodos(user.id());
        return Uni.combine().all().unis(posts, todos).asTuple()
                .map(tuple -> mapToUserDTO(user, tuple.getItem1(), tuple.getItem2()));
    }

    // Second level of fan-out: for each user post, its comments in parallel.
    private Uni<List<PostDTO>> postsWithComments(Long userId) {
        return this.client.getPosts(userId)
                .onItem().transformToUni(posts -> joinAll(posts.stream()
                        .map(post -> this.client.getComments(post.id())
                                .map(comments -> mapToPostDTO(post, comments)))
                        .toList()));
    }

    // Uni.join().all() does not accept an empty list, so that case is short-circuited first.
    private <T> Uni<List<T>> joinAll(List<Uni<T>> unis) {
        if (unis.isEmpty()) {
            return Uni.createFrom().item(List.of());
        }
        return Uni.join().all(unis).andFailFast();
    }

    private UserDTO mapToUserDTO(UserResponse user, List<PostDTO> posts, List<TodoResponse> todosResponse) {
        List<TodoDTO> todos = todosResponse.stream()
                .map(todo -> new TodoDTO(todo.id(), todo.title(), todo.body(), todo.dueOn()))
                .toList();
        return new UserDTO(user.id(), user.name(), user.email(), posts, todos);
    }

    private PostDTO mapToPostDTO(PostResponse post, List<CommentResponse> commentsResponse) {
        List<CommentDTO> comments = commentsResponse.stream()
                .map(comment -> new CommentDTO(comment.id(), comment.name(), comment.email(), comment.body()))
                .toList();
        return new PostDTO(post.id(), post.title(), comments);
    }
}
