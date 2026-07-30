package org.github.controllers;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;

// Runs UserControllerWireMockTest against the packaged (native) binary. The test-resource
// annotation is NOT @Inherited, and @QuarkusIntegrationTest (unlike @QuarkusTest) does not
// scan the superclass for it, so WireMock must be declared here directly — otherwise the
// native process falls back to the real GoRest URL from application.properties.
@QuarkusIntegrationTest
@QuarkusTestResource(GoRestWireMockResource.class)
class UserControllerIT extends UserControllerWireMockTest {
    // Execute the same tests but in packaged mode.
}