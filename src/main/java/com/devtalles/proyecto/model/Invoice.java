package com.devtalles.proyecto.model;

import com.devtalles.proyecto.exceptions.InvalidObjectException;
import com.devtalles.proyecto.utils.Validates;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public abstract class Invoice {
    public static final double TAX_RATE = 0.08;
    protected Order order;
    protected LocalDateTime issueDate;
    protected double subtotal;
    protected double bestDiscount;

    public Invoice(Order order, LocalDateTime issueDate, double subtotal, double bestDiscount) throws InvalidObjectException {
        Validates.validateObject(order, "El pedido no puede ser nulo");
        this.order = order;
        Validates.validateObject(issueDate, "La fecha de emisión no puede ser nula");
        this.issueDate = issueDate;
        this.subtotal = subtotal;
        this.bestDiscount = bestDiscount;
    }

    public abstract List<String> getDestinationData();
}
