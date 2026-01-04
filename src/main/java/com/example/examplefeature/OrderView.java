package com.example.examplefeature;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDateTime;

@Route(value = "orders", layout = MainLayout.class)
@PageTitle("Orders | Ecommerce ERP")
@PermitAll
public class OrderView extends VerticalLayout {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final Grid<Order> grid = new Grid<>(Order.class, false);
    private final Binder<Order> binder = new Binder<>(Order.class);
    private Order currentOrder;

    public OrderView(OrderRepository orderRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;

        setSizeFull();
        configureGrid();

        Button addBtn = new Button("New Order");
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openEditor(new Order()));

        add(addBtn, grid);
        refreshGrid();
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addColumn(Order::getOrderId).setHeader("ID").setWidth("80px").setFlexGrow(0);

        // Show Customer Name instead of ID
        grid.addColumn(order -> {
            return order.getCustomerId() != null ? "Customer #" + order.getCustomerId() : "Guest";
        }).setHeader("Customer");

        grid.addColumn(Order::getTotalAmount).setHeader("Amount ($)");
        grid.addColumn(Order::getOrderStatus).setHeader("Status");

        grid.addComponentColumn(order -> {
            Button edit = new Button("Edit");
            edit.addClickListener(e -> openEditor(order));
            return edit;
        });
    }

    private void refreshGrid() {
        grid.setItems(orderRepository.findAll());
    }

    private void openEditor(Order order) {
        this.currentOrder = order;
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(order.getOrderId() == null ? "Create Order" : "Edit Order");

        FormLayout formLayout = new FormLayout();

        // 1. Customer Selector (Simulated Foreign Key)
        // Since your Order entity uses 'Long customerId' and not 'Customer customer',
        // we use a ComboBox that lists Customers but saves the ID.
        ComboBox<Customer> customerSelect = new ComboBox<>("Customer");
        customerSelect.setItems(customerRepository.findAll());
        customerSelect.setItemLabelGenerator(c -> c.getFirstName() + " " + c.getLastName());

        // Manual binding for ID
        if (order.getCustomerId() != null) {
            customerRepository.findById(order.getCustomerId()).ifPresent(customerSelect::setValue);
        }

        // 2. Amount Field
        NumberField amount = new NumberField("Total Amount");

        // 3. Status Selector
        ComboBox<String> status = new ComboBox<>("Status");
        status.setItems("pending", "processing", "shipped", "delivered", "cancelled", "completed");

        // Bindings
        binder.forField(amount).bind(
                o -> o.getTotalAmount() != null ? o.getTotalAmount().doubleValue() : 0.0,
                (o, v) -> o.setTotalAmount(v)
        );
        binder.forField(status).bind(Order::getOrderStatus, Order::setOrderStatus);
        binder.setBean(currentOrder);

        formLayout.add(customerSelect, amount, status);

        Button save = new Button("Save", e -> {
            try {
                if (binder.validate().isOk()) {
                    if (customerSelect.getValue() != null) {
                        currentOrder.setCustomerId(customerSelect.getValue().getCustomerId());
                    } else {
                        Notification.show("Error: You must select a Customer");
                        return;
                    }

                    // Auto-generate required Order Number if missing
                    if (currentOrder.getOrderNumber() == null) {
                        currentOrder.setOrderNumber("ORD-" + System.currentTimeMillis());
                    }

                    orderRepository.save(currentOrder);
                    refreshGrid();
                    dialog.close();
                    Notification.show("Order Saved!");
                }
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Cancel", e -> dialog.close());

        // Only show delete for existing orders
        Button delete = new Button("Delete", e -> {
            orderRepository.delete(currentOrder);
            refreshGrid();
            dialog.close();
        });
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        delete.setVisible(currentOrder.getOrderId() != null);

        dialog.add(formLayout);
        dialog.getFooter().add(delete, cancel, save);
        dialog.open();
    }
}