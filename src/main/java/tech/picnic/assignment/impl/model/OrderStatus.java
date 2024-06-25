package tech.picnic.assignment.impl.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderStatus {
    CREATED,
    DELIVERED,
    CANCELLED,
    ;

    @JsonValue
    public String toLowerCase() {
        return name().toLowerCase();
    }
}
