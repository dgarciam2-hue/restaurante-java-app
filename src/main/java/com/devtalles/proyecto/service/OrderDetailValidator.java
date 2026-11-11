package com.devtalles.proyecto.service;

import com.devtalles.proyecto.exceptions.InvalidOrderDetailException;
import com.devtalles.proyecto.exceptions.InvalidProductException;
import com.devtalles.proyecto.model.OrderDetail;
import com.devtalles.proyecto.model.Product;

public class OrderDetailValidator {

    public static void validate(Product product, int amount) throws InvalidOrderDetailException, InvalidProductException {
        if(amount <= 0) {
            throw new InvalidOrderDetailException("La cantidad debe ser de al menos uno");
        }

        if(amount > product.getStockProduct()) {
            throw new InvalidOrderDetailException("La cantidad no puede ser mayor al stock del producto");
        }

        if(amount == product.getStockProduct()) {
            product.setAvailabilityProduct(false);
        }
    }
}
