package org.github.services;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.github.clients.GoRestClient;
import org.github.clients.responses.PostResponse;
import org.github.clients.responses.TodoResponse;
import org.github.clients.responses.UserResponse;
import org.github.dto.CommentDTO;
import org.github.dto.PostDTO;
import org.github.dto.TodoDTO;
import org.github.dto.UserDTO;

import java.util.List;

import static org.github.util.MutinyUtils.joinAll;

@ApplicationScoped
public class UserService {

    @RestClient
    GoRestClient httpClient;

    // users -> for each user, in parallel: (posts -> for each post-its comments) + todos.
    // MutinyUtils.joinAll(...) subscribes to all the Unis at once (real fan-out) and returns
    // a Uni<List<...>> preserving the input order.
    public Uni<List<UserDTO>> getUsers() {
        return this.httpClient.getUsers()
                .onItem().transformToUni(users -> joinAll(users.stream()
                        .map(user -> {
                            // Second level of fan-out: for each user post, its comments in parallel.
                            Uni<List<PostDTO>> posts = this.getPostsDto(user);
                            Uni<List<TodoResponse>> todos = this.httpClient.getTodos(user.id());

                            return Uni.combine().all().unis(posts, todos).asTuple()
                                    .map(tuple -> new UserDTO(user.id(), user.name(), user.email(), tuple.getItem1(), tuple.getItem2().stream()
                                            .map(todo -> new TodoDTO(todo.id(), todo.title(), todo.body(), todo.dueOn()))
                                            .toList()));
                        })
                        .toList()));
    }

    private Uni<List<PostDTO>> getPostsDto(UserResponse user) {
        return this.httpClient.getPosts(user.id())
                .onItem().transformToUni(userPosts -> joinAll(userPosts.stream()
                        .map(this::getPostWithComments)
                        .toList()));
    }

    private Uni<PostDTO> getPostWithComments(PostResponse post) {
        return this.httpClient.getComments(post.id())
                .map(comments -> new PostDTO(post.id(), post.title(), comments.stream()
                        .map(comment -> new CommentDTO(comment.id(), comment.name(), comment.email(), comment.body()))
                        .toList()));
    }
}