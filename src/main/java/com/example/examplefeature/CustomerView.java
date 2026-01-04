package com.example.examplefeature;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "customers", layout = MainLayout.class)
@PageTitle("Customers | Ecommerce ERP")
@PermitAll
public class CustomerView extends VerticalLayout {

    private final CustomerRepository customerRepository;
    private final Grid<Customer> grid = new Grid<>(Customer.class, false);
    private final Binder<Customer> binder = new Binder<>(Customer.class);
    private final TextField filterText = new TextField(); // Search Bar
    private Customer currentCustomer;

    public CustomerView(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;

        setSizeFull();
        configureGrid();

        // --- TOOLBAR SETUP ---
        filterText.setPlaceholder("Search by ID or Name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.setValueChangeTimeout(400);
        // Triggers refreshGrid() when you type
        filterText.addValueChangeListener(e -> refreshGrid());

        Button addBtn = new Button("Add Customer");
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openEditor(new Customer()));

        HorizontalLayout toolbar = new HorizontalLayout(filterText, addBtn);
        toolbar.addClassName("toolbar");

        add(toolbar, grid);
        refreshGrid();
    }

    private void configureGrid() {
        grid.setSizeFull();
        // Added ID column so you can see what you are searching for
        grid.addColumn(Customer::getCustomerId).setHeader("ID").setWidth("80px").setFlexGrow(0);
        grid.addColumn(Customer::getFirstName).setHeader("First Name");
        grid.addColumn(Customer::getLastName).setHeader("Last Name");
        grid.addColumn(Customer::getEmail).setHeader("Email");
        grid.addColumn(Customer::getPhoneNumber).setHeader("Phone");

        // Add an "Edit" button column
        grid.addComponentColumn(customer -> {
            Button edit = new Button("Edit");
            edit.addClickListener(e -> openEditor(customer));
            return edit;
        });
    }

    private void refreshGrid() {
        List<Customer> allCustomers = customerRepository.findAll();
        String filter = filterText.getValue();

        if (filter == null || filter.isEmpty()) {
            grid.setItems(allCustomers);
        } else {
            String lowerCaseFilter = filter.toLowerCase();

            List<Customer> filteredCustomers = allCustomers.stream()
                    .filter(customer -> {
                        // 1. Strict ID Match (if input is a number)
                        boolean matchesId = String.valueOf(customer.getCustomerId()).equals(lowerCaseFilter);

                        // 2. Starts With Logic (Fixed: No more random middle-of-name matches)
                        boolean matchesFirst = customer.getFirstName().toLowerCase().startsWith(lowerCaseFilter);
                        boolean matchesLast = customer.getLastName().toLowerCase().startsWith(lowerCaseFilter);

                        // 3. Optional: Allow Email partial match? (Up to you, keeping it 'contains' is usually better for email)
                        boolean matchesEmail = customer.getEmail().toLowerCase().contains(lowerCaseFilter);

                        return matchesId || matchesFirst || matchesLast || matchesEmail;
                    })
                    .collect(Collectors.toList());

            grid.setItems(filteredCustomers);
        }
    }

    // The Dialog Form (unchanged logic)
    private void openEditor(Customer customer) {
        this.currentCustomer = customer;
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(customer.getCustomerId() == null ? "New Customer" : "Edit Customer");

        FormLayout formLayout = new FormLayout();
        TextField firstName = new TextField("First Name");
        TextField lastName = new TextField("Last Name");
        EmailField email = new EmailField("Email");
        TextField phone = new TextField("Phone");

        // Bind fields to the object
        binder.forField(firstName).bind(Customer::getFirstName, Customer::setFirstName);
        binder.forField(lastName).bind(Customer::getLastName, Customer::setLastName);
        binder.forField(email).bind(Customer::getEmail, Customer::setEmail);
        binder.forField(phone).bind(Customer::getPhoneNumber, Customer::setPhoneNumber);
        binder.setBean(currentCustomer);

        formLayout.add(firstName, lastName, email, phone);

        Button save = new Button("Save", e -> {
            try {
                if (binder.validate().isOk()) {

                    // Check if this is a NEW user (ID is null or 0)
                    boolean isNewUser = (currentCustomer.getCustomerId() == null || currentCustomer.getCustomerId() == 0L);

                    if (isNewUser) {
                        // FORCE SET defaults.
                        currentCustomer.setPasswordHash("temp_pass_123");
                        currentCustomer.setAccountStatus("active");
                        currentCustomer.setCreatedAt(LocalDateTime.now());
                    }

                    customerRepository.save(currentCustomer);
                    refreshGrid();
                    dialog.close();
                    Notification.show("Success: Customer Saved!");
                }
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
                ex.printStackTrace();
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Cancel", e -> dialog.close());
        Button delete = new Button("Delete", e -> {
            if (currentCustomer.getCustomerId() != null) {
                customerRepository.delete(currentCustomer);
                refreshGrid();
                dialog.close();
                Notification.show("Customer Deleted");
            }
        });
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);

        dialog.add(formLayout);
        dialog.getFooter().add(delete, cancel, save);
        dialog.open();
    }
}