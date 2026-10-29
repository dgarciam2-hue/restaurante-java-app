package com.devtalles.proyecto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class OrderDelivery extends Order{
    private Customer customer;
    private String deliveryAddress;
    private int deliveryZone;

    public OrderDelivery(int orderId, String deliveryAddress, Customer customer, int deliveryZone) {
        super(orderId);
        this.deliveryAddress = deliveryAddress;
        this.customer = customer;
        this.deliveryZone = deliveryZone;
    }
}
