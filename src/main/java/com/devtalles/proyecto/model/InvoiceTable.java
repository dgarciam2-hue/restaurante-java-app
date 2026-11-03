package com.devtalles.proyecto.model;

import com.devtalles.proyecto.exceptions.InvalidObjectException;
import com.devtalles.proyecto.utils.Validates;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class InvoiceTable extends Invoice{
    public static final double SUGGESTED_TIP = 0.1;
    private int numberTable;
    private TipType tipType;
    private double tipAmount;

    public InvoiceTable(Order order, LocalDateTime issueDate, double subtotal, double bestDiscount, int numberTable, TipType tipType, double tipAmount) throws InvalidObjectException {
        super(order, issueDate, subtotal, bestDiscount);
        this.numberTable = numberTable;
        Validates.validateObject(tipType, "El tipo de propina no puede ser nulo");
        this.tipType = tipType;
        this.tipAmount = tipAmount;
    }

    @Override
    public List<String> getDestinationData() {
        return List.of(String.valueOf(this.numberTable));
    }
}
