package com.example.assignment.impl.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import com.example.assignment.api.OrderStreamProcessor;
import com.example.assignment.api.OrderStreamProcessorFactory;
import com.example.assignment.impl.ObjectMapperFactory;
import com.example.assignment.impl.io.IoAdapter;
import com.example.assignment.impl.io.IoAdapterImpl;

import java.time.Duration;

@AutoService(OrderStreamProcessorFactory.class)
public final class OrderStreamProcessorFactoryImpl implements OrderStreamProcessorFactory {
    private final ObjectMapper objectMapper = ObjectMapperFactory.getOrCreateObjectMapper();

    @Override
    public OrderStreamProcessor createProcessor(int maxOrders, Duration maxTime) {
        IoAdapter ioAdapter = new IoAdapterImpl();
        return new OrderStreamProcessorImpl(ioAdapter, objectMapper, maxOrders, maxTime);
    }
}
