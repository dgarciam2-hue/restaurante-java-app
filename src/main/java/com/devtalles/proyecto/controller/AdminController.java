package com.devtalles.proyecto.controller;

import com.devtalles.proyecto.exceptions.CategoryNotFoundException;
import com.devtalles.proyecto.exceptions.InvalidCategoryException;
import com.devtalles.proyecto.exceptions.InvalidTableException;
import com.devtalles.proyecto.exceptions.InvalidTextException;
import com.devtalles.proyecto.model.Category;
import com.devtalles.proyecto.model.Table;
import com.devtalles.proyecto.model.TableStatus;
import com.devtalles.proyecto.model.User;
import com.devtalles.proyecto.service.CategoryService;
import com.devtalles.proyecto.service.TableService;
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

    @Setter
    private TableService tableService;

    @FXML private Label lblUserName;
    @FXML private Label lblUserInitial;
    @FXML private Label lblUserStatus;

    @FXML private VBox viewCategories;
    @FXML private VBox viewTables;
    @FXML private VBox viewDiscounts;
    @FXML private VBox viewInvoices;

    @FXML private TextField txtCategoryName;
    @FXML public TextField txtTableNumber;

    @FXML private StackPane modalCategoryOverlay;
    @FXML public StackPane modalTableOverlay;

    @FXML private VBox categoriesContainer;
    @FXML public VBox tablesContainer;

    @FXML private Label lblCategoryModalTitle;
    @FXML private Label lblTableModalTitle;

    private Category editingCategory = null;
    private Table editingTable = null;

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
        if (lblCategoryModalTitle != null) lblCategoryModalTitle.setText("Nueva Categoría");
        txtCategoryName.clear();
        modalCategoryOverlay.setVisible(true);
        modalCategoryOverlay.setManaged(true);
        txtCategoryName.requestFocus();
    }

    @FXML
    public void handleOpenEditCategoryModal(Category category) {
        this.editingCategory = category;
        if (lblCategoryModalTitle != null) lblCategoryModalTitle.setText("Editar Categoría");
        txtCategoryName.setText(category.getCategoryName());
        modalCategoryOverlay.setVisible(true);
        modalCategoryOverlay.setManaged(true);
        txtCategoryName.requestFocus();
    }

    @FXML
    public void handleCloseCategoryModal() {
        modalCategoryOverlay.setVisible(false);
        modalCategoryOverlay.setManaged(false);
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
            handleCloseCategoryModal();

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
        renderTables();
    }

    @FXML
    public void handleOpenNewTableModal() {
        this.editingTable = null;
        if (lblTableModalTitle != null) lblTableModalTitle.setText("Nueva mesa");
        txtTableNumber.clear();
        modalTableOverlay.setManaged(true);
        modalTableOverlay.setVisible(true);
        modalTableOverlay.requestFocus();
    }

    @FXML
    public void handleCloseTableModal() {
        modalTableOverlay.setManaged(false);
        modalTableOverlay.setVisible(false);
    }

    @FXML
    public void handleAddTable(){
        String numberTable = txtTableNumber.getText().trim();

        if(numberTable.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campos incompletos", "Por favor completa todos los campos.");
        }

        try {
            int number = Integer.parseInt(numberTable);
            tableService.addTable(number);
            showAlert(Alert.AlertType.INFORMATION, "Registro Exitoso",
                    "Número de mesa registrada: " + number);

            renderTables();
            handleCloseTableModal();

        } catch (InvalidTableException e) {
            showAlert(Alert.AlertType.WARNING, "Error en registro", e.getMessage());
        }
    }

    public void handleDeleteTable(Table table) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar eliminación");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("¿Estás seguro de que deseas eliminar la mesa '" + table.getNumberTable() + "'?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    tableService.deleteTable(table.getNumberTable());
                    showAlert(Alert.AlertType.INFORMATION, "Éxito", "Mesa eliminada.");
                    renderTables();
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                }
            }
        });
    }

    public void renderTables() {
        tablesContainer.getChildren().clear();

        List<Table> tables = tableService.getTables();

        for (Table table: tables) {
            HBox cardNode = createTableCardNode(table);
            tablesContainer.getChildren().add(cardNode);
        }
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

        StackPane iconBox = createIconCategoryBox();
        VBox infoBox = createInfoCategoryBox(category);

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

    private HBox createTableCardNode(Table table) {
        HBox card = new HBox();
        card.getStyleClass().add("category-item-card");
        card.setMaxWidth(Double.MAX_VALUE);
        card.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBox = createIconTableBox();
        VBox infoBox = createInfoTableBox(table);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnDelete = new Button("🗑 Eliminar");
        btnDelete.getStyleClass().add("btn-action-delete");
        btnDelete.setOnAction(event -> {
            event.consume();
            handleDeleteTable(table);
        });

        HBox actionsBox = new HBox(8.0, btnDelete);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(iconBox, infoBox, spacer, actionsBox);
        return card;
    }

    private StackPane createIconCategoryBox() {
        StackPane iconBox = new StackPane();
        iconBox.getStyleClass().add("category-icon-box");

        StackPane iconSvg = new StackPane();
        iconSvg.getStyleClass().add("icon-tag-orange");

        iconBox.getChildren().add(iconSvg);
        return iconBox;
    }

    private StackPane createIconTableBox() {
        StackPane iconBox = new StackPane();
        iconBox.getStyleClass().add("category-icon-box");

        StackPane iconSvg = new StackPane();
        iconSvg.getStyleClass().add("icon-table-orange");

        iconBox.getChildren().add(iconSvg);
        return iconBox;
    }

    private VBox createInfoCategoryBox(Category category) {
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

    private VBox createInfoTableBox(Table table) {
        VBox infoBox = new VBox(4.0);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label("Mesa " + table.getNumberTable());
        lblTitle.getStyleClass().add("category-title");

        StackPane statusDot = new StackPane();
        statusDot.setMinSize(8, 8);
        statusDot.setMaxSize(8, 8);

        Label lblStatus = new Label();
        lblStatus.getStyleClass().add("category-subtitle");

        TableStatus status = table.getTableStatus();

        if (status != null) {
            switch (status) {
                case LIBRE -> {
                    statusDot.setStyle("-fx-background-color: #22c55e; -fx-background-radius: 50%;"); // Verde
                    lblStatus.setText("LIBRE");
                    lblStatus.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold; -fx-font-size: 12px;");
                }
                case OCUPADA -> {
                    statusDot.setStyle("-fx-background-color: #ef4444; -fx-background-radius: 50%;"); // Rojo
                    lblStatus.setText("OCUPADA");
                    lblStatus.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 12px;");
                }
                case EN_FACTURACIÓN -> { // Ajusta el nombre según la constante exacta en tu Enum TableStatus
                    statusDot.setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 50%;"); // Azul
                    lblStatus.setText("EN FACTURACIÓN");
                    lblStatus.setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold; -fx-font-size: 12px;");
                }
            }
        }

        HBox statusContainer = new HBox(6.0, statusDot, lblStatus);
        statusContainer.setAlignment(Pos.CENTER_LEFT);

        infoBox.getChildren().addAll(lblTitle, statusContainer);
        return infoBox;
    }

    private int getProductCount(Category category) {
        return category.getProducts() != null ? category.getProducts().size() : 0;
    }
}
