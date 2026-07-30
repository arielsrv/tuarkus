package org.github.services;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.github.clients.GoRestClient;
import org.github.clients.responses.PostResponse;
import org.github.clients.responses.UserResponse;
import org.github.dto.CommentDTO;
import org.github.dto.PostDTO;
import org.github.dto.TodoDTO;
import org.github.dto.UserDTO;
import org.github.util.MutinyUtils;

import java.util.List;


@ApplicationScoped
public class UserService {

    @RestClient
    GoRestClient httpClient;

    // Page size requested from gorest for the top-level users list. gorest caps per_page
    // at 100; larger values are silently ignored and fall back to 10.
    @ConfigProperty(name = "gorest.users.per-page", defaultValue = "10")
    int usersPerPage;

    // users -> for each user, in parallel: (posts -> for each post-its comments) + todos.
    // MutinyUtils.joinAll(...) subscribes to all the Unis at once (real fan-out) and returns
    // a Uni<List<...>> preserving the input order.
    public Uni<List<UserDTO>> getUsers() {
        return this.httpClient.getUsers(this.usersPerPage)
                .onItem().transformToUni(userResponseList -> MutinyUtils.joinAll(userResponseList.stream()
                        .map(user -> {
                            // Second level of fan-out: for each user post, its comments in parallel.
                            Uni<List<PostDTO>> posts = this.getPostsDto(user);
                            Uni<List<TodoDTO>> todos = this.getTodosDto(user);

                            return Uni.combine().all().unis(posts, todos).asTuple()
                                    .map(tuple -> new UserDTO(user.id(), user.name(), user.email(), tuple.getItem1(), tuple.getItem2()));
                        })
                        .toList()));
    }

    private Uni<List<PostDTO>> getPostsDto(UserResponse userResponse) {
        return this.httpClient.getPosts(userResponse.id())
                .onItem().transformToUni(userPosts -> MutinyUtils.joinAll(userPosts.stream()
                        .map(this::getPostWithComments)
                        .toList()));
    }

    private Uni<List<TodoDTO>> getTodosDto(UserResponse userResponse) {
        return this.httpClient.getTodos(userResponse.id())
                .map(todoResponseList -> todoResponseList.stream()
                        .map(todoResponse -> new TodoDTO(todoResponse.id(), todoResponse.title(), todoResponse.body(), todoResponse.dueOn()))
                        .toList());
    }

    private Uni<PostDTO> getPostWithComments(PostResponse postResponse) {
        return this.httpClient.getComments(postResponse.id())
                .map(commentResponseList -> new PostDTO(postResponse.id(), postResponse.title(), commentResponseList.stream()
                        .map(commentResponse -> new CommentDTO(commentResponse.id(), commentResponse.name(), commentResponse.email(), commentResponse.body()))
                        .toList()));
    }
}