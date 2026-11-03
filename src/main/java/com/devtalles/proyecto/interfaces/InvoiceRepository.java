package com.devtalles.proyecto.interfaces;

import com.devtalles.proyecto.exceptions.InvalidInvoiceException;
import com.devtalles.proyecto.exceptions.InvalidObjectException;
import com.devtalles.proyecto.model.Invoice;
import com.devtalles.proyecto.model.TipType;

public interface InvoiceRepository {
    public double calculateDiscount(double bestDiscount, double subtotalOrder);
    public double calculateNetBase(double subtotal, double bestDiscount);
    public double calculateTax(double netBase);
    public double calculateTip(double netBase, TipType tipType, double customTip);
    public double calculateTotalInvoice(Invoice invoice) throws InvalidObjectException;
    public void addInvoice(Invoice newInvoice) throws InvalidObjectException, InvalidInvoiceException;
}
