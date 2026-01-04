package com.example.examplefeature;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.Lumo;

public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Ecommerce ERP");
        logo.addClassNames("text-l", "m-m");

        // 1. Create the Toggle Button
        Button themeToggle = new Button(new Icon(VaadinIcon.MOON_O));
        themeToggle.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // 2. Add Logic to Switch Themes
        themeToggle.addClickListener(e -> {
            ThemeList themeList = UI.getCurrent().getElement().getThemeList();
            if (themeList.contains(Lumo.DARK)) {
                themeList.remove(Lumo.DARK);
                themeToggle.setIcon(new Icon(VaadinIcon.MOON_O)); // Hollow moon for Light mode
            } else {
                themeList.add(Lumo.DARK);
                themeToggle.setIcon(new Icon(VaadinIcon.MOON));   // Solid moon for Dark mode
            }
        });

        // 3. Add everything to the Header Layout
        // We add 'themeToggle' at the end.
        HorizontalLayout header = new HorizontalLayout(
                new DrawerToggle(),
                logo,
                themeToggle
        );

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidth("100%");
        header.addClassNames("py-0", "px-m");

        // This pushes the toggle button to the far right
        header.expand(logo);

        addToNavbar(header);
    }

    private void createDrawer() {
        // Links to your views
        RouterLink dashboardLink = new RouterLink("Dashboard", DashboardView.class);
        RouterLink ordersLink = new RouterLink("Orders", OrderView.class);
        RouterLink customersLink = new RouterLink("Customers", CustomerView.class);

        // Simple Vertical Layout for the drawer
        VerticalLayout drawerLayout = new VerticalLayout(
                dashboardLink,
                ordersLink,
                customersLink
        );

        addToDrawer(drawerLayout);
    }
}