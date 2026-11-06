package com.devtalles.proyecto.controller;

import com.devtalles.proyecto.exceptions.InvalidTextException;
import com.devtalles.proyecto.exceptions.UserAlreadyExistsException;
import com.devtalles.proyecto.model.RoleUser;
import com.devtalles.proyecto.model.User;
import com.devtalles.proyecto.service.CategoryService;
import com.devtalles.proyecto.service.TableService;
import com.devtalles.proyecto.service.UserService;
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
    private TableService tableService;

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
                    adminController.setTableService(this.tableService);
                    adminController.setLoggedUser(user);

                    Stage currentStage = (Stage) txtLoginUserName.getScene().getWindow();
                    Scene newScene = new Scene(root);
                    newScene.getStylesheets().add(getClass().getResource("/styles/global.css").toExternalForm());
                    currentStage.setTitle("Sazón - Panel de Administración");
                    currentStage.setScene(newScene);
                    currentStage.centerOnScreen();

                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "No se pudo cargar la vista de administrador");
                }
            }
            else {
                //Se dirige a vista mesero
            }
        }
        else {
            showAlert(Alert.AlertType.ERROR, "Error de Inicio de sesión",
                    "Credenciales incorrectas o usuario no registrado");
        }
    }

    @FXML
    public void handleRegister() {
        String userName = txtRegisterUserName.getText();
        String email = txtRegisterEmail.getText();

        try {
            User newUser = userService.registerUser(userName, email);
            showAlert(Alert.AlertType.INFORMATION, "Registro Exitoso",
                    "Usuario registrado como " + newUser.getRoleUser() +
                            ". Ahora puedes iniciar sesión.");

            showLoginForm();
            txtLoginUserName.setText(userName);
            txtLoginEmail.setText(email);

        } catch (InvalidTextException | UserAlreadyExistsException | IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Error en registro", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
