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

    // users -> por cada user, en paralelo: (posts -> por cada post sus comments) + todos.
    // Uni.join().all(...).andFailFast() suscribe todos los Uni a la vez (fan-out real)
    // y devuelve un Uni<List<...>> preservando el orden de entrada. Es el equivalente
    // Mutiny del concatMapEager de RxJava.
    public Uni<List<UserDTO>> getUsers() {
        return client.getUsers()
            .onItem().transformToUni(users -> joinAll(users.stream().map(this::toUserDTO).toList()));
    }

    private Uni<UserDTO> toUserDTO(UserResponse user) {
        Uni<List<PostDTO>> posts = postsWithComments(user.id());
        Uni<List<TodoResponse>> todos = client.getTodos(user.id());
        return Uni.combine().all().unis(posts, todos).asTuple()
            .map(tuple -> mapToUserDTO(user, tuple.getItem1(), tuple.getItem2()));
    }

    // Segundo nivel de fan-out: por cada post del usuario, sus comments en paralelo.
    private Uni<List<PostDTO>> postsWithComments(Long userId) {
        return client.getPosts(userId)
            .onItem().transformToUni(posts -> joinAll(posts.stream()
                .map(post -> client.getComments(post.id())
                    .map(comments -> mapToPostDTO(post, comments)))
                .toList()));
    }

    // Uni.join().all() no acepta una lista vacía, así que ese caso se corta antes.
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
