package com.devtalles.proyecto.service;

import com.devtalles.proyecto.exceptions.*;
import com.devtalles.proyecto.interfaces.OrderRepository;
import com.devtalles.proyecto.model.*;
import com.devtalles.proyecto.repository.RestaurantRepositoryService;
import com.devtalles.proyecto.utils.Validates;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
    public void updateOrder(List<OrderDetail> updateOrderDetails, int orderId)
            throws InvalidOrderException, OrderNotFoundException, InvalidOrderDetailException, ProductNotFoundException {

        if (orderId <= 0) {
            throw new InvalidOrderException("El ID del pedido debe ser mayor a cero.");
        }

        if (updateOrderDetails == null || updateOrderDetails.isEmpty()) {
            throw new InvalidOrderException("La lista de productos del pedido no puede estar vacía.");
        }

        Order order = restaurantRepositoryService.getOrders().stream()
                .filter(o -> o.getOrderId() == orderId)
                .findFirst()
                .orElseThrow(() -> new OrderNotFoundException("El pedido con ID " + orderId + " no ha sido encontrado."));

        Map<Integer, Integer> originalQuantities = new LinkedHashMap<>();
        if (order.getDetails() != null) {
            for (OrderDetail detail : order.getDetails()) {
                if (detail != null && detail.getProduct() != null) {
                    originalQuantities.merge(detail.getProduct().getIdProduct(), detail.getAmount(), Integer::sum);
                }
            }
        }

        Map<Integer, Integer> newQuantities = new LinkedHashMap<>();
        Map<Integer, Product> submittedProducts = new LinkedHashMap<>();
        for (OrderDetail detail : updateOrderDetails) {
            if (detail == null || detail.getProduct() == null) {
                throw new InvalidOrderDetailException("Todos los detalles del pedido deben tener un producto válido.");
            }
            if (detail.getAmount() <= 0) {
                throw new InvalidOrderDetailException(
                        "La cantidad de '" + detail.getProduct().getNameProduct() + "' debe ser al menos 1.");
            }
            int productId = detail.getProduct().getIdProduct();
            newQuantities.merge(productId, detail.getAmount(), Integer::sum);
            submittedProducts.putIfAbsent(productId, detail.getProduct());
        }

        Set<Integer> allProductIds = new LinkedHashSet<>();
        allProductIds.addAll(originalQuantities.keySet());
        allProductIds.addAll(newQuantities.keySet());

        Map<Product, Integer> pendingStockUpdates = new LinkedHashMap<>();
        Map<Integer, Product> resolvedCatalogProducts = new LinkedHashMap<>();

        for (int productId : allProductIds) {
            int originalQty = originalQuantities.getOrDefault(productId, 0);
            int newQty = newQuantities.getOrDefault(productId, 0);
            int diff = newQty - originalQty;

            Optional<Product> catalogProductOpt = findProductById(productId);
            catalogProductOpt.ifPresent(p -> resolvedCatalogProducts.put(productId, p));

            if (diff > 0) {
                Product catalogProduct = catalogProductOpt.orElseThrow(() ->
                        new ProductNotFoundException("El producto con ID " + productId + " no existe en el catálogo."));
                if (diff > catalogProduct.getStockProduct()) {
                    throw new InvalidOrderDetailException(
                            "Stock insuficiente para '" + catalogProduct.getNameProduct()
                            + "'. Disponible: " + catalogProduct.getStockProduct()
                            + ", adicional requerido: " + diff + ".");
                }
                pendingStockUpdates.put(catalogProduct, catalogProduct.getStockProduct() - diff);
            } else if (diff < 0) {
                int unitsToReturn = originalQty - newQty;
                catalogProductOpt.ifPresent(catalogProduct ->
                        pendingStockUpdates.put(catalogProduct, catalogProduct.getStockProduct() + unitsToReturn));
            }
        }

        for (Map.Entry<Product, Integer> entry : pendingStockUpdates.entrySet()) {
            Product product = entry.getKey();
            int previousStock = product.getStockProduct();
            int updatedStock = entry.getValue();
            product.setStockProduct(updatedStock);
            if (updatedStock == 0) {
                product.setAvailabilityProduct(false);
            } else if (previousStock == 0) {
                product.setAvailabilityProduct(true);
            }
        }

        List<OrderDetail> consolidatedDetails = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : newQuantities.entrySet()) {
            int productId = entry.getKey();
            int finalQty = entry.getValue();
            Product productRef = resolvedCatalogProducts.getOrDefault(productId, submittedProducts.get(productId));
            consolidatedDetails.add(new OrderDetail(productRef, finalQty));
        }

        if (order.getDetails() == null) {
            order.setDetails(new ArrayList<>(consolidatedDetails));
        } else {
            order.getDetails().clear();
            order.getDetails().addAll(consolidatedDetails);
        }

        if (restaurantRepositoryService.getUsers() != null) {
            for (User user : restaurantRepositoryService.getUsers()) {
                if (user.getOrders() != null) {
                    for (int i = 0; i < user.getOrders().size(); i++) {
                        Order waiterOrder = user.getOrders().get(i);
                        if (waiterOrder != null && waiterOrder.getOrderId() == orderId) {
                            if (waiterOrder != order) {
                                if (waiterOrder.getDetails() == null) {
                                    waiterOrder.setDetails(new ArrayList<>(consolidatedDetails));
                                } else {
                                    waiterOrder.getDetails().clear();
                                    waiterOrder.getDetails().addAll(consolidatedDetails);
                                }
                            }
                            user.getOrders().set(i, order);
                        }
                    }
                }
            }
        }

        restaurantRepositoryService.saveChanges();
    }

    private Optional<Product> findProductById(int productId) {
        if (restaurantRepositoryService.getCategories() == null) {
            return Optional.empty();
        }
        return restaurantRepositoryService.getCategories()
                .stream()
                .filter(c -> c.getProducts() != null)
                .flatMap(c -> c.getProducts().stream())
                .filter(p -> p.getIdProduct() == productId)
                .findFirst();
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
    public double calculateSubtotalOrder(Order order) {
        return order.getDetails()
                .stream()
                .mapToDouble(d -> d.getProduct().getPriceProduct() * d.getAmount())
                .sum();
    }

    @Override
    public double calculateAdditionalValue(Order order) {
        return order.calculateAdditionalValue();
    }

    @Override
    public boolean customerHasOpenOrder(String customerPhone) throws InvalidTextException {
        Validates.validateText(customerPhone, "El teléfono del cliente no puede ser nulo o vacío");

        return restaurantRepositoryService.getOrders()
                .stream()
                .anyMatch(o -> o instanceof DeliveryOrder deliveryOrder
                && deliveryOrder.getCustomer().getCustomerPhone().equalsIgnoreCase(customerPhone)
                && (deliveryOrder.getOrderStatus() == OrderStatus.ABIERTO || deliveryOrder.getOrderStatus() == OrderStatus.ENTREGADO));
    }

    @Override
    public void addOrderToWaiter(Order order, int userId) throws InvalidObjectException, InvalidOrderException {
        User user = restaurantRepositoryService.getUsers()
                .stream()
                .filter(u -> u.getId() == userId)
                .findFirst()
                .orElseThrow(() -> new InvalidObjectException("El usuario con ID " + userId + " no existe."));

        if(user.getRoleUser() != RoleUser.MESERO) {
            throw new InvalidObjectException("El usuario con ID " + userId + " no es un mesero.");
        }

        user.getOrders().add(order);
        restaurantRepositoryService.saveChanges();
    }

    @Override
    public List<Order> getOrders() {
        return restaurantRepositoryService.getOrders();
    }
}
