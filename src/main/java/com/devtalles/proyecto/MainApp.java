package com.devtalles.proyecto;

import com.devtalles.proyecto.controller.AuthController;
import com.devtalles.proyecto.repository.RestaurantRepositoryService;
import com.devtalles.proyecto.service.*;
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
        ProductService productService = new ProductService(restaurantRepositoryService);
        TableService tableService = new TableService(restaurantRepositoryService);
        DiscountService discountService = new DiscountService(restaurantRepositoryService);
        CustomerService customerService = new CustomerService(restaurantRepositoryService);
        OrderDetailService orderDetailService = new OrderDetailService(restaurantRepositoryService);
        OrderService orderService = new OrderService(restaurantRepositoryService);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AuthView.fxml"));
        Parent root = loader.load();

        AuthController authController = loader.getController();
        authController.setUserService(userService);
        authController.setCategoryService(categoryService);
        authController.setProductService(productService);
        authController.setTableService(tableService);
        authController.setDiscountService(discountService);
        authController.setCustomerService(customerService);
        authController.setOrderDetailService(orderDetailService);
        authController.setOrderService(orderService);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/styles/global.css").toExternalForm());
        primaryStage.setTitle("Sazón - Sistema de Gestión de Restaurante");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(640);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }
}
