package com.devtalles.proyecto.controller;

import com.devtalles.proyecto.exceptions.CategoryNotFoundException;
import com.devtalles.proyecto.exceptions.InvalidCategoryException;
import com.devtalles.proyecto.exceptions.InvalidTextException;
import com.devtalles.proyecto.model.Category;
import com.devtalles.proyecto.model.User;
import com.devtalles.proyecto.service.CategoryService;
import com.devtalles.proyecto.service.UserService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.Setter;

import java.util.List;

public class AdminController {
    @Setter
    private UserService userService;

    private CategoryService categoryService;

    public void setCategoryService(CategoryService categoryService) {
        this.categoryService = categoryService;
        renderCategories();
    }

    @FXML private Label lblUserName;
    @FXML private Label lblUserInitial;
    @FXML private Label lblUserStatus;

    @FXML private VBox viewCategories;
    @FXML private VBox viewTables;
    @FXML private VBox viewDiscounts;
    @FXML private VBox viewInvoices;
    @FXML private TextField txtCategoryName;
    @FXML private StackPane modalOverlay;
    @FXML private VBox categoriesContainer;
    @FXML private Label lblModalTitle;
    private Category editingCategory = null;

    public void setLoggedUser(User user) {
        if(user != null && user.getUserName() != null) {
            lblUserName.setText(user.getUserName());
            String initial = user.getUserName().substring(0,1).toUpperCase();
            lblUserInitial.setText(initial);
            String status = user.getRoleUser().name();
            lblUserStatus.setText(status);
        }
    }

    @FXML
    public void handleCategories() {
        showSection(viewCategories);
        renderCategories();
    }

    @FXML
    public void handleOpenNewCategoryModal() {
        this.editingCategory = null;
        if (lblModalTitle != null) lblModalTitle.setText("Nueva Categoría");
        txtCategoryName.clear();
        showModal();
    }

    @FXML
    public void handleOpenEditCategoryModal(Category category) {
        this.editingCategory = category;
        if (lblModalTitle != null) lblModalTitle.setText("Editar Categoría");
        txtCategoryName.setText(category.getCategoryName());
        showModal();
    }

    @FXML
    public void handleCloseModal() {
        modalOverlay.setVisible(false);
        modalOverlay.setManaged(false);
    }

    @FXML
    public void handleAddCategory() {
        String categoryName = txtCategoryName.getText();

        try {
            if(editingCategory == null) {
                Category newCategory = categoryService.addCategory(categoryName);
                showAlert(Alert.AlertType.INFORMATION, "Registro Exitoso",
                        "Categoría registrada: " + newCategory.getCategoryName());
            }
            else {
                categoryService.updateCategory(editingCategory.getCategoryName(), categoryName);
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Categoría actualizada correctamente.");
            }

            renderCategories();
            handleCloseModal();

        } catch (InvalidTextException | InvalidCategoryException | CategoryNotFoundException e) {
            showAlert(Alert.AlertType.WARNING, "Error en registro", e.getMessage());
        }
    }

    public void handleDeleteCategory(Category category) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar eliminación");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("¿Estás seguro de que deseas eliminar la categoría '" + category.getCategoryName() + "'?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    categoryService.deleteCategoryName(category.getCategoryName());
                    showAlert(Alert.AlertType.INFORMATION, "Éxito", "Categoría eliminada.");
                    renderCategories();
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                }
            }
        });
    }

    public void renderCategories() {
        categoriesContainer.getChildren().clear();

        List<Category> categories = categoryService.getCategories();

        for (Category category: categories) {
            HBox cardNode = createCategoryCardNode(category);
            categoriesContainer.getChildren().add(cardNode);
        }
    }

    @FXML
    public void handleTables() {
        showSection(viewTables);
    }

    @FXML
    public void handleDiscounts() {
        showSection(viewDiscounts);
    }

    @FXML
    public void handleInvoices() {
        showSection(viewInvoices);
    }

    @FXML
    public void handleUser() {

    }

    private void showSection(VBox targetView) {
        hideView(viewCategories);
        hideView(viewTables);
        hideView(viewDiscounts);
        hideView(viewInvoices);

        targetView.setVisible(true);
        targetView.setManaged(true);
    }

    private void hideView(VBox view) {
        view.setVisible(false);
        view.setManaged(false);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private HBox createCategoryCardNode(Category category) {
        HBox card = new HBox();
        card.getStyleClass().add("category-item-card");
        card.setMaxWidth(Double.MAX_VALUE);
        card.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBox = createIconBox();
        VBox infoBox = createInfoBox(category);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnEdit = new Button("✏ Editar");
        btnEdit.getStyleClass().add("btn-action-edit");

        btnEdit.setOnAction(event -> {
            event.consume();
            handleOpenEditCategoryModal(category);
        });

        Button btnDelete = new Button("🗑 Eliminar");
        btnDelete.getStyleClass().add("btn-action-delete");
        btnDelete.setOnAction(event -> {
            event.consume();
            handleDeleteCategory(category);
        });

        HBox actionsBox = new HBox(8.0, btnEdit, btnDelete);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(iconBox, infoBox, spacer, actionsBox);
        return card;
    }

    private StackPane createIconBox() {
        StackPane iconBox = new StackPane();
        iconBox.getStyleClass().add("category-icon-box");

        StackPane iconSvg = new StackPane();
        iconSvg.getStyleClass().add("icon-tag-orange");

        iconBox.getChildren().add(iconSvg);
        return iconBox;
    }

    private VBox createInfoBox(Category category) {
        VBox infoBox = new VBox(2.0);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label(category.getCategoryName());
        lblTitle.getStyleClass().add("category-title");

        int count = getProductCount(category);
        String text = count + (count == 1 ? " producto" : " productos");

        Label lblSubtitle = new Label(text);
        lblSubtitle.getStyleClass().add("category-subtitle");

        infoBox.getChildren().addAll(lblTitle, lblSubtitle);
        return infoBox;
    }

    private int getProductCount(Category category) {
        return category.getProducts() != null ? category.getProducts().size() : 0;
    }

    private void showModal() {
        modalOverlay.setVisible(true);
        modalOverlay.setManaged(true);
        txtCategoryName.requestFocus();
    }
}
