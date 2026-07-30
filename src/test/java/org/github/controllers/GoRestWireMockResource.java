package org.github.controllers;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

// Starts a WireMock server in the outer test JVM and points the "gorest" REST client at
// it. This works in both @QuarkusTest (JVM) and @QuarkusIntegrationTest (native): the
// returned config map is applied to the launched process, so the packaged binary talks
// to WireMock instead of the real GoRest API. No Mockito involved.
public class GoRestWireMockResource implements QuarkusTestResourceLifecycleManager {

    private WireMockServer server;

    @Override
    public Map<String, String> start() {
        this.server = new WireMockServer(options().dynamicPort());
        this.server.start();

        stubJson("/public/v2/users",
                "[{\"id\":1,\"name\":\"Alice\",\"email\":\"alice@example.com\"}]");
        stubJson("/public/v2/users/1/posts",
                "[{\"id\":10,\"title\":\"Post 1\"}]");
        stubJson("/public/v2/users/1/todos",
                "[{\"id\":100,\"title\":\"Todo 1\",\"body\":\"Body 1\",\"due_on\":null}]");
        stubJson("/public/v2/posts/10/comments",
                "[{\"id\":1000,\"name\":\"Carol\",\"email\":\"carol@example.com\",\"body\":\"Comment on post 10\"}]");

        return Map.of("quarkus.rest-client.gorest.url", this.server.baseUrl());
    }

    private void stubJson(String path, String body) {
        this.server.stubFor(get(urlEqualTo(path))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)));
    }

    @Override
    public void stop() {
        if (this.server != null) {
            this.server.stop();
        }
    }
}