package com.devtalles.proyecto.service;

import com.devtalles.proyecto.exceptions.InvalidInvoiceException;
import com.devtalles.proyecto.exceptions.InvalidObjectException;
import com.devtalles.proyecto.interfaces.InvoiceRepository;
import com.devtalles.proyecto.model.Invoice;
import com.devtalles.proyecto.model.InvoiceDelivery;
import com.devtalles.proyecto.model.InvoiceTable;
import com.devtalles.proyecto.model.TipType;
import com.devtalles.proyecto.repository.RestaurantRepositoryService;
import com.devtalles.proyecto.utils.Validates;

public class InvoiceService implements InvoiceRepository {
    private final RestaurantRepositoryService restaurantRepositoryService;

    public InvoiceService(RestaurantRepositoryService restaurantRepositoryService) {
        this.restaurantRepositoryService = restaurantRepositoryService;
    }

    @Override
    public double calculateDiscount(double bestDiscount, double subtotalOrder) {
        return (bestDiscount/100) * subtotalOrder;
    }

    @Override
    public double calculateNetBase(double subtotal, double bestDiscount) {
        return subtotal - bestDiscount;
    }

    @Override
    public double calculateTax(double netBase) {
        return netBase * Invoice.TAX_RATE;
    }

    @Override
    public double calculateTip(double netBase, TipType tipType, double customTip) {
        if (tipType == TipType.SUGERIDA) {
            return netBase * InvoiceTable.SUGGESTED_TIP;
        } else if (tipType == TipType.PERSONALIZADA) {
            return Math.max(0, customTip);
        }
        return 0.0;
    }

    @Override
    public double calculateTotalInvoice(Invoice invoice) throws InvalidObjectException {
        Validates.validateObject(invoice, "La factura no puede ser nula");
        Validates.validateObject(invoice.getOrder(), "El pedido asociado a la factura no puede ser nulo");

        double netBase = calculateNetBase(invoice.getSubtotal(), invoice.getBestDiscount());
        double tax = calculateTax(netBase);
        double additionalValue = 0.0;

        if (invoice instanceof InvoiceTable invoiceTable) {
            additionalValue = invoiceTable.getTipAmount();
        } else if (invoice instanceof InvoiceDelivery) {
            additionalValue = invoice.getOrder().calculateAdditionalValue();
        }

        return netBase + tax + additionalValue;
    }

    @Override
    public void addInvoice(Invoice newInvoice) throws InvalidObjectException, InvalidInvoiceException {
        Validates.validateObject(newInvoice, "La factura no puede ser nula.");
        Validates.validateObject(newInvoice.getOrder(), "El pedido asociado a la factura no puede ser nulo.");

        if (newInvoice.getSubtotal() < 0) {
            throw new InvalidInvoiceException("El subtotal de la factura no puede ser negativo.");
        }

        restaurantRepositoryService.getInvoices().add(newInvoice);
        restaurantRepositoryService.saveChanges();
    }
}
