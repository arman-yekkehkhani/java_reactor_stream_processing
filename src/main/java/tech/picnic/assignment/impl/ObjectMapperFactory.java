package tech.picnic.assignment.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Constructs {@link ObjectMapper} instances with custom features enabled or disabled.
 */
final class ObjectMapperFactory {
    private ObjectMapperFactory() {
    }

    /* You may tweak the configuration of the mapper returned by this method as needed. */
    public static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.getFactory().disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        return objectMapper;
    }
}
