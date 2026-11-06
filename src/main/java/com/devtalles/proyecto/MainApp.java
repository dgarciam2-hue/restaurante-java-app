package com.devtalles.proyecto;

import com.devtalles.proyecto.controller.AuthController;
import com.devtalles.proyecto.repository.RestaurantRepositoryService;
import com.devtalles.proyecto.service.CategoryService;
import com.devtalles.proyecto.service.TableService;
import com.devtalles.proyecto.service.UserService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        RestaurantRepositoryService restaurantRepositoryService = new RestaurantRepositoryService();
        UserService userService = new UserService(restaurantRepositoryService);
        CategoryService categoryService = new CategoryService(restaurantRepositoryService);
        TableService tableService = new TableService(restaurantRepositoryService);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AuthView.fxml"));
        Parent root = loader.load();

        AuthController authController = loader.getController();
        authController.setUserService(userService);
        authController.setCategoryService(categoryService);
        authController.setTableService(tableService);

        Scene scene = new Scene(root);
        primaryStage.setTitle("Sazón - Sistema de Gestión de Restaurante");
        primaryStage.setScene(scene);
        scene.getStylesheets().add(getClass().getResource("/styles/global.css").toExternalForm());
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
