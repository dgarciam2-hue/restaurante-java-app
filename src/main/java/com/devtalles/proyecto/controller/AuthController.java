package com.devtalles.proyecto.controller;

import com.devtalles.proyecto.exceptions.InvalidTextException;
import com.devtalles.proyecto.exceptions.UserAlreadyExistsException;
import com.devtalles.proyecto.model.RoleUser;
import com.devtalles.proyecto.model.User;
import com.devtalles.proyecto.service.*;
import com.devtalles.proyecto.utils.RestaurantAlert;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Setter;

import java.io.IOException;
import java.util.Optional;

public class AuthController {
    @FXML private VBox loginBox;
    @FXML private VBox registerBox;

    @FXML private TextField txtLoginUserName;
    @FXML private TextField txtLoginEmail;

    @FXML private TextField txtRegisterUserName;
    @FXML private TextField txtRegisterEmail;

    @FXML private Button btnTabLogin;
    @FXML private Button btnTabRegister;

    @Setter
    private UserService userService;
    @Setter
    private CategoryService categoryService;
    @Setter
    private ProductService productService;
    @Setter
    private TableService tableService;
    @Setter
    private DiscountService discountService;
    @Setter
    private CustomerService customerService;
    @Setter
    private OrderDetailService orderDetailService;
    @Setter
    private OrderService orderService;

    @FXML
    public void showRegisterForm() {
        loginBox.setVisible(false);
        loginBox.setManaged(false);

        registerBox.setVisible(true);
        registerBox.setManaged(true);

        btnTabRegister.getStyleClass().removeAll("tab-button-inactive");
        btnTabRegister.getStyleClass().add("tab-button-active");

        btnTabLogin.getStyleClass().removeAll("tab-button-active");
        btnTabLogin.getStyleClass().add("tab-button-inactive");
    }

    @FXML
    public void showLoginForm() {
        registerBox.setVisible(false);
        registerBox.setManaged(false);

        loginBox.setVisible(true);
        loginBox.setManaged(true);

        btnTabLogin.getStyleClass().removeAll("tab-button-inactive");
        btnTabLogin.getStyleClass().add("tab-button-active");

        btnTabRegister.getStyleClass().removeAll("tab-button-active");
        btnTabRegister.getStyleClass().add("tab-button-inactive");
    }

    @FXML
    public void handleLogin() {
        String userName = txtLoginUserName.getText();
        String email = txtLoginEmail.getText();

        Optional<User> userOptional = userService.login(userName, email);

        if(userOptional.isPresent()) {
            User user = userOptional.get();
            if(user.getRoleUser() == RoleUser.ADMINISTRADOR) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AdminView.fxml"));
                    Parent root = loader.load();

                    AdminController adminController = loader.getController();
                    adminController.setUserService(this.userService);
                    adminController.setCategoryService(this.categoryService);
                    adminController.setProductService(this.productService);
                    adminController.setTableService(this.tableService);
                    adminController.setDiscountService(this.discountService);
                    adminController.setCustomerService(this.customerService);
                    adminController.setOrderDetailService(this.orderDetailService);
                    adminController.setOrderService(this.orderService);
                    adminController.setLoggedUser(user);

                    Stage currentStage = (Stage) txtLoginUserName.getScene().getWindow();
                    currentStage.getScene().setRoot(root);
                    currentStage.setTitle("Sazón - Panel de Administración");

                } catch (IOException e) {
                    RestaurantAlert.showError(txtLoginUserName.getScene().getWindow(), "Error", "No se pudo cargar la vista de administrador", "Aceptar");
                }
            }
            else {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/WaiterView.fxml"));
                    Parent root = loader.load();

                    WaiterController waiterController = loader.getController();
                    waiterController.setUserService(this.userService);
                    waiterController.setCategoryService(this.categoryService);
                    waiterController.setProductService(this.productService);
                    waiterController.setTableService(this.tableService);
                    waiterController.setDiscountService(this.discountService);
                    waiterController.setCustomerService(this.customerService);
                    waiterController.setOrderDetailService(this.orderDetailService);
                    waiterController.setOrderService(this.orderService);
                    waiterController.setLoggedUser(user);

                    Stage currentStage = (Stage) txtLoginUserName.getScene().getWindow();
                    currentStage.getScene().setRoot(root);
                    currentStage.setTitle("Sazón - Panel de Meseros");
                } catch (IOException e) {
                    RestaurantAlert.showError(txtLoginUserName.getScene().getWindow(), "Error", "No se pudo cargar la vista de mesero", "Aceptar");
                }
            }
        }
        else {
            RestaurantAlert.showError(txtLoginUserName.getScene().getWindow(), "Error de Inicio de sesión",
                    "Credenciales incorrectas o usuario no registrado", "Aceptar");
        }
    }

    @FXML
    public void handleRegister() {
        String userName = txtRegisterUserName.getText();
        String email = txtRegisterEmail.getText();

        try {
            User newUser = userService.registerUser(userName, email);
            RestaurantAlert.showSuccess(txtLoginUserName.getScene().getWindow(), "¡Cuenta creada con éxito!",
                    "Ya puedes iniciar sesión con tu usuario " + newUser.getUserName() +
                            " y tu correo " + newUser.getEmail() + ".", "Aceptar");

            showLoginForm();
            txtLoginUserName.setText(userName);
            txtLoginEmail.setText(email);

        } catch (InvalidTextException | UserAlreadyExistsException | IllegalArgumentException e) {
            RestaurantAlert.showError(txtLoginUserName.getScene().getWindow(), "Error de Registro", e.getMessage(), "Aceptar");
        }
    }
}
