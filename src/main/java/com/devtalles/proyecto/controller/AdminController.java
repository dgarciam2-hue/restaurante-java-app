package com.devtalles.proyecto.controller;

import com.devtalles.proyecto.exceptions.*;
import com.devtalles.proyecto.model.*;
import com.devtalles.proyecto.service.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import lombok.Setter;

import java.io.IOException;
import java.time.DayOfWeek;
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
    private ProductService productService;
    @Setter
    private TableService tableService;
    @Setter
    private DiscountService discountService;

    @FXML public Button btnLogout;

    @FXML private Label lblUserName;
    @FXML private Label lblUserInitial;
    @FXML private Label lblUserStatus;
    @FXML public Label lblDiscountTarget;
    @FXML private Label lblCategoryModalTitle;
    @FXML public Label lblDiscountModalTitle;
    @FXML public Label lblProductModalTitle;
    @FXML public Label lblContainerCategory;
    @FXML public Label lblDescriptionContainerCategory;

    @FXML private VBox viewCategories;
    @FXML private VBox viewTables;
    @FXML private VBox viewDiscounts;
    @FXML private VBox viewInvoices;
    @FXML public VBox viewProducts;

    @FXML private TextField txtCategoryName;
    @FXML public TextField txtTableNumber;
    @FXML public TextField txtDiscountPercentage;
    @FXML public TextField txtDiscountId;
    @FXML public TextField txtDiscountDescription;
    @FXML public TextField txtDiscountTarget;
    @FXML public TextField txtProductName;
    @FXML public TextField txtProductDescription;
    @FXML public TextField txtProductPrice;
    @FXML public TextField txtProductId;
    @FXML public TextField txtProductStock;
    @FXML public TextField txtCategoryContainerName;

    @FXML private StackPane modalCategoryOverlay;
    @FXML public StackPane modalTableOverlay;
    @FXML public StackPane modalDiscountOverlay;
    @FXML public StackPane modalProductOverlay;

    @FXML private VBox categoriesContainer;
    @FXML public VBox productsContainer;
    @FXML public VBox tablesContainer;
    @FXML public VBox discountsContainer;

    @FXML private ToggleGroup discountTypeGroup;

    @FXML public ToggleButton tbAll;
    @FXML public ToggleButton tbCategory;
    @FXML public ToggleButton tbProduct;
    @FXML public ToggleButton tbMonday;
    @FXML public ToggleButton tbTuesday;
    @FXML public ToggleButton tbWednesday;
    @FXML public ToggleButton tbThursday;
    @FXML public ToggleButton tbFriday;
    @FXML public ToggleButton tbSaturday;
    @FXML public ToggleButton tbSunday;

    @FXML public VBox boxDiscountTarget;

    private Category editingCategory = null;
    private Product editingProduct = null;
    private Discount editingDiscount = null;

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
    public void handleUser() {
        btnLogout.setManaged(true);
        btnLogout.setVisible(true);
    }

    @FXML
    public void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AuthView.fxml"));
            Parent root = loader.load();

            AuthController authController = loader.getController();
            authController.setUserService(this.userService);
            authController.setCategoryService(this.categoryService);
            authController.setProductService(this.productService);
            authController.setTableService(this.tableService);
            authController.setDiscountService(this.discountService);

            Stage currentStage = (Stage) lblUserName.getScene().getWindow();
            Scene newScene = new Scene(root);
            newScene.getStylesheets().add(getClass().getResource("/styles/global.css").toExternalForm());

            currentStage.setTitle("Sazón - Sistema de Gestión de Restaurante");
            currentStage.setScene(newScene);
            currentStage.centerOnScreen();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo volver a la pantalla de inicio de sesión.");
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
    public void handleAddOrUpdateCategory() {
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

    public void renderProducts(Category category) {
        productsContainer.getChildren().clear();

        List<Product> products = category.getProducts();

        for(Product product: products) {
            VBox cardNode = createProductCardNode(category, product);
            productsContainer.getChildren().add(cardNode);
        }
    }

    public void handleOpenNewProductModal(String nameCategory) {
        this.editingProduct = null;
        if (lblProductModalTitle != null) lblProductModalTitle.setText("Nuevo Producto");
        txtCategoryContainerName.setText(nameCategory);
        txtCategoryContainerName.setDisable(true);
        txtProductName.clear();
        txtProductDescription.clear();
        txtProductPrice.clear();
        txtProductId.clear();
        txtProductStock.clear();
        modalProductOverlay.setVisible(true);
        modalProductOverlay.setManaged(true);
        txtProductName.requestFocus();
    }

    public void handleOpenEditProductModal(String nameCategory, Product product) {
        this.editingProduct = product;
        if (lblProductModalTitle != null) lblProductModalTitle.setText("Editar Producto");
        txtCategoryContainerName.setText(nameCategory);
        txtCategoryContainerName.setDisable(true);
        txtProductName.setText(product.getNameProduct());
        txtProductDescription.setText(product.getDescriptionProduct());
        txtProductPrice.setText(String.valueOf(product.getPriceProduct()));
        txtProductId.setText(String.valueOf(product.getIdProduct()));
        txtProductStock.setText(String.valueOf(product.getStockProduct()));
        modalProductOverlay.setVisible(true);
        modalProductOverlay.setManaged(true);
        txtProductName.requestFocus();
    }

    public void handleShowCategoryProducts(Category category) {
        lblContainerCategory.setText(category.getCategoryName());
        lblDescriptionContainerCategory.setText("Categoría / " + category.getCategoryName() +
                " · " + category.getProducts().size() + " producto(s) en esta categoría");
        showSection(viewProducts);
        renderProducts(category);
    }

    @FXML
    public void handleCloseProductModal() {
        modalProductOverlay.setManaged(false);
        modalProductOverlay.setVisible(false);
    }

    @FXML
    public void handleAddOrUpdateProduct() {
        String categoryName = txtCategoryContainerName.getText();
        String nameProduct = txtProductName.getText();
        String descriptionProduct = txtProductDescription.getText();
        String priceProduct = txtProductPrice.getText().trim();
        String idProduct = txtProductId.getText().trim();
        String stockProduct = txtProductStock.getText().trim();
        if(priceProduct.isEmpty() || idProduct.isEmpty() || stockProduct.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campos incompletos", "Por favor completa todos los campos.");
        }

        try {
            double price = Double.parseDouble(priceProduct);
            int id = Integer.parseInt(idProduct);
            int stock = Integer.parseInt(stockProduct);
            Product product = new Product(nameProduct, descriptionProduct, price, true, id, stock);

            if(editingProduct == null) {
                productService.addProduct(categoryName, product);
                showAlert(Alert.AlertType.INFORMATION, "Registro Exitoso",
                        "Id del producto registrado: " + id);
            }
            else {
                productService.updateProduct(id, product);
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Producto actualizado correctamente.");
            }

            Category containerCategory = categoryService.getCategoryByName(categoryName);
            handleShowCategoryProducts(containerCategory);
            renderProducts(containerCategory);
            handleCloseProductModal();

        } catch(CategoryNotFoundException | InvalidProductException | InvalidObjectException | InvalidTextException
                | ProductNotFoundException e) {
            showAlert(Alert.AlertType.WARNING, "Error en registro", e.getMessage());
        }

    }

    public void handleDeleteProduct(String nameCategory, Product product) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar eliminación");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("¿Estás seguro de que deseas eliminar el producto '" + product.getNameProduct() + "'" +
                " de la categoría " + nameCategory);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    productService.deleteProduct(product.getIdProduct());
                    showAlert(Alert.AlertType.INFORMATION, "Éxito", "Categoría eliminada.");

                    Category containerCategory = categoryService.getCategoryByName(nameCategory);
                    handleShowCategoryProducts(containerCategory);
                    renderProducts(containerCategory);

                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                }
            }
        });
    }

    public void handleChangeProductAvailability(Category category, Product product) {
        boolean isAvailable = product.isAvailabilityProduct();

        try {
            if(isAvailable) {
                productService.NoAvailableProduct(product.getIdProduct());
            }
            else {
                productService.availableProduct(product.getIdProduct());
            }

            renderProducts(category);

        } catch(ProductNotFoundException | InvalidProductException e) {
            showAlert(Alert.AlertType.WARNING, "Error en cambio de estado", e.getMessage());
        }
    }

    @FXML
    public void handleTables() {
        showSection(viewTables);
        renderTables();
    }

    @FXML
    public void handleOpenNewTableModal() {
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
        renderDiscounts();
    }

    public void renderDiscounts() {
        discountsContainer.getChildren().clear();

        List<Discount> discounts = discountService.getDiscounts();

        for(Discount discount: discounts) {
            VBox cardNode = createDiscountCardNode(discount);
            discountsContainer.getChildren().add(cardNode);
        }
    }

    @FXML
    public void handleOpenNewDiscountModal() {
        this.editingDiscount = null;
        lblDiscountModalTitle.setText("Nuevo descuento");
        txtDiscountId.clear();
        txtDiscountId.setDisable(false);
        txtDiscountDescription.clear();
        txtDiscountTarget.clear();
        txtDiscountPercentage.clear();
        modalDiscountOverlay.setManaged(true);
        modalDiscountOverlay.setVisible(true);
        modalDiscountOverlay.requestFocus();
    }

    @FXML
    public void handleOpenEditDiscountModal(Discount discount) {
        this.editingDiscount = discount;
        lblDiscountModalTitle.setText("Editar descuento");
        txtDiscountId.setText(String.valueOf(discount.getIdDiscount()));
        txtDiscountId.setDisable(true);
        txtDiscountDescription.setText(discount.getDescription());
        txtDiscountTarget.setText(discount.getTargetName());
        txtDiscountPercentage.setText(String.valueOf(discount.getPercentage()));
        modalDiscountOverlay.setManaged(true);
        modalDiscountOverlay.setVisible(true);
        txtDiscountDescription.requestFocus();
    }

    @FXML
    public void handleCloseDiscountModal() {
        modalDiscountOverlay.setManaged(false);
        modalDiscountOverlay.setVisible(false);
    }

    @FXML
    public void handleDiscountScopeChange() {
        Toggle selected = discountTypeGroup.getSelectedToggle();

        if(selected == tbCategory) {
            lblDiscountTarget.setText("Nombre de la categoría");
            txtDiscountTarget.setPromptText("Ej. Bebidas, Postres...");
            boxDiscountTarget.setManaged(true);
            boxDiscountTarget.setVisible(true);
        }
        else if(selected == tbProduct) {
            lblDiscountTarget.setText("Nombre del producto");
            txtDiscountTarget.setPromptText("Ej. Pizza Hawaiana");
            boxDiscountTarget.setManaged(true);
            boxDiscountTarget.setVisible(true);
        }
        else {
            boxDiscountTarget.setManaged(false);
            boxDiscountTarget.setVisible(false);
        }
    }

    @FXML
    public void handleAddOrUpdateDiscount() {
        String idDiscount = txtDiscountId.getText().trim();
        if(idDiscount.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campos incompletos", "Por favor completa todos los campos.");
        }
        String description = txtDiscountDescription.getText();
        DayOfWeek dayOfWeek = tbMonday.isSelected() ? DayOfWeek.MONDAY
                : tbTuesday.isSelected() ? DayOfWeek.TUESDAY
                : tbWednesday.isSelected() ? DayOfWeek.WEDNESDAY
                : tbThursday.isSelected() ? DayOfWeek.THURSDAY
                : tbFriday.isSelected() ? DayOfWeek.FRIDAY
                : tbSaturday.isSelected() ? DayOfWeek.SATURDAY
                : DayOfWeek.SUNDAY;
        DiscountScope scope = tbCategory.isSelected() ? DiscountScope.CATEGORIA
                : tbProduct.isSelected()  ? DiscountScope.PRODUCTO
                : DiscountScope.TODOS;
        String targetName = scope != DiscountScope.CATEGORIA && scope != DiscountScope.PRODUCTO ? null
                : txtDiscountTarget.getText();
        String percentageDiscount = txtDiscountPercentage.getText().trim();

        try {
            int id = Integer.parseInt(idDiscount);
            double percentage = Double.parseDouble(percentageDiscount);
            if(editingDiscount == null) {
                discountService.addDiscount(id, description, dayOfWeek, percentage, scope, targetName, true);
                showAlert(Alert.AlertType.INFORMATION, "Registro Exitoso",
                        "ID de descuento registrado: " + id);
            }
            else {
                Discount discount = new Discount(id,description,dayOfWeek,percentage,scope,targetName,true);
                discountService.updateDiscount(id, discount);
            }

            renderDiscounts();
            handleCloseDiscountModal();
        } catch (InvalidDiscountException | InvalidTextException | InvalidObjectException |
                CategoryNotFoundException | ProductNotFoundException | DiscountNotFoundException e) {
            showAlert(Alert.AlertType.WARNING, "Error en registro", e.getMessage());
        }
    }

    public void handleChangeDiscountActive(Discount discount) {
        boolean isActive = discount.isActive();

        try {
            if(isActive)
                discountService.deActivateDiscount(discount.getIdDiscount());
            else
                discountService.activateDiscount(discount.getIdDiscount());

            renderDiscounts();
        }catch (InvalidDiscountException | DiscountNotFoundException e) {
            showAlert(Alert.AlertType.WARNING, "Error en cambio de estado", e.getMessage());
        }
    }

    public void handleDeleteDiscount(Discount discount) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar eliminación");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("¿Estás seguro de que deseas eliminar el descuento '" + discount.getIdDiscount() + "'?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    discountService.deleteDiscount(discount.getIdDiscount());
                    showAlert(Alert.AlertType.INFORMATION, "Éxito", "Descuento eliminado.");
                    renderDiscounts();
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                }
            }
        });
    }

    @FXML
    public void handleInvoices() {
        showSection(viewInvoices);
    }

    private void showSection(VBox targetView) {
        hideView(viewCategories);
        hideView(viewTables);
        hideView(viewDiscounts);
        hideView(viewInvoices);
        hideView(viewProducts);

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

        card.setOnMouseClicked(event -> {
            handleShowCategoryProducts(category);
        });

        StackPane iconBox = createIconCategoryBox();
        VBox infoBox = createInfoCategoryBox(category);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnAddProduct = new Button("+  Agregar producto");
        btnAddProduct.getStyleClass().add("btn-action-add");

        btnAddProduct.setOnAction(event -> {
            event.consume();
            handleOpenNewProductModal(category.getCategoryName());
        });

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

        HBox actionsBox = new HBox(8.0, btnAddProduct, btnEdit, btnDelete);
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
                case EN_FACTURACION -> { // Ajusta el nombre según la constante exacta en tu Enum TableStatus
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

    private VBox createDiscountCardNode(Discount discount) {
        VBox card = new VBox(16.0);
        card.getStyleClass().add("discount-item-card");
        card.setMaxWidth(Double.MAX_VALUE);

        HBox headerBox = createDiscountHeaderBox(discount);
        HBox mainInfoBox = createDiscountMainInfoBox(discount);
        HBox detailsBox = createDiscountDetailsBox(discount);
        HBox actionsBox = createDiscountActionsBox(discount);

        card.getChildren().addAll(headerBox,mainInfoBox,detailsBox,actionsBox);
        return card;
    }

    private HBox createDiscountHeaderBox(Discount discount) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblId = new Label("#" + discount.getIdDiscount());
        lblId.getStyleClass().add("discount-badge-id");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        StackPane statusDot = new StackPane();
        statusDot.setMinSize(8, 8);
        statusDot.setMaxSize(8, 8);

        Label lblStatus = new Label();
        boolean isActive = discount.isActive();

        if (isActive) {
            statusDot.setStyle("-fx-background-color: #22c55e; -fx-background-radius: 50%;");
            lblStatus.setText("Activo");
            lblStatus.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 12px;");
        } else {
            statusDot.setStyle("-fx-background-color: #ef4444; -fx-background-radius: 50%;");
            lblStatus.setText("Inactivo");
            lblStatus.setStyle("-fx-text-fill: #8e929f; -fx-font-weight: bold; -fx-font-size: 12px;");
        }

        HBox statusBadge = new HBox(6.0, statusDot, lblStatus);
        statusBadge.setAlignment(Pos.CENTER);
        statusBadge.getStyleClass().add("discount-badge-status");

        header.getChildren().addAll(lblId, spacer, statusBadge);
        return header;
    }

    private HBox createDiscountMainInfoBox(Discount discount) {
        HBox mainBox = new HBox(16.0);
        mainBox.setAlignment(Pos.CENTER_LEFT);

        Label lblNumber = new Label(String.valueOf(discount.getPercentage()));
        lblNumber.getStyleClass().add("discount-percent-number");

        Label lblSymbol = new Label("%");
        lblSymbol.getStyleClass().add("discount-percent-symbol");

        HBox percentContent = new HBox(1.0, lblNumber, lblSymbol);
        percentContent.setAlignment(Pos.BASELINE_CENTER);

        StackPane percentBox = new StackPane(percentContent);
        percentBox.getStyleClass().add("discount-percent-box");

        Label lblDescription = new Label(discount.getDescription());
        lblDescription.getStyleClass().add("discount-title");

        mainBox.getChildren().addAll(percentBox, lblDescription);
        return mainBox;
    }

    private HBox createDiscountDetailsBox(Discount discount) {
        HBox detailsBox = new HBox();
        detailsBox.getStyleClass().add("discount-details-box");
        detailsBox.setAlignment(Pos.CENTER_LEFT);

        String dayText = translateDayOfWeek(discount.getDayWeek());
        String scopeText = formatScope(discount.getScope().name());
        String targetText = discount.getTargetName() == null ? "Todos"
                : discount.getTargetName();

        VBox colDay = createDetailColumn("Día", dayText);
        VBox colScope = createDetailColumn("Alcance", scopeText);
        VBox colTarget = createDetailColumn("Objetivo", targetText);

        HBox.setHgrow(colDay, Priority.ALWAYS);
        HBox.setHgrow(colScope, Priority.ALWAYS);
        HBox.setHgrow(colTarget, Priority.ALWAYS);

        detailsBox.getChildren().addAll(colDay, colScope, colTarget);
        return detailsBox;
    }

    private HBox createDiscountActionsBox(Discount discount) {
        HBox actionsBox = new HBox(10.0);
        actionsBox.setAlignment(Pos.CENTER);

        Button btnToggle;
        String toggleText;

        if(discount.isActive()) {
            toggleText = "⏻  Desactivar";
            btnToggle = new Button(toggleText);
            btnToggle.getStyleClass().add("btn-discount-action-deActive");
        }
        else {
            toggleText = "⏻  Activar";
            btnToggle = new Button(toggleText);
            btnToggle.getStyleClass().add("btn-discount-action-active");
        }

        btnToggle.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnToggle, Priority.ALWAYS);
        btnToggle.setOnAction(event -> {
            event.consume();
            handleChangeDiscountActive(discount);
        });

        Button btnEdit = new Button("✏  Editar");
        btnEdit.getStyleClass().add("btn-action-edit");
        btnEdit.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnEdit, Priority.ALWAYS);
        btnEdit.setOnAction(event -> {
            event.consume();
            handleOpenEditDiscountModal(discount);
        });

        Button btnDelete = new Button("🗑  Eliminar");
        btnDelete.getStyleClass().add("btn-action-delete");
        btnDelete.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnDelete, Priority.ALWAYS);
        btnDelete.setOnAction(event -> {
            event.consume();
            handleDeleteDiscount(discount);
        });

        actionsBox.getChildren().addAll(btnToggle, btnEdit, btnDelete);
        return actionsBox;
    }

    private String translateDayOfWeek(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "Lunes";
            case TUESDAY -> "Martes";
            case WEDNESDAY -> "Miércoles";
            case THURSDAY -> "Jueves";
            case FRIDAY -> "Viernes";
            case SATURDAY -> "Sábado";
            case SUNDAY -> "Domingo";
        };
    }

    private String formatScope(String scope) {
        return switch (scope) {
            case "CATEGORIA" -> "Categoría";
            case "PRODUCTO" -> "Producto";
            default -> "Todos";
        };
    }

    private VBox createDetailColumn(String labelText, String valueText) {
        VBox col = new VBox(4.0);
        col.setAlignment(Pos.CENTER_LEFT);

        Label lblHeader = new Label(labelText);
        lblHeader.getStyleClass().add("category-subtitle");

        Label lblValue = new Label(valueText);
        lblValue.getStyleClass().add("discount-detail-value");

        col.getChildren().addAll(lblHeader, lblValue);
        return col;
    }

    private VBox createProductCardNode(Category category, Product product) {
        VBox card = new VBox(14.0);
        card.getStyleClass().add("discount-item-card");
        card.setMaxWidth(Double.MAX_VALUE);

        HBox headerBox = createProductHeaderBox(product);
        VBox infoBox = createProductInfoBox(product);
        HBox priceStockBox = createProductPriceStockBox(product);
        VBox actionsBox = createProductActionsBox(category, product);

        card.getChildren().addAll(headerBox, infoBox, priceStockBox, actionsBox);
        return card;
    }

    private HBox createProductHeaderBox(Product product) {
        HBox header = new HBox(10.0);
        header.setAlignment(Pos.CENTER_LEFT);

        boolean isAvailable = product.isAvailabilityProduct();

        StackPane iconBox = new StackPane();
        iconBox.getStyleClass().add("product-icon-box");

        StackPane iconSvg = new StackPane();
        iconSvg.getStyleClass().add(isAvailable ? "icon-target-orange" : "icon-target-gray");
        iconBox.getChildren().add(iconSvg);

        Label lblId = new Label("#" + product.getIdProduct());
        lblId.getStyleClass().add("discount-badge-id");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        StackPane statusDot = new StackPane();
        statusDot.setMinSize(8, 8);
        statusDot.setMaxSize(8, 8);

        Label lblStatus = new Label();
        if (isAvailable) {
            statusDot.setStyle("-fx-background-color: #22c55e; -fx-background-radius: 50%;");
            lblStatus.setText("Disponible");
            lblStatus.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 12px;");
        } else {
            statusDot.setStyle("-fx-background-color: #8e929f; -fx-background-radius: 50%;");
            lblStatus.setText("No disponible");
            lblStatus.setStyle("-fx-text-fill: #d1d5db; -fx-font-weight: bold; -fx-font-size: 12px;");
        }

        HBox statusBadge = new HBox(6.0, statusDot, lblStatus);
        statusBadge.setAlignment(Pos.CENTER);
        statusBadge.getStyleClass().add("discount-badge-status");

        header.getChildren().addAll(iconBox, lblId, spacer, statusBadge);
        return header;
    }

    private VBox createProductInfoBox(Product product) {
        VBox infoBox = new VBox(6.0);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label lblName = new Label(product.getNameProduct());
        lblName.getStyleClass().add(product.isAvailabilityProduct() ? "product-title" : "product-title-muted");

        Label lblDescription = new Label(product.getDescriptionProduct());
        lblDescription.getStyleClass().add("product-description");
        lblDescription.setWrapText(true);

        infoBox.getChildren().addAll(lblName, lblDescription);
        return infoBox;
    }

    private HBox createProductPriceStockBox(Product product) {
        HBox detailsBox = new HBox();
        detailsBox.getStyleClass().add("discount-details-box");
        detailsBox.setAlignment(Pos.CENTER_LEFT);

        String formattedPrice = String.format("$%,.0f", product.getPriceProduct()).replace(",", ".");
        Label lblPrice = new Label(formattedPrice);
        lblPrice.getStyleClass().add(product.isAvailabilityProduct() ? "product-price" : "product-price-muted");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox stockCol = new VBox(2.0);
        stockCol.setAlignment(Pos.CENTER_RIGHT);

        Label lblStockHeader = new Label("Stock");
        lblStockHeader.getStyleClass().add("category-subtitle");

        int stock = product.getStockProduct();
        String stockText = stock + (stock == 1 ? " unidad" : " unidades");
        Label lblStockValue = new Label(stockText);
        lblStockValue.getStyleClass().add("discount-detail-value");

        stockCol.getChildren().addAll(lblStockHeader, lblStockValue);

        detailsBox.getChildren().addAll(lblPrice, spacer, stockCol);
        return detailsBox;
    }

    private VBox createProductActionsBox(Category category, Product product) {
        VBox actionsContainer = new VBox(10.0);

        boolean isAvailable = product.isAvailabilityProduct();

        StackPane eyeIcon = new StackPane();
        eyeIcon.getStyleClass().add(isAvailable ? "icon-eye-off" : "icon-eye-on");

        Button btnToggleAvailability = new Button(
                isAvailable ? "Marcar como no disponible" : "Marcar como disponible",
                eyeIcon
        );
        btnToggleAvailability.getStyleClass().add(
                isAvailable ? "btn-discount-action-deActive" : "btn-product-available-orange"
        );
        btnToggleAvailability.setMaxWidth(Double.MAX_VALUE);
        btnToggleAvailability.setOnAction(event -> {
            event.consume();
            handleChangeProductAvailability(category, product);
        });

        Button btnUpdate = new Button("✏  Actualizar");
        btnUpdate.getStyleClass().add("btn-discount-action-deActive");
        btnUpdate.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnUpdate, Priority.ALWAYS);
        btnUpdate.setOnAction(event -> {
            event.consume();
            handleOpenEditProductModal(category.getCategoryName(), product);
        });

        Button btnDelete = new Button("🗑  Eliminar");
        btnDelete.getStyleClass().add("btn-action-delete");
        btnDelete.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnDelete, Priority.ALWAYS);
        btnDelete.setOnAction(event -> {
            event.consume();
            handleDeleteProduct(category.getCategoryName(), product);
        });

        HBox bottomRow = new HBox(10.0, btnUpdate, btnDelete);
        bottomRow.setAlignment(Pos.CENTER);

        actionsContainer.getChildren().addAll(btnToggleAvailability, bottomRow);
        return actionsContainer;
    }
}
