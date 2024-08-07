package com.example.assignment.impl.model;

public record Order(String orderId, OrderStatus orderStatus, Delivery delivery, Integer amount) {
}
