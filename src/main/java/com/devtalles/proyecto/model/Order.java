package com.devtalles.proyecto.model;

import com.devtalles.proyecto.exceptions.InvalidObjectException;
import com.devtalles.proyecto.exceptions.InvalidOrderException;
import com.devtalles.proyecto.utils.Validates;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public abstract class Order {
    protected int orderId;
    protected LocalDateTime date;
    protected List<OrderDetail> details;
    protected OrderStatus orderStatus;

    public Order(int orderId, LocalDateTime date, List<OrderDetail> details, OrderStatus orderStatus) throws InvalidObjectException, InvalidOrderException {
        this.orderId = orderId;

        Validates.validateObject(date, "La fecha no puede ser nula");
        if(details.isEmpty()) {
            throw new InvalidOrderException("La lista de productos no puede estar vacía");
        }
        Validates.validateObject(orderStatus, "El estado del pedido no puede ser nulo");

        this.date = date;
        this.details = details;
        this.orderStatus = orderStatus;
    }

    public abstract double calculateAdditionalValue();
}
