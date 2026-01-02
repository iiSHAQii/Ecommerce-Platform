package com.example.examplefeature;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;

//@Route("orders")
@Route(value = "orders", layout = MainLayout.class)
public class OrderView extends VerticalLayout {

    private final OrderRepository orderRepository;
    private final Grid<Order> grid = new Grid<>(Order.class);
    private final TextField filterText = new TextField();

    public OrderView(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;

        setSizeFull();
        add(new H2("Order Management System"));

        configureGrid();
        configureFilter();

        add(filterText, grid);
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.setColumns("orderNumber", "totalAmount", "orderStatus");
        grid.getColumnByKey("orderNumber").setHeader("Order #");

        // Add "Edit" Button Column
        grid.addComponentColumn(order -> {
            Button editBtn = new Button("Update Status");
            editBtn.addClickListener(e -> openEditDialog(order));
            return editBtn;
        });

        // Add "Delete" Button Column
        grid.addComponentColumn(order -> {
            Button deleteBtn = new Button("Delete");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e -> {
                orderRepository.delete(order);
                updateList();
                Notification.show("Order Deleted", 3000, Notification.Position.BOTTOM_END);
            });
            return deleteBtn;
        });
    }

    private void openEditDialog(Order order) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Update Order: " + order.getOrderNumber());

        // Status Dropdown
        ComboBox<String> statusSelect = new ComboBox<>("New Status");
        statusSelect.setItems("pending", "processing", "shipped", "completed", "cancelled");
        statusSelect.setValue(order.getOrderStatus());

        // Save Button
        Button saveButton = new Button("Save", e -> {
            order.setOrderStatus(statusSelect.getValue());
            orderRepository.save(order);
            updateList();
            dialog.close();
            Notification.show("Status Updated!");
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.add(new VerticalLayout(statusSelect, saveButton));
        dialog.open();
    }

    private void configureFilter() {
        filterText.setPlaceholder("Filter by Order Number...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());
    }

    @PostConstruct
    private void updateList() {
        // Simple filter logic
        String filter = filterText.getValue();
        if (filter == null || filter.isEmpty()) {
            grid.setItems(orderRepository.findAll());
        } else {
            // Note: We are reusing the findAll, in a real app use a custom query
            grid.setItems(orderRepository.findAll().stream()
                    .filter(o -> o.getOrderNumber().toLowerCase().contains(filter.toLowerCase()))
                    .toList());
        }
    }
}