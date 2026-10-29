package com.devtalles.proyecto.persistence;

import com.devtalles.proyecto.model.Restaurant;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.List;

public class RestaurantPersistence {
    private static final String FILE_PATH = "restaurante.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void saveRestaurant(Restaurant restaurant) {
        try (Writer writer = new FileWriter(FILE_PATH)){
            gson.toJson(restaurant, writer);
        } catch (IOException e) {
            System.out.println("Error al guardar JSON: " + e.getMessage());
        }
    }

    public static Restaurant loadRestaurant() {
        File file = new File(FILE_PATH);
        if(!file.exists()) {
            return new Restaurant();
        }
        try(Reader reader = new FileReader(FILE_PATH)) {
            Restaurant restaurant = gson.fromJson(reader, Restaurant.class);
            return (restaurant != null) ? restaurant : new Restaurant();
        } catch (IOException e) {
            System.out.println("Error al cargar JSON: " + e.getMessage());
            return new Restaurant();
        }
    }
}
