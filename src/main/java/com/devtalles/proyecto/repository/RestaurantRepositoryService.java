package com.devtalles.proyecto.repository;

import com.devtalles.proyecto.model.*;
import com.devtalles.proyecto.persistence.RestaurantPersistence;
import lombok.Data;

import java.util.List;

public class RestaurantRepositoryService {
    private Restaurant restaurant;

    public RestaurantRepositoryService() {
        restaurant = RestaurantPersistence.loadRestaurant();
    }

    public List<Category> getCategories() {
        return restaurant.getCategories();
    }
    public List<Table> getTables() { return restaurant.getTables(); }
    public List<Discount> getDiscounts() { return restaurant.getDiscounts(); }
    public List<Customer> getCustomers() { return restaurant.getCustomers(); }

    public void saveChanges() {
        RestaurantPersistence.saveRestaurant(restaurant);
    }
}
