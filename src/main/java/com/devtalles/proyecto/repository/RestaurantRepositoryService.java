package com.devtalles.proyecto.repository;

import com.devtalles.proyecto.model.Category;
import com.devtalles.proyecto.model.Restaurant;
import com.devtalles.proyecto.model.Table;
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

    public void saveChanges() {
        RestaurantPersistence.saveRestaurant(restaurant);
    }
}
