package tech.picnic.assignment.impl.io;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class IoAdapterImplTest {

    @Test
    void givenInputStream_whenInvokeRead_shouldReturnStringFlux() {
        String inputString = "First line\nSecond line";
        InputStream source = new ByteArrayInputStream(inputString.getBytes(StandardCharsets.UTF_8));

        IoAdapter ioAdapter = new IoAdapterImpl();
        Flux<String> stringFlux = ioAdapter.read(source, 10, Duration.ofSeconds(10));

        StepVerifier.create(stringFlux)
                .expectNext("First line")
                .expectNext("Second line")
                .expectComplete()
                .verify();

    }

    @Test
    void givenInputStream_whenInvokeRead_shouldReturnNonBlankLines() {
        String inputString = "First line\n \n  \nSecond line";
        InputStream source = new ByteArrayInputStream(inputString.getBytes(StandardCharsets.UTF_8));

        IoAdapter ioAdapter = new IoAdapterImpl();
        Flux<String> stringFlux = ioAdapter.read(source, 10, Duration.ofSeconds(10));

        StepVerifier.create(stringFlux)
                .expectNext("First line")
                .expectNext("Second line")
                .expectComplete()
                .verify();

    }

    @Test
    void givenInputStreamAndMaxLines_whenInvokeRead_shouldReturnValidNumOfLines() {
        String inputString = "First line\nSecond line\nThird line";
        InputStream source = new ByteArrayInputStream(inputString.getBytes(StandardCharsets.UTF_8));

        IoAdapter ioAdapter = new IoAdapterImpl();
        Flux<String> stringFlux = ioAdapter.read(source, 2, Duration.ofSeconds(10));

        StepVerifier.create(stringFlux)
                .expectNext("First line")
                .expectNext("Second line")
                .expectComplete()
                .verify();

    }

    @Test
    void givenInputStreamAndMaxTime_whenInvokeRead_shouldReturnValidString() {
        String inputString = "First line\nSecond line";
        InputStream source = new ByteArrayInputStream(inputString.getBytes(StandardCharsets.UTF_8));

        IoAdapter ioAdapter = new IoAdapterImpl(){
            @Override
            protected Flux<String> readFrom(InputStream source) {
                return Flux.just("First line", "Second line").delayElements(Duration.ofSeconds(1));
            }
        };

        StepVerifier.withVirtualTime(() -> ioAdapter.read(source, 2, Duration.ofSeconds(2)))
                .expectSubscription()
                .thenAwait(Duration.ofSeconds(2))
                .expectNext("First line")
                .expectComplete()
                .verify();
    }


    @Test
    void giveOutputStreamAndResult_whenInvokeWrite_shouldWriteToDest() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String inputString = "Hello, Reactor!";
        Mono<String> inputMono = Mono.just(inputString);

        IoAdapter ioAdapter = new IoAdapterImpl();
        Mono<String> resultMono = ioAdapter.write(outputStream, inputMono);

        StepVerifier.create(resultMono)
                .expectNext("Done")
                .verifyComplete();

        String outputString = outputStream.toString(StandardCharsets.UTF_8);
        assertEquals(inputString, outputString);
    }
}
