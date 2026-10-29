package com.devtalles.proyecto.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderDetail {
    private Product product;
    private int amount;
}
