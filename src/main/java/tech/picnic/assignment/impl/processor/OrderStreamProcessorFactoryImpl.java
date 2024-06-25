package tech.picnic.assignment.impl.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import tech.picnic.assignment.api.OrderStreamProcessor;
import tech.picnic.assignment.api.OrderStreamProcessorFactory;
import tech.picnic.assignment.impl.ObjectMapperFactory;
import tech.picnic.assignment.impl.io.IoAdapter;
import tech.picnic.assignment.impl.io.IoAdapterImpl;

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
