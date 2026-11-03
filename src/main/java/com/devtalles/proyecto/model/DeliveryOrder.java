package com.devtalles.proyecto.model;

import com.devtalles.proyecto.exceptions.InvalidObjectException;
import com.devtalles.proyecto.exceptions.InvalidOrderException;
import com.devtalles.proyecto.exceptions.InvalidTextException;
import com.devtalles.proyecto.utils.Validates;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DeliveryOrder extends Order{
    private Customer customer;
    private String deliveryAddress;
    private int deliveryZone;

    public DeliveryOrder(int orderId, LocalDateTime date, List<OrderDetail> details, OrderStatus orderStatus, String deliveryAddress, Customer customer, int deliveryZone) throws InvalidObjectException, InvalidTextException, InvalidOrderException {
        super(orderId,date,details,orderStatus);

        Validates.validateObject(customer, "El cliente es obligatorio para un pedido a domicilio");
        Validates.validateText(deliveryAddress, "La dirección de entrega no puede estar vacía");

        this.deliveryAddress = deliveryAddress.trim();
        this.customer = customer;
        this.deliveryZone = deliveryZone;
    }

    @Override
    public double calculateAdditionalValue() {
        return switch(this.deliveryZone) {
            case 1 -> 4000.0;
            case 2 -> 6000.0;
            case 3 -> 8000.0;
            default -> 10000.0;
        };
    }
}
