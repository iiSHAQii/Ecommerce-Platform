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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "customers", layout = MainLayout.class)
@PageTitle("Customers | Ecommerce ERP")
@PermitAll
public class CustomerView extends VerticalLayout {

    private final CustomerRepository customerRepository;
    private final Grid<Customer> grid = new Grid<>(Customer.class, false);
    private final Binder<Customer> binder = new Binder<>(Customer.class);
    private Customer currentCustomer;

    public CustomerView(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;

        setSizeFull();
        configureGrid();

        Button addBtn = new Button("Add Customer");
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openEditor(new Customer()));

        add(addBtn, grid);
        refreshGrid();
    }

    private void configureGrid() {
        grid.setSizeFull();
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
        grid.setItems(customerRepository.findAll());
    }

    // The Dialog Form
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
            if (binder.validate().isOk()) {
                customerRepository.save(currentCustomer);
                refreshGrid();
                dialog.close();
                Notification.show("Customer Saved!");
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