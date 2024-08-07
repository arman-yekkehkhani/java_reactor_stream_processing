package com.example.assignment.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Constructs {@link ObjectMapper} instances with custom features enabled or disabled.
 */
public final class ObjectMapperFactory {
    private static ObjectMapper objectMapper;

    private ObjectMapperFactory() {
    }

    /* You may tweak the configuration of the mapper returned by this method as needed. */
    public static ObjectMapper getOrCreateObjectMapper() {
        if (objectMapper == null) {
            objectMapper = JsonMapper.builder()
                    .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
                    .propertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy())
                    .serializationInclusion(JsonInclude.Include.NON_NULL)
                    .addModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                    .enable(SerializationFeature.INDENT_OUTPUT)
                    .build();
        }
        return objectMapper;
    }
}
