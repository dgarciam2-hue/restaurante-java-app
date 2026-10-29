package com.devtalles.proyecto.model;

import lombok.Data;

@Data
public class OrderTable extends Order{
    private Table table;

    public OrderTable(int orderId, Table table) {
        super(orderId);
        this.table = table;
    }
}
