package com.devtalles.proyecto.interfaces;

import com.devtalles.proyecto.exceptions.InvalidObjectException;
import com.devtalles.proyecto.exceptions.InvalidOrderDetailException;
import com.devtalles.proyecto.exceptions.InvalidOrderException;
import com.devtalles.proyecto.exceptions.InvalidTextException;
import com.devtalles.proyecto.exceptions.OrderNotFoundException;
import com.devtalles.proyecto.exceptions.ProductNotFoundException;
import com.devtalles.proyecto.model.Order;
import com.devtalles.proyecto.model.OrderDetail;
import com.devtalles.proyecto.model.OrderStatus;

import java.util.List;

public interface OrderRepository {
    public void addOrder(Order order) throws InvalidObjectException, InvalidOrderException;
    public void updateOrder(List<OrderDetail> updateOrderDetails, int orderId)
            throws InvalidOrderException, OrderNotFoundException, InvalidOrderDetailException, ProductNotFoundException;
    public Order getOrderById(int idOrder) throws OrderNotFoundException, InvalidOrderException;
    public void changeOrderStatus(int idOrder, OrderStatus newStatus) throws
            OrderNotFoundException, InvalidOrderException, InvalidObjectException;
    public double calculateSubtotalOrder(Order order) throws InvalidObjectException;
    public double calculateAdditionalValue(Order order) throws InvalidObjectException;
    public boolean customerHasOpenOrder(String customerPhone) throws InvalidTextException;
    public void addOrderToWaiter(Order order, int userId) throws InvalidObjectException, InvalidOrderException;
    public List<Order> getOrders();
}
