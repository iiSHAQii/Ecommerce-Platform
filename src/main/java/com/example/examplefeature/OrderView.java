package com.example.examplefeature;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import org.springframework.data.domain.PageRequest;

@Route(value = "orders", layout = MainLayout.class)
@PageTitle("Orders | Ecommerce ERP")
@PermitAll
public class OrderView extends VerticalLayout {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final Grid<Order> grid = new Grid<>(Order.class, false);
    private final Binder<Order> binder = new Binder<>(Order.class);
    private final TextField filterText = new TextField();

    // LAZY LOADING PROVIDER
    private ConfigurableFilterDataProvider<Order, Void, String> dataProvider;
    private Order currentOrder;

    public OrderView(OrderRepository orderRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;

        setSizeFull();
        configureGrid();

        // --- LAZY DATA PROVIDER SETUP ---
        // This replaces the old "Fetch All" logic. It asks the DB for just one page at a time.
        DataProvider<Order, String> lazyDataProvider = DataProvider.fromFilteringCallbacks(
                // 1. QUERY: Fetch specific rows (e.g., rows 0-50)
                query -> {
                    PageRequest pageRequest = PageRequest.of(query.getPage(), query.getPageSize());
                    String filter = query.getFilter().orElse(null);

                    // Check if filter is empty or null
                    if (filter == null || filter.isEmpty()) {
                        return orderRepository.findAllWithPagination(pageRequest).stream();
                    } else {
                        // Passes the filter to the SQL query we added (matches ORD-XXXX, Name, etc.)
                        return orderRepository.searchWithPagination(filter, pageRequest).stream();
                    }
                },
                // 2. COUNT: Count total rows so scrollbar is sized correctly
                query -> {
                    String filter = query.getFilter().orElse(null);
                    if (filter == null || filter.isEmpty()) {
                        return (int) orderRepository.count();
                    } else {
                        return (int) orderRepository.searchWithPagination(filter, PageRequest.of(0, 1)).getTotalElements();
                    }
                }
        );

        // Wrap the provider so we can update the filter string easily
        this.dataProvider = lazyDataProvider.withConfigurableFilter();
        grid.setItems(dataProvider);

        // --- TOOLBAR ---
        filterText.setPlaceholder("Search (Order #, Name, Status)...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.setValueChangeTimeout(400); // 400ms delay to prevent spamming DB while typing

        // This triggers the Lazy DataProvider to reload with the new filter
        filterText.addValueChangeListener(e -> dataProvider.setFilter(e.getValue()));

        Button addBtn = new Button("New Order");
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openEditor(new Order()));

        HorizontalLayout toolbar = new HorizontalLayout(filterText, addBtn);
        toolbar.addClassName("toolbar");

        add(toolbar, grid);
        // Note: We do not call refreshGrid() here because setting the items above triggers the first load.
    }

    private void configureGrid() {
        grid.setSizeFull();

        // 1. REAL ORDER NUMBER (Direct from DB)
        grid.addColumn(Order::getOrderNumber)
                .setHeader("Order #")
                .setWidth("180px")
                .setFlexGrow(0);

        // 2. Customer Name
        grid.addColumn(order -> {
            if (order.getCustomer() != null) {
                return order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName();
            } else {
                return "ID: " + order.getCustomerId();
            }
        }).setHeader("Customer");

        // 3. Amount & Status
        grid.addColumn(Order::getTotalAmount).setHeader("Amount ($)");
        grid.addColumn(Order::getOrderStatus).setHeader("Status");

        // 4. Edit Button
        grid.addComponentColumn(order -> {
            Button edit = new Button("Edit");
            edit.addClickListener(e -> openEditor(order));
            return edit;
        });
    }

    // Helper to refresh the lazy grid after Save/Delete
    private void refreshGrid() {
        dataProvider.refreshAll();
    }

    private void openEditor(Order order) {
        this.currentOrder = order;
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(order.getOrderId() == null ? "Create Order" : "Edit Order");

        FormLayout formLayout = new FormLayout();

        // 1. Customer Selector
        ComboBox<Customer> customerSelect = new ComboBox<>("Customer");
        customerSelect.setItems(customerRepository.findAll());
        customerSelect.setItemLabelGenerator(c -> c.getFirstName() + " " + c.getLastName());

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

                    if (currentOrder.getOrderNumber() == null) {
                        currentOrder.setOrderNumber("ORD-" + System.currentTimeMillis());
                    }

                    orderRepository.save(currentOrder);
                    refreshGrid(); // Refreshes the Lazy DataProvider
                    dialog.close();
                    Notification.show("Order Saved!");
                }
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Cancel", e -> dialog.close());

        Button delete = new Button("Delete", e -> {
            orderRepository.delete(currentOrder);
            refreshGrid(); // Refreshes the Lazy DataProvider
            dialog.close();
        });
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        delete.setVisible(currentOrder.getOrderId() != null);

        dialog.add(formLayout);
        dialog.getFooter().add(delete, cancel, save);
        dialog.open();
    }
}