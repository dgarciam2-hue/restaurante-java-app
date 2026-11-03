package com.devtalles.proyecto.service;

import com.devtalles.proyecto.exceptions.InvalidObjectException;
import com.devtalles.proyecto.exceptions.InvalidOrderException;
import com.devtalles.proyecto.exceptions.OrderNotFoundException;
import com.devtalles.proyecto.interfaces.OrderRepository;
import com.devtalles.proyecto.model.Order;
import com.devtalles.proyecto.model.OrderStatus;
import com.devtalles.proyecto.repository.RestaurantRepositoryService;
import com.devtalles.proyecto.utils.Validates;

public class OrderService implements OrderRepository {
    private final RestaurantRepositoryService restaurantRepositoryService;

    public OrderService(RestaurantRepositoryService restaurantRepositoryService) {
        this.restaurantRepositoryService = restaurantRepositoryService;
    }

    @Override
    public void addOrder(Order newOrder) throws InvalidObjectException, InvalidOrderException {
        Validates.validateObject(newOrder, "El pedido no puede ser nulo");

        if(newOrder.getOrderId() <= 0) {
            throw new InvalidOrderException("El ID del pedido debe ser mayor a cero");
        }

        boolean idExists = restaurantRepositoryService.getOrders()
                .stream()
                .anyMatch(o -> o.getOrderId() == newOrder.getOrderId());

        if(idExists) {
            throw new InvalidOrderException("Ya existe un pedido con el ID " + newOrder.getOrderId());
        }

        restaurantRepositoryService.getOrders().add(newOrder);
        restaurantRepositoryService.saveChanges();
    }

    @Override
    public Order getOrderById(int idOrder) throws OrderNotFoundException, InvalidOrderException {
        if (idOrder <= 0) {
            throw new InvalidOrderException("El ID del pedido debe ser mayor a cero.");
        }

        return restaurantRepositoryService.getOrders()
                .stream()
                .filter(o -> o.getOrderId() == idOrder)
                .findFirst()
                .orElseThrow(() -> new OrderNotFoundException("El pedido con ID " + idOrder + " no existe."));
    }

    @Override
    public void changeOrderStatus(int idOrder, OrderStatus newStatus) throws
            OrderNotFoundException, InvalidOrderException, InvalidObjectException {

        Validates.validateObject(newStatus, "El nuevo estado no puede ser nulo.");

        Order order = getOrderById(idOrder);
        order.setOrderStatus(newStatus);

        restaurantRepositoryService.saveChanges();
    }

    @Override
    public double calculateSubtotalOrder(Order order) throws InvalidObjectException {
        Validates.validateObject(order, "El pedido no puede ser nulo");

        return order.getDetails()
                .stream()
                .mapToDouble(d -> d.getProduct().getPriceProduct() * d.getAmount())
                .sum();
    }

    @Override
    public double calculateAdditionalValue(Order order) throws InvalidObjectException {
        Validates.validateObject(order, "El pedido no puede ser nulo");

        return order.calculateAdditionalValue();
    }
}
