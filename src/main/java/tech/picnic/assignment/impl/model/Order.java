package tech.picnic.assignment.impl.model;

public record Order(String orderId, OrderStatus orderStatus, Delivery delivery, Integer amount) {
}
