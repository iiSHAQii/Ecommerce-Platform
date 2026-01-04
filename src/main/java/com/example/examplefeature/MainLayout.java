package com.example.examplefeature;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;

public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Ecommerce Resource Planning System");
        logo.addClassNames("text-l", "m-m");

        HorizontalLayout header = new HorizontalLayout(
                new DrawerToggle(),
                logo
        );

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidth("100%");
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);
    }

    private void createDrawer() {
        // These are the buttons in the sidebar
        RouterLink dashboardLink = new RouterLink("Dashboard", DashboardView.class);
        RouterLink ordersLink = new RouterLink("Order Management", OrderView.class);
        // Highlight logic happens automatically by Vaadin based on current URL

        VerticalLayout layout = new VerticalLayout(
                dashboardLink,
                ordersLink
        );

        addToDrawer(layout);
        addToDrawer(new VerticalLayout(
                new RouterLink("Dashboard", DashboardView.class),
                new RouterLink("Orders", OrderView.class),      // <--- NEW
                new RouterLink("Customers", CustomerView.class) // <--- NEW
        ));
    }
}