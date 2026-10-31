package com.devtalles.proyecto.service;

import com.devtalles.proyecto.exceptions.InvalidProductException;
import com.devtalles.proyecto.model.Product;

public class ProductValidator {
    public static void validate(Product product) throws InvalidProductException {
        if(product.getPriceProduct() <= 0) {
            throw new InvalidProductException("El precio del producto debe ser mayor a cero");
        }
        if(product.getStockProduct() < 0) {
            throw new InvalidProductException("El stock no puede ser negativo");
        }
    }
}
