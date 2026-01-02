package com.example.examplefeature;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;

@Route("products")
public class ProductView extends VerticalLayout {

    private final ProductService service;
    private final Grid<Product> grid = new Grid<>(Product.class);
    private final TextField filterText = new TextField(); // The Search Bar

    public ProductView(ProductService service) {
        this.service = service;
        setSizeFull();
        configureGrid();
        configureFilter();

        add(filterText, grid); // Add search bar AND grid
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.setColumns("productName", "sku", "basePrice", "status");
        grid.getColumnByKey("productName").setHeader("Product Name");
    }

    private void configureFilter() {
        filterText.setPlaceholder("Filter by name or SKU...");
        filterText.setClearButtonVisible(true);
        // This makes it search instantly while you type!
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.setValueChangeTimeout(500);
        filterText.addValueChangeListener(e -> updateList());
    }

    private void updateList() {
        grid.setItems(service.findAllProducts(filterText.getValue()));
    }

    @PostConstruct
    public void fillGrid() {
        updateList();
    }
}