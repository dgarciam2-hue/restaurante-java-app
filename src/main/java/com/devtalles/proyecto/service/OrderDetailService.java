package com.devtalles.proyecto.service;

import com.devtalles.proyecto.exceptions.*;
import com.devtalles.proyecto.interfaces.OrderDetailRepository;
import com.devtalles.proyecto.model.Order;
import com.devtalles.proyecto.model.OrderDetail;
import com.devtalles.proyecto.model.Product;
import com.devtalles.proyecto.repository.RestaurantRepositoryService;
import com.devtalles.proyecto.utils.Validates;

import java.util.Optional;

public class OrderDetailService implements OrderDetailRepository {

    private final RestaurantRepositoryService restaurantRepositoryService;

    public OrderDetailService(RestaurantRepositoryService restaurantRepositoryService) {
        this.restaurantRepositoryService = restaurantRepositoryService;
    }

    @Override
    public void addOrderDetail(Order order, Product product, int amount) throws
            InvalidObjectException, InvalidOrderDetailException, InvalidProductException {

        Validates.validateObject(order, "El pedido no puede ser nulo.");
        Validates.validateObject(product, "Debe agregar un producto obligatoriamente.");
        OrderDetailValidator.validate(product, amount);

        if (!product.isAvailabilityProduct()) {
            throw new InvalidOrderDetailException("El producto '" + product.getNameProduct() + "' no se encuentra disponible actualmente.");
        }

        Optional<OrderDetail> existingDetail = order.getDetails()
                .stream()
                .filter(o -> o.getProduct().getIdProduct() == product.getIdProduct())
                .findFirst();

        if (existingDetail.isPresent()) {
            OrderDetail detail = existingDetail.get();
            detail.setAmount(detail.getAmount() + amount);
        } else {
            order.getDetails().add(new OrderDetail(product, amount));
        }

        restaurantRepositoryService.saveChanges();
    }

    @Override
    public void deleteOrderDetail(Order order, Product product) throws InvalidObjectException, InvalidOrderDetailException {
        OrderDetail orderDetail = getOrderDetailByProduct(order, product);

        order.getDetails().remove(orderDetail);
        restaurantRepositoryService.saveChanges();
    }

    @Override
    public void updateOrderDetail(Order order, Product product, int newAmount) throws
            InvalidObjectException, InvalidProductException, InvalidOrderDetailException {

        OrderDetailValidator.validate(product, newAmount);

        OrderDetail orderDetail = getOrderDetailByProduct(order, product);

        orderDetail.setAmount(newAmount);
        restaurantRepositoryService.saveChanges();
    }

    @Override
    public OrderDetail getOrderDetailByProduct(Order order, Product product) throws InvalidObjectException, InvalidOrderDetailException {
        Validates.validateObject(order, "El pedido no puede ser nulo.");
        Validates.validateObject(product, "El producto no puede ser nulo.");

        return order.getDetails()
                .stream()
                .filter(d -> d.getProduct().getIdProduct() == product.getIdProduct())
                .findFirst()
                .orElseThrow(() -> new InvalidOrderDetailException(
                        "El producto '" + product.getNameProduct() + "' no pertenece a este pedido."));
    }
}
