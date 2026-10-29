package com.devtalles.proyecto.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public abstract class Order {
    protected int orderId;
    protected LocalDateTime date;
    protected List<OrderDetail> details;

    public Order(int orderId) {
        this.orderId = orderId;
    }
}
