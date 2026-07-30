# tuarkus

[![CI](https://github.com/arielsrv/tuarkus/actions/workflows/ci.yml/badge.svg)](https://github.com/arielsrv/tuarkus/actions/workflows/ci.yml)
![Coverage](.github/badges/jacoco.svg)

A **fully reactive** sample API built with [Quarkus](https://quarkus.io/), ported from
[`javalin-api`](../javalin-api) by swapping **RxJava for [Mutiny](https://smallrye.io/smallrye-mutiny/)**.

It exposes a single endpoint, `GET /users`, that aggregates data from the public
[gorest.co.in](https://gorest.co.in) API: for each user it fetches, **in parallel**, its `posts`
(and for each post its `comments`) and its `todos`, and assembles a nested `UserDTO`.

The whole pipeline is non-blocking: the HTTP calls run on the Vert.x event loop and Quarkus REST subscribes to the `Uni`
for us, without tying up threads while waiting.

## Endpoint

```shell
curl http://localhost:8080/users
```

```jsonc
[
  {
    "user_id": 8548968,
    "name": "Swarnalata Ahluwalia",
    "email": "swarnalata_ahluwalia@kovacek.example",
    "posts": [
      { "id": 1, "title": "Post 1", "comments": [ { "id": 10, "name": "...", "email": "...", "body": "..." } ] }
    ],
    "todos": [
      { "id": 107119, "title": "...", "due_on": "2026-08-04T18:30:00Z" }
    ]
  }
]
```

## Architecture

```
org.github
├── clients
│   ├── GoRestClient            reactive REST client (@RegisterRestClient), 4 endpoints → Uni<...>
│   └── responses               UserResponse, PostResponse, TodoResponse, CommentResponse
├── dto                         UserDTO, PostDTO, TodoDTO, CommentDTO  (the response)
├── services
│   └── UserService             the fan-out / orchestration
└── controllers
    └── UserController          GET /users → Uni<List<UserDTO>>
```

### The reactive fan-out

`UserService` is the Mutiny equivalent of `javalin-api`'s `UserService`:

| javalin-api (RxJava)                                      | tuarkus (Mutiny)                                          |
|-----------------------------------------------------------|-----------------------------------------------------------|
| `Observable<List<T>>`                                     | `Uni<List<T>>`                                            |
| `parallelMapEach` → `concatMapEager` (parallel + ordered) | `Uni.join().all(unis).andFailFast()` (parallel + ordered) |
| `Observable.zip(posts, todos, …)`                         | `Uni.combine().all().unis(posts, todos).asTuple()`        |
| you subscribe yourself                                    | Quarkus REST subscribes for you                           |

`andFailFast()`: if a sub-call fails, it short-circuits immediately. To tolerate partial failures, switch it to
`andCollectFailures()`.

### ObjectMapper

`config.ObjectMapperConfig` is an `ObjectMapperCustomizer` (equivalent to `javalin-api`'s
`ObjectMapperProvider`) applied to the single managed mapper used by both the server **and**
the REST client:

```java
mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)  // userId → user_id, dueOn → due_on
    .

setSerializationInclusion(JsonInclude.Include.NON_NULL)           // null fields are omitted
    .

disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)           // ISO-8601 dates
    .

disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);       // ignore extra fields from gorest
```

Thanks to `SNAKE_CASE`, no `@JsonProperty` annotations are needed on the records.

> It is configured in code rather than via the `quarkus.jackson.property-naming-strategy`
> property on purpose: that property makes Quarkus resolve the strategy with a reflective
> `Class.forName`, which fails in **native image** with `ClassNotFoundException`. Referencing
> `PropertyNamingStrategies.SNAKE_CASE` in Java pins the class so GraalVM keeps it.

The client base URL is configured with:

```properties
quarkus.rest-client.gorest.url=https://gorest.co.in
```

## Dev mode (hot reload)

```shell
./gradlew quarkusDev          # or: task dev
```

Dev UI at <http://localhost:8080/q/dev/>.

## Tests

```shell
./gradlew test                # or: task test  (runs ./gradlew check)
```

- **`UserServiceTest`** — Mockito unit test: mocks `GoRestClient` and verifies the full mapping (posts → comments +
  nested todos).
- **`UserControllerTest`** — `@QuarkusTest` + RestAssured against `/users` with the REST client mocked
  (`@InjectMock @RestClient`); asserts the snake_case JSON and that null fields are omitted.

## Packaging

```shell
./gradlew build                                        # JVM: build/quarkus-app/quarkus-run.jar
java -jar build/quarkus-app/quarkus-run.jar
```

## Native & Docker

Prefer the `Taskfile` wrappers (`task --list`), which validate the binary format before it goes into an image:

```shell
task native:build          # native binary for THIS host (Mach-O on macOS) — runs locally
task native:build:linux    # Linux (ELF) binary built inside the Mandrel image, for Docker
task native:test           # native integration tests (src/native-test)
task docker:build:native   # native container image
task docker:smoke:native   # start the image, send a request, report startup + memory
```

See the native build notes in `gradle.properties`. More info:
<https://quarkus.io/guides/gradle-tooling>.
