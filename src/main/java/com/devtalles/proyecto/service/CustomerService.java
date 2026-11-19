package com.devtalles.proyecto.service;

import com.devtalles.proyecto.exceptions.CustomerNotFoundException;
import com.devtalles.proyecto.exceptions.InvalidCustomerException;
import com.devtalles.proyecto.exceptions.InvalidTextException;
import com.devtalles.proyecto.interfaces.CustomerRepository;
import com.devtalles.proyecto.model.Customer;
import com.devtalles.proyecto.repository.RestaurantRepositoryService;
import com.devtalles.proyecto.utils.Validates;

import java.util.Optional;

public class CustomerService implements CustomerRepository {
    private final RestaurantRepositoryService restaurantRepositoryService;

    public CustomerService(RestaurantRepositoryService restaurantRepositoryService) {
        this.restaurantRepositoryService = restaurantRepositoryService;
    }

    @Override
    public void addCustomer(String customerName, String customerAddress, String customerPhone) throws InvalidTextException, InvalidCustomerException {
        Validates.validateText(customerName, "El cliente debe tener un nombre");
        Validates.validateText(customerAddress, "El cliente debe tener una dirección de residencia");
        Validates.validateText(customerPhone, "El cliente debe tener un teléfono de contacto");

        String formattedName = customerName.trim();
        String formattedAddress = customerAddress.trim();
        String formattedPhone = customerPhone.trim();

        boolean existsCustomer = restaurantRepositoryService.getCustomers()
                .stream()
                .anyMatch(c -> c.getCustomerPhone().equalsIgnoreCase(formattedPhone));

        if(existsCustomer) {
            throw new InvalidCustomerException("El cliente con teléfono '" + formattedPhone + "' ya existe en el sistema");
        }

        restaurantRepositoryService.getCustomers().add(new Customer(formattedName, formattedAddress, formattedPhone));
        restaurantRepositoryService.saveChanges();
    }

    @Override
    public Optional<Customer> findByPhone(String customerPhone) {
        return restaurantRepositoryService.getCustomers()
                .stream()
                .filter(c -> c.getCustomerPhone().equalsIgnoreCase(customerPhone))
                .findFirst();
    }
}
