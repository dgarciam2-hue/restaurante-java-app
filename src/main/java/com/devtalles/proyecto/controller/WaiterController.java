package com.devtalles.proyecto.controller;

import com.devtalles.proyecto.exceptions.InvalidCustomerException;
import com.devtalles.proyecto.exceptions.InvalidTextException;
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
import java.util.*;

public class WaiterController {
    @Setter private UserService userService;
    @Setter private CategoryService categoryService;
    @Setter private ProductService productService;
    @Setter private TableService tableService;
    @Setter private DiscountService discountService;
    @Setter private CustomerService customerService;

    @FXML private Label lblUserInitial;
    @FXML private Label lblUserName;
    @FXML private Label lblUserStatus;
    @FXML private Label lblOrderModalTitle;
    @FXML private Label lblPhoneFeedback;
    @FXML private Label lblCustomerName;
    @FXML private Label lblCustomerAddress;
    @FXML private Label lblOrderId;
    @FXML private Label lblOrderDynamicId;
    @FXML private Label quantityProducts;

    @FXML private ComboBox<Integer> cmbDeliveryZone;

    @FXML private TextField txtCustomerPhone;
    @FXML private TextField txtCustomerName;
    @FXML private TextField txtCustomerAddress;
    @FXML private TextField txtDeliveryAddress;

    @FXML private Button btnLogout;
    @FXML private Button btnTable;
    @FXML private Button btnDelivery;
    @FXML private Button btnSaveOrder;

    @FXML private FlowPane availableTablesContainer;
    @FXML private FlowPane availableProductsInCategory;

    @FXML private VBox orderCategoriesContainer;
    @FXML private VBox emptyOrderStateBox;
    @FXML private VBox containerForAddedProducts;

    @FXML private StackPane modalTypeOrderOverlay;
    @FXML private StackPane viewTableForm;
    @FXML private StackPane viewDeliveryForm;
    @FXML private StackPane viewSectionAvailableProducts;
    @FXML private StackPane viewSectionOrders;

    @FXML private ScrollPane spaceForAddedProducts;

    private Order editingOrder = null;

    private Table selectedTable = null;
    private Customer selectedCustomer = null;
    private String deliveryAddress = null;
    private int selectedZone = 1;
    private int defaultOrderId = 1;

    private final ToggleGroup tableSelectionGroup = new ToggleGroup();
    private final ToggleGroup categoryFilterGroup = new ToggleGroup();
    private Category selectedFilterCategory = null;
    private final List<OrderDetail> selectedOrderDetails = new ArrayList<>();

    public void setLoggedUser(User user) {
        if(user != null && user.getUserName() != null) {
            lblUserName.setText(user.getUserName());
            String initial = user.getUserName().substring(0,1).toUpperCase();
            lblUserInitial.setText(initial);
            String status = user.getRoleUser().name();
            lblUserStatus.setText(status);
        }
        setupPhoneRealtimeSearch();
        setupDeliveryZoneComboBox();
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
            authController.setCustomerService(this.customerService);

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
    public void handleOpenNewOrderModal() {
        this.editingOrder = null;
        if(lblOrderModalTitle != null) lblOrderModalTitle.setText("Nuevo pedido");
        modalTypeOrderOverlay.setManaged(true);
        modalTypeOrderOverlay.setVisible(true);
        renderAvailableTables();
    }

    @FXML
    public void handleCloseOrderModal() {
        modalTypeOrderOverlay.setVisible(false);
        modalTypeOrderOverlay.setManaged(false);
    }

    @FXML
    public void showOrderTableForm() {
        viewDeliveryForm.setVisible(false);
        viewDeliveryForm.setManaged(false);

        viewTableForm.setManaged(true);
        viewTableForm.setVisible(true);

        btnTable.getStyleClass().removeAll("tab-button-inactive");
        btnTable.getStyleClass().add("tab-button-active");

        btnDelivery.getStyleClass().removeAll("tab-button-active");
        btnDelivery.getStyleClass().add("tab-button-inactive");
    }

    public void renderAvailableTables() {
        availableTablesContainer.getChildren().clear();
        tableSelectionGroup.getToggles().clear();
        selectedTable = null;

        List<Table> allTables = tableService.getTables();

        for (Table table : allTables) {
            if (table.getTableStatus() == TableStatus.LIBRE) {
                ToggleButton btnTable = createTableSelectButton(table);
                availableTablesContainer.getChildren().add(btnTable);
            }
        }
    }

    @FXML
    public void showOrderDeliveryForm() {
        viewTableForm.setManaged(false);
        viewTableForm.setVisible(false);

        viewDeliveryForm.setManaged(true);
        viewDeliveryForm.setVisible(true);

        btnDelivery.getStyleClass().removeAll("tab-button-inactive");
        btnDelivery.getStyleClass().add("tab-button-active");

        btnTable.getStyleClass().removeAll("tab-button-active");
        btnTable.getStyleClass().add("tab-button-inactive");
    }

    private void setupPhoneRealtimeSearch() {
        txtCustomerPhone.textProperty().addListener((observable, oldValue, newValue) -> {

            if (!newValue.matches("\\d*")) {
                txtCustomerPhone.setText(newValue.replaceAll("[^\\d]", ""));
                return;
            }

            String phone = newValue.trim();

            if (phone.length()<10) {
                resetCustomerState();
                return;
            }

            verifyCustomerByPhone(phone);
        });
    }

    private void verifyCustomerByPhone(String phone) {
        Optional<Customer> customerOpt = customerService.findByPhone(phone);

        lblPhoneFeedback.setManaged(true);
        lblPhoneFeedback.setVisible(true);

        if (customerOpt.isPresent()) {
            this.selectedCustomer = customerOpt.get();

            setNewCustomerFieldsVisible(false);

            lblPhoneFeedback.setText("✓ Cliente encontrado: " + selectedCustomer.getCustomerName());
            lblPhoneFeedback.setStyle("-fx-background-color:#112a1d; -fx-text-fill: #6ee798; " +
                    "-fx-font-size: 12px; -fx-background-radius: 12px; -fx-padding: 10px; -fx-font-weight: bold;");
        } else {
            this.selectedCustomer = null;

            setNewCustomerFieldsVisible(true);

            lblPhoneFeedback.setText("✕ Cliente no registrado: Llenar la información");
            lblPhoneFeedback.setStyle("-fx-background-color:#2a1215; -fx-text-fill: #f87171; -fx-font-size: 12px; " +
                    "-fx-background-radius: 12px; -fx-padding: 10px; -fx-font-weight: bold;");
        }
    }

    private ToggleButton createTableSelectButton(Table table) {
        ToggleButton btn = new ToggleButton("Mesa " + table.getNumberTable());
        btn.getStyleClass().add("table-select-card");
        btn.setPrefWidth(100.0);
        btn.minWidth(100.0);
        btn.maxWidth(100.0);

        btn.setToggleGroup(tableSelectionGroup);

        btn.setOnAction(event -> {
            if (btn.isSelected()) {
                this.selectedTable = table;
            } else {
                this.selectedTable = null;
            }
        });

        return btn;
    }

    private void resetCustomerState() {
        this.selectedCustomer = null;
        lblPhoneFeedback.setText("");
        lblPhoneFeedback.setManaged(false);
        lblPhoneFeedback.setVisible(false);
        setNewCustomerFieldsVisible(false);
    }

    private void setNewCustomerFieldsVisible(boolean visible) {
        lblCustomerName.setManaged(visible);
        lblCustomerName.setVisible(visible);
        txtCustomerName.setManaged(visible);
        txtCustomerName.setVisible(visible);
        lblCustomerAddress.setManaged(visible);
        lblCustomerAddress.setVisible(visible);
        txtCustomerAddress.setManaged(visible);
        txtCustomerAddress.setVisible(visible);
    }

    private void setupDeliveryZoneComboBox() {
        // 1 = Norte, 2 = Sur, 3 = Este, 4 = Oeste
        cmbDeliveryZone.getItems().setAll(1, 2, 3, 4);

        cmbDeliveryZone.setConverter(new javafx.util.StringConverter<Integer>() {
            @Override
            public String toString(Integer zoneNumber) {
                if (zoneNumber == null) return "";

                String zoneName = switch (zoneNumber) {
                    case 1 -> "Norte";
                    case 2 -> "Sur";
                    case 3 -> "Este";
                    case 4 -> "Oeste";
                    default -> "Zona " + zoneNumber;
                };

                double additionalCost = zoneName.equalsIgnoreCase("Norte") ? 4000
                        : zoneName.equalsIgnoreCase("Sur") ? 6000
                        : zoneName.equalsIgnoreCase("Este") ? 8000
                        : 10000;
                String formattedCost = String.format("$%,.0f", additionalCost).replace(",", ".");

                return zoneName + " · " + formattedCost;
            }

            @Override
            public Integer fromString(String string) {
                return null;
            }
        });

        cmbDeliveryZone.getSelectionModel().selectFirst();
    }

    private boolean validateDeliveryForm() {
        clearDeliveryValidationStyles();

        String phone = txtCustomerPhone.getText() != null ? txtCustomerPhone.getText().trim() : "";
        String customerName = txtCustomerName.getText() != null ? txtCustomerName.getText().trim() : "";
        String customerAddress = txtCustomerAddress.getText() != null ? txtCustomerAddress.getText().trim() : "";
        String deliveryAddress = txtDeliveryAddress.getText() != null ? txtDeliveryAddress.getText().trim() : "";
        Integer selectedZone = cmbDeliveryZone.getValue();

        if (phone.length() < 10) {
            markFieldAsError(txtCustomerPhone, "Ingresa un número de teléfono válido de 10 dígitos.");
            return false;
        }

        if (this.selectedCustomer == null) {
            if (customerName.isEmpty()) {
                markFieldAsError(txtCustomerName, "El nombre del cliente es obligatorio para registrarlo.");
                return false;
            }
            if (customerAddress.isEmpty()) {
                markFieldAsError(txtCustomerAddress, "La dirección del cliente es obligatoria para registrarlo.");
                return false;
            }
        }

        if (deliveryAddress.isEmpty()) {
            markFieldAsError(txtDeliveryAddress, "La dirección de la entrega no puede estar vacía.");
            return false;
        }

        if (selectedZone == null) {
            cmbDeliveryZone.getStyleClass().add("input-error");
            showAlert(Alert.AlertType.WARNING, "Campo obligatorio", "Selecciona una zona de entrega.");
            return false;
        }

        return true;
    }

    private void markFieldAsError(TextField field, String message) {
        if (!field.getStyleClass().contains("input-error")) {
            field.getStyleClass().add("input-error");
        }
        field.requestFocus();
        showAlert(Alert.AlertType.WARNING, "Campos incompletos", message);
    }

    private void clearDeliveryValidationStyles() {
        txtCustomerPhone.getStyleClass().remove("input-error");
        txtCustomerName.getStyleClass().remove("input-error");
        txtCustomerAddress.getStyleClass().remove("input-error");
        txtDeliveryAddress.getStyleClass().remove("input-error");
        cmbDeliveryZone.getStyleClass().remove("input-error");
    }

    @FXML
    public void handleCreateAndAddProducts() {
        if (viewTableForm.isVisible()) {
            if (this.selectedTable == null) {
                showAlert(Alert.AlertType.WARNING, "Mesa requerida", "Por favor selecciona una mesa disponible antes de continuar.");
                return;
            }
            lblOrderId.setText("Pedido #" + defaultOrderId + " · " + "Mesa " + selectedTable.getNumberTable());
        }
        else if (viewDeliveryForm.isVisible()) {
            if (!validateDeliveryForm()) {
                return;
            }

            if (this.selectedCustomer == null) {
                try {
                    customerService.addCustomer(txtCustomerName.getText().trim(),
                            txtCustomerAddress.getText().trim(),
                            txtCustomerPhone.getText().trim());
                } catch (InvalidTextException | InvalidCustomerException e) {
                    showAlert(Alert.AlertType.ERROR, "Cliente no válido", e.getMessage());
                }
            }

            this.deliveryAddress = txtDeliveryAddress.getText().trim();
            this.selectedZone = cmbDeliveryZone.getValue();
            lblOrderId.setText("Pedido #" + defaultOrderId + " · " + "Cliente: " + selectedCustomer.getCustomerName());
        }
        modalTypeOrderOverlay.setVisible(false);
        modalTypeOrderOverlay.setManaged(false);

        viewSectionOrders.setManaged(false);
        viewSectionOrders.setVisible(false);

        viewSectionAvailableProducts.setVisible(true);
        viewSectionAvailableProducts.setManaged(true);

        lblOrderDynamicId.setText("Pedido #" + defaultOrderId);
        renderOrderCategories();
    }

    @FXML
    public void handleCloseAvailableProducts() {
        viewSectionAvailableProducts.setManaged(false);
        viewSectionAvailableProducts.setVisible(false);

        viewSectionOrders.setVisible(true);
        viewSectionOrders.setManaged(true);
    }

    public void renderOrderCategories() {
        orderCategoriesContainer.getChildren().clear();
        categoryFilterGroup.getToggles().clear();

        List<Category> categories = categoryService.getCategories();

        int totalProducts = categories.stream()
                .mapToInt(c -> Math.toIntExact(c.getProducts() != null && !c.getProducts().isEmpty()
                        ? c.getProducts().stream().filter(Product::isAvailabilityProduct).count() : 0))
                .sum();

        ToggleButton btnAll = createCategoryFilterCard("Todos", totalProducts, null);
        btnAll.setSelected(true);
        handleCategoryProducts(null);
        orderCategoriesContainer.getChildren().add(btnAll);

        for(Category category: categories) {
            if (category.getProducts() != null) {
                int count = Math.toIntExact(category.getProducts()
                        .stream()
                        .filter(Product::isAvailabilityProduct)
                        .count());
                ToggleButton btnCategory = createCategoryFilterCard(category.getCategoryName(), count, category);
                orderCategoriesContainer.getChildren().add(btnCategory);
            }
        }
    }

    private ToggleButton createCategoryFilterCard(String name, int count, Category category) {
        Label lblName = new Label(name);
        lblName.getStyleClass().add("cat-filter-name");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblCount = new Label(String.valueOf(count));
        lblCount.getStyleClass().add("cat-filter-count");

        HBox content = new HBox(10.0, lblName, spacer, lblCount);
        content.setAlignment(Pos.CENTER_LEFT);

        ToggleButton cardBtn = new ToggleButton();
        cardBtn.setGraphic(content);
        cardBtn.setMaxWidth(Double.MAX_VALUE);
        cardBtn.getStyleClass().add("cat-filter-card");
        cardBtn.setToggleGroup(categoryFilterGroup);

        cardBtn.setOnAction(event -> {
            if (!cardBtn.isSelected()) {
                cardBtn.setSelected(true);
                return;
            }
            this.selectedFilterCategory = category;
            handleCategoryProducts(category);
        });

        return cardBtn;
    }

    public void handleCategoryProducts(Category category) {
        availableProductsInCategory.getChildren().clear();

        List<Product> productsToShow = new ArrayList<>();

        if(category == null) {
            for (Category category1: categoryService.getCategories()) {
                if(!category1.getProducts().isEmpty()) {
                    productsToShow.addAll(category1.getProducts());
                }
            }

        }
        else {
            productsToShow.addAll(category.getProducts());
        }

        for (Product product: productsToShow) {
            if(product.isAvailabilityProduct()) {
                VBox productCard = createOrderProductCardNode(product);
                availableProductsInCategory.getChildren().add(productCard);
            }
        }
    }

    private VBox createOrderProductCardNode(Product product) {
        VBox card = new VBox(6.0);
        card.getStyleClass().add("order-product-card");
        card.setPrefWidth(210.0);
        card.setMinWidth(210.0);
        card.setMaxWidth(210.0);
        card.setMinHeight(150.0);

        Label lblBadge = new Label("✓  En el pedido");
        lblBadge.getStyleClass().add("in-order-badge");
        lblBadge.setVisible(false);

        Label lblName = new Label(product.getNameProduct());
        lblName.getStyleClass().add("order-product-title");
        lblName.setWrapText(true);

        Label lblDesc = new Label(product.getDescriptionProduct());
        lblDesc.getStyleClass().add("order-product-desc");
        lblDesc.setWrapText(true);

        Region vSpacer = new Region();
        VBox.setVgrow(vSpacer, Priority.ALWAYS);

        String formattedPrice = String.format("$%,.0f", product.getPriceProduct()).replace(",", ".");
        Label lblPrice = new Label(formattedPrice);
        lblPrice.getStyleClass().add("order-product-price");
        lblPrice.setMinWidth(Region.USE_PREF_SIZE);

        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);

        Button btnAdd = new Button("+  Agregar");
        btnAdd.getStyleClass().add("btn-add-product");
        btnAdd.setMinWidth(Region.USE_PREF_SIZE);

        HBox bottomRow = new HBox(10.0, lblPrice, hSpacer, btnAdd);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(lblBadge, lblName, lblDesc, vSpacer, bottomRow);

        boolean alreadyInOrder = selectedOrderDetails.stream()
                        .anyMatch(d -> d.getProduct().equals(product));
        updateCardVisualState(card, lblBadge, btnAdd, alreadyInOrder);

        btnAdd.setOnAction(e -> {
            OrderDetail orderDetail = new OrderDetail(product, 1);
            selectedOrderDetails.add(orderDetail);
            updateCardVisualState(card, lblBadge, btnAdd, true);

            renderOrderSummarySection();
        });

        return card;
    }

    private void updateCardVisualState(VBox card, Label lblBadge, Button btnAdd, boolean inOrder) {
        lblBadge.setVisible(inOrder);
        btnAdd.setDisable(inOrder);

        if (inOrder) {
            if (!card.getStyleClass().contains("order-product-card-selected")) {
                card.getStyleClass().add("order-product-card-selected");
            }
        } else {
            card.getStyleClass().remove("order-product-card-selected");
        }
    }

    public void renderOrderSummarySection() {
        if(selectedOrderDetails.isEmpty()) {
            spaceForAddedProducts.setManaged(false);
            spaceForAddedProducts.setVisible(false);
            emptyOrderStateBox.setManaged(true);
            emptyOrderStateBox.setVisible(true);
            btnSaveOrder.setDisable(true);
        }
        else {
            emptyOrderStateBox.setManaged(false);
            emptyOrderStateBox.setVisible(false);
            spaceForAddedProducts.setManaged(true);
            spaceForAddedProducts.setVisible(true);
            btnSaveOrder.setDisable(false);
        }

        containerForAddedProducts.getChildren().clear();
        containerForAddedProducts.setFillWidth(false);

        for (OrderDetail orderDetail: selectedOrderDetails) {
            containerForAddedProducts.getChildren().add(createOrderSummaryCardNode(orderDetail));
        }
    }

    private HBox createOrderSummaryCardNode(OrderDetail detail) {
        Product product = detail.getProduct();

        HBox card = new HBox(6.0);
        card.getStyleClass().add("order-summary-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(320.0);
        card.setMinWidth(320.0);
        card.setMaxWidth(320.0);
        card.setMinHeight(Region.USE_PREF_SIZE);

        Label lblName = new Label(product.getNameProduct());
        lblName.getStyleClass().add("order-summary-title");
        lblName.setWrapText(true);
        lblName.setPrefWidth(120.0);
        lblName.setMinWidth(120.0);
        lblName.setMaxWidth(120.0);
        lblName.setMinHeight(Region.USE_PREF_SIZE);

        Label lblUnitPrice = new Label( String.format("$%,.0f", product.getPriceProduct()).replace(",", ".") + " c/u");
        lblUnitPrice.getStyleClass().add("order-summary-unit-price");

        VBox infoBox = new VBox(2.0, lblName, lblUnitPrice);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setPrefWidth(120.0);
        infoBox.setMinWidth(120.0);
        infoBox.setMaxWidth(120.0);

        Button btnMinus = new Button("−");
        btnMinus.getStyleClass().add("btn-qty");

        Label lblQty = new Label(String.valueOf(detail.getAmount()));
        lblQty.getStyleClass().add("order-summary-qty");
        lblQty.setMinWidth(24.0);
        lblQty.setAlignment(Pos.CENTER);

        Button btnPlus = new Button("+");
        btnPlus.getStyleClass().add("btn-qty");

        HBox qtyBox = new HBox(8.0, btnMinus, lblQty, btnPlus);
        qtyBox.getStyleClass().add("qty-box");
        qtyBox.setAlignment(Pos.CENTER);
        qtyBox.setMinWidth(Region.USE_PREF_SIZE);

        Label lblSubtotal = new Label(String.format("$%,.0f", product.getPriceProduct() * detail.getAmount()).replace(",", "."));
        lblSubtotal.getStyleClass().add("order-summary-subtotal");
        lblSubtotal.setMinWidth(Region.USE_PREF_SIZE);

        Button btnDelete = new Button("🗑");
        btnDelete.getStyleClass().add("btn-delete-item");

        card.getChildren().addAll(infoBox, qtyBox, lblSubtotal, btnDelete);

        btnPlus.setOnAction(e -> {
            detail.setAmount(detail.getAmount() + 1);
            refreshSummaryCard(detail, lblQty, lblSubtotal);
        });

        btnMinus.setOnAction(e -> {
            if (detail.getAmount() > 1) {
                detail.setAmount(detail.getAmount() - 1);
                refreshSummaryCard(detail, lblQty, lblSubtotal);
            }
        });

        btnDelete.setOnAction(e -> {
            selectedOrderDetails.remove(detail);
            renderOrderSummarySection();
            handleCategoryProducts(null);
        });

        return card;
    }

    private void refreshSummaryCard(OrderDetail detail, Label lblQty, Label lblSubtotal) {
        lblQty.setText(String.valueOf(detail.getAmount()));
        lblSubtotal.setText(String.format("$%,.0f", detail.getProduct().getPriceProduct() * detail.getAmount()).replace(",", "."));
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
