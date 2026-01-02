package com.example.examplefeature;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | Ecommerce ERP")
@PermitAll
public class DashboardView extends VerticalLayout {

    private final OrderRepository orderRepository;

    public DashboardView(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;

        addClassName("dashboard-view");
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setSizeFull();

        add(getToolbar());

        TabSheet tabSheet = new TabSheet();
        tabSheet.setWidthFull();
        tabSheet.setHeightFull();

        // Tab 1: Executive Overview
        tabSheet.add("Executive Overview", createExecutiveDashboard());

        // Tab 2: The New "Whale Watching" Chart
        tabSheet.add("Top VIP Customers", createTopCustomersChart());

        // Tab 3: Operations (Order Status)
        tabSheet.add("Order Status", createOrderStatusChart());

        add(tabSheet);
    }

    private Component getToolbar() {
        H2 title = new H2("Executive Dashboard");
        title.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Margin.Bottom.MEDIUM);
        return title;
    }

    // --- TAB 1: KPIS (ALL REAL DATA NOW) ---
    private Component createExecutiveDashboard() {
        VerticalLayout layout = new VerticalLayout();

        // Fetch Real Data
        BigDecimal revenue = orderRepository.getTotalRevenue();
        long totalOrders = orderRepository.count();

        // Calculate Average Order Value (Handle division by zero)
        BigDecimal avgOrderValue = BigDecimal.ZERO;
        if (totalOrders > 0) {
            avgOrderValue = revenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
        }

        HorizontalLayout stats = new HorizontalLayout();
        stats.setWidthFull();
        stats.addClassNames(LumoUtility.Gap.LARGE);

        stats.add(createCard("Total Revenue", "$" + revenue.toString(), "green"));
        stats.add(createCard("Total Orders", String.valueOf(totalOrders), "blue"));
        stats.add(createCard("Avg Order Value", "$" + avgOrderValue.toString(), "purple")); // NOW REAL

        // Adding the Top Customers Chart here too for impact? No, let's keep it in its own tab for clarity.
        // We will leave the "Revenue Trend" here as a placeholder for now since we don't have date-based queries ready.
        layout.add(stats, createRevenueChartPlaceholder());
        return layout;
    }

    // --- TAB 2: TOP 5 CUSTOMERS (NEW & IMPRESSIVE) ---
    private Component createTopCustomersChart() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        Chart chart = new Chart(ChartType.BAR); // Horizontal Bar Chart
        Configuration conf = chart.getConfiguration();
        conf.setTitle("Top 5 Customers by Lifetime Spend");

        XAxis xAxis = new XAxis();
        YAxis yAxis = new YAxis();
        yAxis.setTitle(new AxisTitle("Total Spend ($)"));
        conf.addyAxis(yAxis);

        DataSeries series = new DataSeries();
        series.setName("Total Spend");

        // Fetch Real Data via Complex SQL Join
        List<Object[]> data = orderRepository.findTop5Customers();

        String[] categories = new String[data.size()];
        for (int i = 0; i < data.size(); i++) {
            Object[] row = data.get(i);
            String name = row[0].toString();
            Number spend = (Number) row[1];

            categories[i] = name; // Add name to Axis
            series.add(new DataSeriesItem(name, spend));
        }

        xAxis.setCategories(categories);
        conf.addxAxis(xAxis);
        conf.addSeries(series);

        layout.add(chart);
        return layout;
    }

    // --- TAB 3: ORDER STATUS (REAL) ---
    private Component createOrderStatusChart() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setAlignItems(Alignment.CENTER);

        Chart pieChart = new Chart(ChartType.PIE);
        Configuration conf = pieChart.getConfiguration();
        conf.setTitle("Order Status Breakdown");

        DataSeries series = new DataSeries();
        List<Object[]> data = orderRepository.countOrdersByStatus();
        for (Object[] row : data) {
            series.add(new DataSeriesItem(row[0].toString(), (Number) row[1]));
        }
        conf.setSeries(series);
        layout.add(pieChart);
        return layout;
    }

    // Helper: KPI Card
    private Component createCard(String title, String value, String color) {
        VerticalLayout card = new VerticalLayout();
        card.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.LARGE, LumoUtility.BorderRadius.LARGE);
        card.setSpacing(false);
        Span t = new Span(title);
        t.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL, LumoUtility.FontWeight.BOLD);
        Span v = new Span(value);
        v.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.FontWeight.EXTRABOLD);
        v.getStyle().set("color", "var(--lumo-" + color + "-text)");
        card.add(t, v);
        return card;
    }

    // Helper: Static Placeholder for visual balance
    private Component createRevenueChartPlaceholder() {
        Chart lineChart = new Chart(ChartType.SPLINE);
        Configuration conf = lineChart.getConfiguration();
        conf.setTitle("Projected Revenue Trend (Forecast)");
        XAxis xAxis = new XAxis();
        xAxis.setCategories("Jan", "Feb", "Mar", "Apr", "May");
        conf.addxAxis(xAxis);
        ListSeries series = new ListSeries("Projected", 1200, 3400, 2800, 4500, 6000);
        conf.addSeries(series);
        return lineChart;
    }
}