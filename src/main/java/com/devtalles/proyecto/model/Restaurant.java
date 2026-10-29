package com.devtalles.proyecto.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Restaurant {
    private List<Category> categories;
    private List<Table> tables;
    private List<Order> orders;
    private List<Customer> customers;
    private List<Discount> discounts;

    public Restaurant() {
        categories = new ArrayList<>();
        tables = new ArrayList<>();
        orders = new ArrayList<>();
        customers = new ArrayList<>();
        discounts = new ArrayList<>();
    }
}
