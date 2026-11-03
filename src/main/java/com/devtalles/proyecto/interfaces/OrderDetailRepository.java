package com.devtalles.proyecto.interfaces;

import com.devtalles.proyecto.exceptions.*;
import com.devtalles.proyecto.model.Order;
import com.devtalles.proyecto.model.OrderDetail;
import com.devtalles.proyecto.model.Product;

public interface OrderDetailRepository {
    public void addOrderDetail(Order order, Product product, int amount) throws InvalidObjectException, InvalidOrderDetailException, InvalidOrderException, OrderNotFoundException, InvalidProductException;
    public void deleteOrderDetail(Order order, Product product) throws InvalidObjectException, InvalidOrderDetailException;
    public void updateOrderDetail(Order order, Product product, int newAmount) throws InvalidObjectException, InvalidProductException, InvalidOrderDetailException;
    public OrderDetail getOrderDetailByProduct(Order order, Product product) throws InvalidObjectException, InvalidOrderDetailException;
}
