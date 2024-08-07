package com.example.assignment.impl.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import com.example.assignment.api.OrderStreamProcessor;
import com.example.assignment.impl.io.IoAdapter;
import com.example.assignment.impl.model.Delivery;
import com.example.assignment.impl.model.DeliveryStatus;
import com.example.assignment.impl.model.Order;
import com.example.assignment.impl.model.OrderStatus;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class OrderStreamProcessorImpl implements OrderStreamProcessor {
    IoAdapter ioAdapter;
    ObjectMapper objectMapper;
    int maxOrders;
    Duration maxTime;

    public OrderStreamProcessorImpl(IoAdapter ioAdapter, ObjectMapper objectMapper, int maxOrders, Duration maxTime) {
        this.ioAdapter = ioAdapter;
        this.objectMapper = objectMapper;
        this.maxOrders = maxOrders;
        this.maxTime = maxTime;
    }

    @Override
    public void process(InputStream source, OutputStream sink) {
        Flux<String> inputFlux = ioAdapter.read(source, maxOrders, maxTime);
        Flux<Order> orders = deserialize(inputFlux);
        Flux<Order> filteredOrders = filterWithStatus(orders, Set.of(OrderStatus.CANCELLED, OrderStatus.DELIVERED));
        Flux<Delivery> sortedDeliveries = groupByDeliveryAndSort(filteredOrders);
        Mono<String> serialized = serialize(sortedDeliveries);
        Mono<String> result = ioAdapter.write(sink, serialized);
        result.block();
    }

    Flux<Order> deserialize(Flux<String> stringFlux) {
        return stringFlux.handle((it, sink) -> {
            try {
                sink.next(objectMapper.readValue(it, Order.class));
            } catch (JsonProcessingException e) {
                sink.error(new RuntimeException(e));
            }
        });
    }

    Flux<Order> filterWithStatus(Flux<Order> orderFlux, Set<OrderStatus> statusSet) {
        return orderFlux.filter((it) -> statusSet.contains(it.orderStatus()));
    }

    Flux<Delivery> groupByDeliveryAndSort(Flux<Order> orderFlux) {
        Flux<GroupedFlux<String, Order>> groupedBy = orderFlux.groupBy(order -> order.delivery().deliveryId());
        Flux<Delivery> deliveryFlux = groupedBy.flatMap(Flux::collectList)
                .map(orderList -> {
                    boolean delivered = orderList.stream().anyMatch(order -> order.orderStatus() == OrderStatus.DELIVERED);
                    AtomicInteger totalAmount = new AtomicInteger();
                    List<Order> orders = orderList.stream()
                            .peek(order -> {
                                if (order.orderStatus() == OrderStatus.DELIVERED) {
                                    totalAmount.addAndGet(order.amount());
                                }
                            })
                            .map(order -> new Order(order.orderId(), null, null, order.amount()))
                            .sorted(Comparator.comparing(Order::orderId).reversed())
                            .collect(Collectors.toList());
                    return new Delivery(
                            orderList.getFirst().delivery().deliveryId(),
                            orderList.getFirst().delivery().deliveryTime(),
                            delivered ? DeliveryStatus.DELIVERED : DeliveryStatus.CANCELLED,
                            orders,
                            totalAmount.get() == 0 ? null : totalAmount.get()
                    );
                });
        return deliveryFlux.sort((Comparator.comparing(Delivery::deliveryTime).thenComparing(Delivery::deliveryId)));
    }

    Mono<String> serialize(Flux<Delivery> deliveryFlux) {
        return deliveryFlux.collectList().handle((l, sink) -> {
            try {
                sink.next(objectMapper.writeValueAsString(l));
            } catch (JsonProcessingException e) {
                sink.error(new RuntimeException(e));
            }
        });
    }
}
