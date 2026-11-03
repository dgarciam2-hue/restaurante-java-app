package com.devtalles.proyecto.model;

import com.devtalles.proyecto.exceptions.InvalidObjectException;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class InvoiceDelivery extends Invoice{
    private String nameCustomer;
    private String phoneCustomer;

    public InvoiceDelivery(Order order, LocalDateTime issueDate, double subtotal, double bestDiscount, String nameCustomer, String phoneCustomer) throws InvalidObjectException {
        super(order, issueDate, subtotal, bestDiscount);
        this.nameCustomer = nameCustomer;
        this.phoneCustomer = phoneCustomer;
    }

    @Override
    public List<String> getDestinationData() {
        return List.of(this.nameCustomer, this.phoneCustomer);
    }
}
