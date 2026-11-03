package com.devtalles.proyecto.model;

import com.devtalles.proyecto.exceptions.InvalidObjectException;
import com.devtalles.proyecto.exceptions.InvalidOrderException;
import com.devtalles.proyecto.utils.Validates;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TableOrder extends Order{
    private Table table;

    public TableOrder(int orderId, LocalDateTime date, List<OrderDetail> details, OrderStatus orderStatus, Table table) throws InvalidObjectException, InvalidOrderException {
        super(orderId,date,details,orderStatus);

        Validates.validateObject(table, "El pedido debe tener una mesa asociada");

        this.table = table;
    }

    @Override
    public double calculateAdditionalValue() {
        return 0.0;
    }
}
