package com.devtalles.proyecto.interfaces;

import com.devtalles.proyecto.exceptions.CustomerNotFoundException;
import com.devtalles.proyecto.exceptions.InvalidCustomerException;
import com.devtalles.proyecto.exceptions.InvalidTextException;
import com.devtalles.proyecto.model.Customer;

public interface CustomerRepository {
    public void addCustomer(String customerName, String customerAddress, String customerPhone) throws InvalidTextException, InvalidCustomerException;
    public Customer getCustomer(String customerPhone) throws InvalidTextException, CustomerNotFoundException;
}
