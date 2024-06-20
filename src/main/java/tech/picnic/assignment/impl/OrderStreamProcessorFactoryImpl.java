package tech.picnic.assignment.impl;

import com.google.auto.service.AutoService;
import java.time.Duration;
import tech.picnic.assignment.api.OrderStreamProcessorFactory;
import tech.picnic.assignment.api.OrderStreamProcessor;

@AutoService(OrderStreamProcessorFactory.class)
public final class OrderStreamProcessorFactoryImpl implements OrderStreamProcessorFactory {
    @Override
    public OrderStreamProcessor createProcessor(int maxOrders, Duration maxTime) {
        throw new UnsupportedOperationException(
                "Please implement me; see README.md for the specification");
    }
}
