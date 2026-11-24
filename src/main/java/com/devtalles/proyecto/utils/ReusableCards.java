package com.devtalles.proyecto.utils;

import com.devtalles.proyecto.model.*;
import com.devtalles.proyecto.service.OrderService;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class ReusableCards {

    public static VBox createAdminOrderCardNode(Order order, User waiter, OrderService orderService) {
        VBox card = createOrderCardNode(order, orderService);
        Label lblWaiter = new Label("Mesero: @" + waiter.getEmail());
        lblWaiter.getStyleClass().add("order-meta-text");
        card.getChildren().add(2, lblWaiter);

        boolean finalized = order.getOrderStatus() == OrderStatus.CERRADO || order.getOrderStatus() == OrderStatus.CANCELADO;
        if (finalized) {
            card.getStyleClass().add("order-card-finalized");
        }

        return card;
    }

    /**
     * @param onUpdate acción a ejecutar al pulsar "Actualizar"; recibe el pedido de la tarjeta.
     */
    public static VBox createWaiterCardNode(Order order, OrderService orderService, Consumer<Order> onUpdate) {
        VBox card = createOrderCardNode(order, orderService);

        boolean finalized = order.getOrderStatus() == OrderStatus.CERRADO || order.getOrderStatus() == OrderStatus.CANCELADO;
        if (finalized) {
            card.getStyleClass().add("order-card-finalized");
        }

        Button btnUpdate = new Button("✎ Actualizar");
        btnUpdate.getStyleClass().add("btn-order-update");
        btnUpdate.setMaxWidth(Double.MAX_VALUE);
        btnUpdate.setDisable(finalized);
        HBox.setHgrow(btnUpdate, Priority.ALWAYS);
        btnUpdate.setOnAction(e -> {
            if (onUpdate != null) onUpdate.accept(order);
        });

        Button btnInvoice = new Button("📄 Facturar");
        btnInvoice.getStyleClass().add("btn-order-invoice");
        btnInvoice.setMaxWidth(Double.MAX_VALUE);
        btnInvoice.setDisable(finalized);
        HBox.setHgrow(btnInvoice, Priority.ALWAYS);
        btnInvoice.setOnAction(e -> {
            if(onUpdate != null) onUpdate.accept(order);
        });

        HBox actionsRow = new HBox(10.0, btnUpdate, btnInvoice);

        card.getChildren().add(actionsRow);

        return card;
    }

    private static VBox createOrderCardNode(Order order, OrderService orderService) {
        VBox card = new VBox(8.0);
        card.getStyleClass().add("order-card");
        card.setPrefWidth(270.0);
        card.setMinWidth(270.0);
        card.setMaxWidth(270.0);
        card.setMinHeight(210.0);

        String iconSymbol = (order instanceof DeliveryOrder) ? "🛍" : "▦";
        Label lblIcon = new Label(iconSymbol);
        lblIcon.getStyleClass().add("order-icon-label");

        StackPane iconBox = new StackPane(lblIcon);
        iconBox.getStyleClass().add("order-icon-box");

        Label lblOrderId = new Label("#" + order.getOrderId());
        lblOrderId.getStyleClass().add("order-id-pill");

        Label lblStatus = createOrderStatusLabel(order);

        HBox topRow = new HBox(8.0);
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.getChildren().addAll(iconBox, lblOrderId);

        VBox bodyBox = new VBox(6.0);
        int totalItems = getOrderTotalItems(order);
        String itemsText = totalItems + (totalItems == 1 ? " producto" : " productos");
        String metaLine = "🕒 " + formatOrderTime(order) + "   " + itemsText;

        if (order instanceof TableOrder tableOrder) {
            Region topSpacer = new Region();
            HBox.setHgrow(topSpacer, Priority.ALWAYS);
            topRow.getChildren().addAll(topSpacer, lblStatus);

            int tableNumber = tableOrder.getTable() != null
                    ? tableOrder.getTable().getNumberTable()
                    : 0;

            Label lblTitle = new Label("Mesa " + tableNumber);
            lblTitle.getStyleClass().add("order-card-title");
            lblTitle.setWrapText(true);

            Label lblMeta = new Label(metaLine);
            lblMeta.getStyleClass().add("order-meta-text");

            bodyBox.getChildren().addAll(lblTitle, lblMeta);

        }
        else if (order instanceof DeliveryOrder deliveryOrder) {
            Label lblDeliveryBadge = new Label("Domicilio");
            lblDeliveryBadge.getStyleClass().add("order-type-pill");
            topRow.getChildren().add(lblDeliveryBadge);

            String customerName = deliveryOrder.getCustomer() != null
                    ? deliveryOrder.getCustomer().getCustomerName()
                    : "Cliente";
            String customerPhone = deliveryOrder.getCustomer() != null
                    ? deliveryOrder.getCustomer().getCustomerPhone()
                    : "";

            Label lblTitle = new Label(customerName);
            lblTitle.getStyleClass().add("order-card-title");
            lblTitle.setWrapText(true);

            Label lblContactMeta = new Label("📞 " + customerPhone + "   📍 Zona " + deliveryOrder.getDeliveryZone());
            lblContactMeta.getStyleClass().add("order-meta-text");

            Label lblTimeMeta = new Label(metaLine);
            lblTimeMeta.getStyleClass().add("order-meta-text");

            bodyBox.getChildren().addAll(lblStatus, lblTitle, lblContactMeta, lblTimeMeta);

        }
        else {
            Region topSpacer = new Region();
            HBox.setHgrow(topSpacer, Priority.ALWAYS);
            topRow.getChildren().addAll(topSpacer, lblStatus);

            Label lblTitle = new Label("Pedido #" + order.getOrderId());
            lblTitle.getStyleClass().add("order-card-title");

            Label lblMeta = new Label(metaLine);
            lblMeta.getStyleClass().add("order-meta-text");

            bodyBox.getChildren().addAll(lblTitle, lblMeta);
        }

        Hyperlink lnkDetail = new Hyperlink("Ver detalle ›");
        lnkDetail.getStyleClass().add("order-detail-link");
        lnkDetail.setOnAction(e -> {
            OrderDetailModal.show(null, order, orderService);
        });

        card.getChildren().addAll(topRow, bodyBox, lnkDetail);

        return card;
    }

    private static Label createOrderStatusLabel(Order order) {
        OrderStatus status = order.getOrderStatus();

        String text = "• Abierto";
        String statusStyleClass = "status-abierto";

        if (status != null) {
            switch (status) {
                case ABIERTO -> {
                    text = "• Abierto";
                    statusStyleClass = "status-abierto";
                }
                case ENTREGADO -> {
                    text = "• Entregado";
                    statusStyleClass = "status-entregado";
                }
                case CERRADO -> {
                    text = "• Cerrado";
                    statusStyleClass = "status-cerrado";
                }
                case CANCELADO -> {
                    text = "• Cancelado";
                    statusStyleClass = "status-cancelado";
                }
            }
        }

        Label lblStatus = new Label(text);
        lblStatus.getStyleClass().addAll("order-status-pill", statusStyleClass);
        return lblStatus;
    }

    private static int getOrderTotalItems(Order order) {
        if (order.getDetails() == null) {
            return 0;
        }
        return order.getDetails().stream()
                .mapToInt(OrderDetail::getAmount)
                .sum();
    }

    public static String formatOrderTime(Order order) {
        LocalDateTime dateTime = order.getDate();
        if (dateTime == null) {
            return "--:--";
        }
        String timeStr = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        if (dateTime.toLocalDate().equals(LocalDate.now())) {
            return "Hoy · " + timeStr;
        }
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM")) + " · " + timeStr;
    }
}
