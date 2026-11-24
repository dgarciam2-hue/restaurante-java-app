package com.devtalles.proyecto.utils;

import com.devtalles.proyecto.model.*;
import com.devtalles.proyecto.service.OrderService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.util.Collections;
import java.util.List;

import static com.devtalles.proyecto.utils.ReusableCards.formatOrderTime;

public class OrderDetailModal {

    private OrderDetailModal() {
    }

    public static void show(Window owner, Order order, OrderService orderService) {
        Stage stage = new Stage();
        if (owner != null) {
            stage.initOwner(owner);
        }
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);

        VBox card = new VBox(16.0);
        card.getStyleClass().add("order-detail-card");
        card.setPrefWidth(460.0);
        card.setMaxWidth(460.0);

        VBox headerSection = buildHeaderSection(order, stage);

        VBox infoSection = buildInfoSection(order, orderService);

        VBox productsSection = buildProductsAndTotalsSection(order, orderService);

        Label lblFooterNote = new Label("Descuentos e impuestos se calculan al facturar.");
        lblFooterNote.getStyleClass().add("odm-footer-note");
        lblFooterNote.setWrapText(true);

        card.getChildren().addAll(headerSection, infoSection, productsSection, lblFooterNote);

        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color: transparent;");
        root.setPadding(new Insets(18.0));

        var cssUrl = OrderDetailModal.class.getResource("/styles/detail-modal.css");
        if (cssUrl != null) {
            root.getStylesheets().add(cssUrl.toExternalForm());
        }

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                stage.close();
            }
        });

        stage.setScene(scene);
        stage.showAndWait();
    }

    private static VBox buildHeaderSection(Order order, Stage stage) {
        Label lblTitle = new Label("Pedido #" + order.getOrderId());
        lblTitle.getStyleClass().add("odm-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblStatus = createStatusBadge(order.getOrderStatus());

        Button btnClose = new Button("✕");
        btnClose.getStyleClass().add("odm-close-btn");
        btnClose.setOnAction(e -> stage.close());

        HBox topRow = new HBox(10.0, lblTitle, spacer, lblStatus, btnClose);
        topRow.setAlignment(Pos.CENTER_LEFT);

        String orderTypeText = (order instanceof DeliveryOrder) ? "Domicilio" : "Mesa";
        Label lblSubtitle = new Label(orderTypeText + " · " + formatOrderTime(order));
        lblSubtitle.getStyleClass().add("odm-subtitle");

        return new VBox(4.0, topRow, lblSubtitle);
    }

    private static Label createStatusBadge(OrderStatus status) {
        String text = "• Abierto";
        String statusClass = "odm-status-abierto";

        if (status != null) {
            switch (status) {
                case ABIERTO -> {
                    text = "• Abierto";
                    statusClass = "odm-status-abierto";
                }
                case ENTREGADO -> {
                    text = "• Entregado";
                    statusClass = "odm-status-entregado";
                }
                case CERRADO -> {
                    text = "• Cerrado";
                    statusClass = "odm-status-cerrado";
                }
                case CANCELADO -> {
                    text = "• Cancelado";
                    statusClass = "odm-status-cancelado";
                }
            }
        }

        Label badge = new Label(text);
        badge.getStyleClass().addAll("odm-status-pill", statusClass);
        return badge;
    }

    private static VBox buildInfoSection(Order order, OrderService orderService) {
        VBox section = new VBox(10.0);

        GridPane grid = new GridPane();
        grid.setHgap(16.0);
        grid.setVgap(10.0);

        ColumnConstraints colLabel = new ColumnConstraints();
        colLabel.setMinWidth(135.0);
        colLabel.setPrefWidth(135.0);

        ColumnConstraints colValue = new ColumnConstraints();
        colValue.setHgrow(Priority.ALWAYS);
        colValue.setFillWidth(true);

        grid.getColumnConstraints().addAll(colLabel, colValue);

        if (order instanceof DeliveryOrder deliveryOrder) {
            Label lblSectionTitle = new Label("CLIENTE Y ENTREGA");
            lblSectionTitle.getStyleClass().add("odm-section-header");

            String customerName = deliveryOrder.getCustomer() != null
                    ? deliveryOrder.getCustomer().getCustomerName()
                    : "Sin cliente";
            String customerPhone = deliveryOrder.getCustomer() != null
                    ? formatPhone(deliveryOrder.getCustomer().getCustomerPhone())
                    : "—";
            String deliveryAddress = deliveryOrder.getDeliveryAddress() != null
                    ? deliveryOrder.getDeliveryAddress()
                    : "—";

            double deliveryCost = orderService.calculateAdditionalValue(deliveryOrder);
            String zoneText = "Zona " + deliveryOrder.getDeliveryZone() + " · " + formatCurrency(deliveryCost);

            addInfoRow(grid, 0, "Cliente", customerName);
            addInfoRow(grid, 1, "Teléfono", customerPhone);
            addInfoRow(grid, 2, "Dirección de entrega", deliveryAddress);
            addInfoRow(grid, 3, "Zona", zoneText);

            section.getChildren().addAll(lblSectionTitle, grid);

        } else if (order instanceof TableOrder tableOrder) {
            Label lblSectionTitle = new Label("MESA");
            lblSectionTitle.getStyleClass().add("odm-section-header");

            String tableText = tableOrder.getTable() != null
                    ? "Mesa " + tableOrder.getTable().getNumberTable()
                    : "Sin asignar";

            addInfoRow(grid, 0, "Número de mesa", tableText);

            section.getChildren().addAll(lblSectionTitle, grid);
        }

        return section;
    }

    private static void addInfoRow(GridPane grid, int rowIndex, String key, String value) {
        Label lblKey = new Label(key);
        lblKey.getStyleClass().add("odm-info-key");
        lblKey.setWrapText(true);

        Label lblValue = new Label(value);
        lblValue.getStyleClass().add("odm-info-value");
        lblValue.setWrapText(true);

        grid.add(lblKey, 0, rowIndex);
        grid.add(lblValue, 1, rowIndex);
    }

    private static String formatPhone(String phone) {
        if (phone == null) {
            return "—";
        }
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() == 10) {
            return digits.substring(0, 3) + " " + digits.substring(3, 6) + " " + digits.substring(6);
        }
        return phone;
    }

    private static String formatCurrency(double amount) {
        return String.format("$%,.0f", amount).replace(",", ".");
    }

    private static VBox buildProductsAndTotalsSection(Order order, OrderService orderService) {
        VBox section = new VBox(10.0);
        List<OrderDetail> details = order.getDetails() != null
                ? order.getDetails()
                : Collections.emptyList();

        Label lblSectionTitle = new Label("PRODUCTOS (" + details.size() + ")");
        lblSectionTitle.getStyleClass().add("odm-section-header");

        VBox itemsList = new VBox(0.0);

        for (OrderDetail detail : details) {
            if (detail == null || detail.getProduct() == null) {
                continue;
            }
            int amount = detail.getAmount();
            double lineTotal = detail.getProduct().getPriceProduct() * amount;

            HBox row = createLineRow(
                    amount + " × " + detail.getProduct().getNameProduct(),
                    formatCurrency(lineTotal),
                    "odm-item-name",
                    "odm-item-price"
            );
            row.getStyleClass().add("odm-row-bordered");
            itemsList.getChildren().add(row);
        }

        Node productsNode = itemsList;
        if (details.size() > 5) {
            ScrollPane scroll = new ScrollPane(itemsList);
            scroll.setFitToWidth(true);
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scroll.setPrefHeight(220.0);
            scroll.setMaxHeight(220.0);
            scroll.getStyleClass().add("odm-scroll");
            productsNode = scroll;
        }

        double subtotal = (orderService != null)
                ? orderService.calculateSubtotalOrder(order) // ajusta al nombre exacto de tu método
                : calculateSubtotalFallback(details);

        VBox totalsBox = new VBox(0.0);

        HBox subtotalRow = createLineRow(
                "Subtotal",
                formatCurrency(subtotal),
                "odm-subtotal-label",
                "odm-subtotal-value"
        );
        subtotalRow.getStyleClass().add("odm-row-bordered");
        totalsBox.getChildren().add(subtotalRow);

        if (order instanceof DeliveryOrder deliveryOrder) {
            int zone = deliveryOrder.getDeliveryZone();

            double deliveryCost = (orderService != null)
                    ? orderService.calculateAdditionalValue(deliveryOrder)
                    : deliveryOrder.calculateAdditionalValue();

            HBox deliveryRow = createLineRow(
                    "Valor del domicilio (zona " + zone + ")",
                    formatCurrency(deliveryCost),
                    "odm-item-name",
                    "odm-item-price"
            );
            deliveryRow.getStyleClass().add("odm-row-bordered");
            totalsBox.getChildren().add(deliveryRow);
        }

        section.getChildren().addAll(lblSectionTitle, productsNode, totalsBox);
        return section;
    }

    private static HBox createLineRow(String leftText, String rightText, String leftClass, String rightClass) {
        Label lblLeft = new Label(leftText);
        lblLeft.getStyleClass().add(leftClass);
        lblLeft.setWrapText(true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblRight = new Label(rightText);
        lblRight.getStyleClass().add(rightClass);
        lblRight.setMinWidth(Region.USE_PREF_SIZE);

        HBox row = new HBox(12.0, lblLeft, spacer, lblRight);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private static double calculateSubtotalFallback(List<OrderDetail> details) {
        double total = 0.0;
        for (OrderDetail detail : details) {
            if (detail != null && detail.getProduct() != null) {
                total += detail.getProduct().getPriceProduct() * detail.getAmount();
            }
        }
        return total;
    }
}
