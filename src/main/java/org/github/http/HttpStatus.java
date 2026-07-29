package org.github.http;

/**
 * Compile-time String constants for HTTP status codes, usable as {@code @APIResponse}
 * response codes.
 *
 * <p>This is a class of String constants rather than an enum on purpose: annotation
 * attributes must be constant expressions, and an enum value is not a String. JAX-RS's
 * {@link jakarta.ws.rs.core.Response.Status} cannot be used either, since its code is
 * only reachable via the {@code getStatusCode()} method call.
 */
public final class HttpStatus {

    public static final String OK = "200";

    private HttpStatus() {
    }
}
