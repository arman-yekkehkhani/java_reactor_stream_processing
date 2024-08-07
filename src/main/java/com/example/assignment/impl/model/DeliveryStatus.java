package com.example.assignment.impl.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DeliveryStatus {
    DELIVERED,
    CANCELLED,
    ;

    @JsonValue
    public String toLowerCase() {
        return name().toLowerCase();
    }
}
