package org.github;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/hello")
@Tag(name = "Example", description = "Health/greeting endpoint")
public class ExampleResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Greeting", description = "Returns a plain-text greeting; useful as a liveness smoke check.")
    @APIResponse(responseCode = "200", description = "The greeting text",
            content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(implementation = String.class)))
    public String hello() {
        return "Hello from Quarkus REST";
    }
}
