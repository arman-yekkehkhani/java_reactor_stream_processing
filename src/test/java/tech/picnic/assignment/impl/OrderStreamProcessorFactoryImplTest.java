package tech.picnic.assignment.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Iterator;
import java.util.Scanner;
import java.util.ServiceLoader;
import java.util.stream.Stream;
import org.json.JSONException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import tech.picnic.assignment.api.OrderStreamProcessor;
import tech.picnic.assignment.api.OrderStreamProcessorFactory;

final class OrderStreamProcessorFactoryImplTest {
    private static Stream<Arguments> testProcessInputProvider() {
        return Stream.of(
                Arguments.of(
                        100, Duration.ofSeconds(30), "happy-path-input.json-stream", "happy-path-output.json"));
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("testProcessInputProvider")
    void testProcess(
            int maxOrders, Duration maxTime, String inputResource, String expectedOutputResource)
            throws IOException, JSONException {
        try (InputStream source = getClass().getResourceAsStream(inputResource);
             ByteArrayOutputStream sink = new ByteArrayOutputStream()) {
            OrderStreamProcessorFactory factory = new OrderStreamProcessorFactoryImpl();
            OrderStreamProcessor processor = factory.createProcessor(maxOrders, maxTime);
            processor.process(source, sink);
            String expectedOutput = loadResource(expectedOutputResource);
            String actualOutput = sink.toString(StandardCharsets.UTF_8);
            JSONAssert.assertEquals(expectedOutput, actualOutput, JSONCompareMode.STRICT);
        }
    }

    private String loadResource(String resource) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(resource);
             Scanner scanner = new Scanner(is, StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    /**
     * Verifies that precisely one {@link OrderStreamProcessorFactory} can be service-loaded.
     */
    @Test
    void testServiceLoading() {
        Iterator<OrderStreamProcessorFactory> factories =
                ServiceLoader.load(OrderStreamProcessorFactory.class).iterator();
        assertTrue(factories.hasNext(), "No OrderStreamProcessorFactory is service-loaded");
        factories.next();
        assertFalse(factories.hasNext(), "More than one OrderStreamProcessorFactory is service-loaded");
    }
}
