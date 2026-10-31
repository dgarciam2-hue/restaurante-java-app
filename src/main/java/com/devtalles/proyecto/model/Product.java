package com.devtalles.proyecto.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Product {
    private String nameProduct;
    private String descriptionProduct;
    private double priceProduct;
    private boolean availabilityProduct;
    private int idProduct;
    private int stockProduct;
}
