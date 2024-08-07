package com.example.assignment.impl.io;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.*;
import java.time.Duration;

public class IoAdapterImpl implements IoAdapter {

    @Override
    public Flux<String> read(InputStream source, int maxLines, Duration maxTime) {
        return readFrom(source)
                .filter(s -> !s.isBlank())
                .take(maxLines)
                .take(maxTime);
    }

    protected Flux<String> readFrom(InputStream source) {
        return Flux.<String>create(sink -> {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(source));
            try {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sink.next(line);
                }
                sink.complete();
            } catch (IOException e) {
                sink.error(e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<String> write(OutputStream outputStream, Mono<String> mono) {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        return mono.publishOn(Schedulers.boundedElastic()).handle((it, sink) -> {
            try {
                writer.write(it);
                writer.flush();
                sink.next("Done");
            } catch (IOException e) {
                sink.error(e);
            }
        });
    }
}
