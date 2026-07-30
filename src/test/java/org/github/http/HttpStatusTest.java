package org.github.http;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class HttpStatusTest {

    @Test
    void okIsHttp200() {
        assertEquals("200", HttpStatus.OK);
    }
}