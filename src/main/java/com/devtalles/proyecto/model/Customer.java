package com.devtalles.proyecto.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Customer {
    private String customerName;
    private String customerAddress;
    private String customerPhone;
}
