package tech.picnic.assignment.impl.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import tech.picnic.assignment.impl.ObjectMapperFactory;
import tech.picnic.assignment.impl.io.IoAdapter;
import tech.picnic.assignment.impl.io.IoAdapterImpl;
import tech.picnic.assignment.impl.model.Delivery;
import tech.picnic.assignment.impl.model.DeliveryStatus;
import tech.picnic.assignment.impl.model.Order;
import tech.picnic.assignment.impl.model.OrderStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

class OrderStreamProcessorImplTest {
    private OrderStreamProcessorImpl orderStreamProcessor;

    @BeforeEach
    void setup() {
        IoAdapter ioAdapter = new IoAdapterImpl();
        ObjectMapper objectMapper = ObjectMapperFactory.getOrCreateObjectMapper();
        orderStreamProcessor = new OrderStreamProcessorImpl(ioAdapter, objectMapper, 100, Duration.ofSeconds(100));
    }

    @Test
    void givenOrder_whenFilterWithStatus_shouldRetainValidStatus() {
        Flux<Order> orderFlux = Flux.just(
                new Order("id1", OrderStatus.CREATED, null, null),
                new Order("id2", OrderStatus.CANCELLED, null, null),
                new Order("id3", OrderStatus.DELIVERED, null, null)
        );

        StepVerifier.create(orderStreamProcessor.filterWithStatus(orderFlux, Set.of(OrderStatus.DELIVERED)))
                .expectNextMatches(order -> order.orderStatus() == OrderStatus.DELIVERED)
                .expectComplete()
                .verify();
    }

    @Test
    void givenFluxOfString_deserialize_returnFluxOfOrders() {
        Flux<String> stringFlux = Flux.just(
                "{\"order_id\":\"1234567892\",\"order_status\":\"delivered\",\"delivery\":{\"delivery_id\":\"j93jf923jf23jg9f\",\"delivery_time\":\"2022-03-20T08:15:00Z\"},\"amount\": 4295}\n"
        );
        StepVerifier.create(orderStreamProcessor.deserialize(stringFlux))
                .expectNextMatches(order ->
                        "1234567892".equals(order.orderId()) &&
                                OrderStatus.DELIVERED == order.orderStatus() &&
                                4295 == order.amount() &&
                                "j93jf923jf23jg9f".equals(order.delivery().deliveryId()) &&
                                Instant.parse("2022-03-20T08:15:00Z").equals(order.delivery().deliveryTime())
                )
                .expectComplete()
                .verify();

    }

    @Test
    void givenFluxOfDeliveries_serialize_returnMonoOfString() {
        Order order = new Order("456", null, null, 100);
        Flux<Delivery> deliveryFlux = Flux.just(
                new Delivery("124",
                        Instant.parse("2022-03-20T08:15:00Z"),
                        DeliveryStatus.DELIVERED,
                        List.of(order),
                        100)
        );

        String expectedValue = "[ {\n" +
                "  \"delivery_id\" : \"124\",\n" +
                "  \"delivery_time\" : \"2022-03-20T08:15:00Z\",\n" +
                "  \"delivery_status\" : \"delivered\",\n" +
                "  \"orders\" : [ {\n" +
                "    \"order_id\" : \"456\",\n" +
                "    \"amount\" : 100\n" +
                "  } ],\n" +
                "  \"total_amount\" : 100\n" +
                "} ]";

        StepVerifier.create(orderStreamProcessor.serialize(deliveryFlux))
                .expectNextMatches(s -> {
                            try {
                                JSONAssert.assertEquals(expectedValue, s, JSONCompareMode.STRICT);
                                return true;
                            } catch (JSONException e) {
                                return false;
                            }
                        }
                )
                .expectComplete()
                .verify();
    }


    @Test
    void givenFluxOfOrder_whenGroupByDeliver_shouldReturnSortedDeliveries() {
        Flux<Order> orderFlux = Flux.just(
                new Order("1234567890",
                        OrderStatus.DELIVERED,
                        new Delivery("d923jd29j91d1gh6",
                                Instant.parse("2022-05-20T11:50:48Z"),
                                null,
                                null,
                                null),
                        6477),
                new Order("1234567891",
                        OrderStatus.DELIVERED,
                        new Delivery("d923jd29j91d1gh6",
                                Instant.parse("2022-05-20T11:50:48Z"),
                                null,
                                null,
                                null),
                        249),
                new Order("1234567892",
                        OrderStatus.DELIVERED,
                        new Delivery("j93jf923jf23jg9f",
                                Instant.parse("2022-03-20T08:15:00Z"),
                                null,
                                null,
                                null),
                        4295)
        );

        StepVerifier.create(orderStreamProcessor.groupByDeliveryAndSort(orderFlux))
                .expectNextMatches(delivery ->{
                                System.out.println(delivery);
                        return "j93jf923jf23jg9f".equals(delivery.deliveryId()) &&
                                Instant.parse("2022-03-20T08:15:00Z").equals(delivery.deliveryTime()) &&
                                DeliveryStatus.DELIVERED == delivery.deliveryStatus() &&
                                "1234567892".equals(delivery.orders().getFirst().orderId()) &&
                                4295 == delivery.orders().getFirst().amount() &&
                                4295 == delivery.totalAmount();
                }
                ).expectNextMatches(delivery ->
                        "d923jd29j91d1gh6".equals(delivery.deliveryId()) &&
                                Instant.parse("2022-05-20T11:50:48Z").equals(delivery.deliveryTime()) &&
                                DeliveryStatus.DELIVERED == delivery.deliveryStatus() &&
                                "1234567891".equals(delivery.orders().getFirst().orderId()) &&
                                249 == delivery.orders().getFirst().amount() &&
                                "1234567890".equals(delivery.orders().getLast().orderId()) &&
                                6477 == delivery.orders().getLast().amount() &&
                                6726 == delivery.totalAmount()
                ).expectComplete()
                .verify();
    }
}