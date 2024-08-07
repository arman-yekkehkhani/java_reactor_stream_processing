package com.example.assignment.impl.io;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;

public interface IoAdapter {
    Flux<String> read(InputStream source, int maxLines, Duration maxTime);

    Mono<String> write(OutputStream sink, Mono<String> flux);
}
