package org.github.controllers;

import io.quarkus.test.junit.QuarkusIntegrationTest;

// Runs UserControllerWireMockTest against the packaged (native) binary. The WireMock test
// resource is inherited from the base class, so the native process is pointed at the stub
// server the same way the JVM test is.
@QuarkusIntegrationTest
class UserControllerIT extends UserControllerWireMockTest {
    // Execute the same tests but in packaged mode.
}