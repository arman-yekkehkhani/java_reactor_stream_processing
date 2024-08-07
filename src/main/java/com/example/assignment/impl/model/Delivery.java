package com.example.assignment.impl.model;


import java.time.Instant;
import java.util.List;

public record Delivery(
    String deliveryId,
    Instant deliveryTime,
    DeliveryStatus deliveryStatus,
    List<Order>orders,
    Integer totalAmount
) {
}
