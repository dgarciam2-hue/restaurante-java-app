package com.devtalles.proyecto.interfaces;

import com.devtalles.proyecto.exceptions.InvalidObjectException;
import com.devtalles.proyecto.exceptions.InvalidOrderException;
import com.devtalles.proyecto.exceptions.OrderNotFoundException;
import com.devtalles.proyecto.model.Order;
import com.devtalles.proyecto.model.OrderStatus;

public interface OrderRepository {
    public void addOrder(Order order) throws InvalidObjectException, InvalidOrderException;
    public Order getOrderById(int idOrder) throws OrderNotFoundException, InvalidOrderException;
    public void changeOrderStatus(int idOrder, OrderStatus newStatus) throws
            OrderNotFoundException, InvalidOrderException, InvalidObjectException;
    public double calculateSubtotalOrder(Order order) throws InvalidObjectException;
    public double calculateAdditionalValue(Order order) throws InvalidObjectException;
}
