package org.github.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;

// Equivalent to javalin-api's ObjectMapperProvider, applied to the single managed
// ObjectMapper (server + REST client). Configuring the naming strategy in Java rather
// than via quarkus.jackson.property-naming-strategy is deliberate: that property makes
// Quarkus do a Class.forName on PropertyNamingStrategies$SnakeCaseStrategy, which fails
// in native image with ClassNotFoundException. Referencing the constant here pins the
// class statically so GraalVM keeps it. The JavaTimeModule is auto-registered by Quarkus.
@Singleton
public class ObjectMapperConfig implements ObjectMapperCustomizer {

    @Override
    public void customize(ObjectMapper mapper) {
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)   // userId -> user_id
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)            // omit null fields
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)            // ISO-8601 dates
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);        // ignore extra fields
    }
}
