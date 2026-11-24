package com.devtalles.proyecto.persistence;

import com.devtalles.proyecto.model.DeliveryOrder;
import com.devtalles.proyecto.model.Order;
import com.devtalles.proyecto.model.Restaurant;
import com.devtalles.proyecto.model.TableOrder;
import com.google.gson.*;

import java.io.*;
import java.util.List;

public class RestaurantPersistence {
    private static final String FILE_PATH = "restaurante.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting()
            .registerTypeAdapter(Order.class, (JsonDeserializer<Order>) (json, typeOfT, context) -> {
                JsonObject obj = json.getAsJsonObject();

                if(obj.has("table") && !obj.get("table").isJsonNull()) {
                    return context.deserialize(json, TableOrder.class);
                }

                if ((obj.has("customer") && !obj.get("customer").isJsonNull())
                        || (obj.has("deliveryAddress") && !obj.get("deliveryAddress").isJsonNull())) {
                    return context.deserialize(json, DeliveryOrder.class);
                }

                throw new JsonParseException("No se pudo determinar el tipo de pedido (Order) a partir del JSON: " + json);
            })
            .create();

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
