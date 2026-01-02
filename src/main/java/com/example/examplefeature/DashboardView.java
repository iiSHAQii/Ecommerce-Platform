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

        // 1. KPI Cards (Header)
        add(createRealKPICards());

        // 2. The Tab System (Now contains ALL 4 visuals)
        TabSheet tabSheet = new TabSheet();
        tabSheet.setWidthFull();
        tabSheet.setHeightFull();

        // TAB 1: Revenue Trend (The Blue Line - moved INSIDE here)
        tabSheet.add("Revenue Trend", createRealTrendChart());

        // TAB 2: Order Status
        tabSheet.add("Order Status", createPieChart(
                "Order Status Breakdown",
                orderRepository.countOrdersByStatus()
        ));

        // TAB 3: Top Customers
        tabSheet.add("Top VIP Customers", createTopCustomersChart());

        // TAB 4: Order Size
        tabSheet.add("Sales by Order Size", createPieChart(
                "Order Value Segmentation",
                orderRepository.countOrdersByValueCategory()
        ));

        add(tabSheet);
    }

    private Component getToolbar() {
        H2 title = new H2("Executive Analytics");
        title.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Margin.Bottom.MEDIUM);
        return title;
    }

    // --- COMPONENT 1: KPIS ---
    private Component createRealKPICards() {
        BigDecimal revenue = orderRepository.getTotalRevenue();
        long totalOrders = orderRepository.count();

        BigDecimal avgOrderValue = BigDecimal.ZERO;
        if (totalOrders > 0) {
            avgOrderValue = revenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
        }

        HorizontalLayout stats = new HorizontalLayout();
        stats.setWidthFull();
        stats.addClassNames(LumoUtility.Gap.LARGE, LumoUtility.Margin.Bottom.MEDIUM);

        stats.add(createCard("Total Revenue", "$" + revenue.toString(), "green"));
        stats.add(createCard("Total Orders", String.valueOf(totalOrders), "blue"));
        stats.add(createCard("Avg Order Value", "$" + avgOrderValue.toString(), "purple"));

        return stats;
    }

    // --- COMPONENT 2: TREND CHART (Tab 1 Content) ---
    private Component createRealTrendChart() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        // Center the chart in the tab
        layout.setAlignItems(Alignment.CENTER);

        Chart chart = new Chart(ChartType.SPLINE);
        Configuration conf = chart.getConfiguration();
        conf.setTitle("Revenue Trend (Daily)");

        XAxis xAxis = new XAxis();
        xAxis.setType(AxisType.CATEGORY);
        conf.addxAxis(xAxis);

        YAxis yAxis = new YAxis();
        yAxis.setTitle(new AxisTitle("Revenue ($)"));
        conf.addyAxis(yAxis);

        ListSeries series = new ListSeries("Daily Sales");

        List<Object[]> data = orderRepository.getRevenueTrend();
        String[] dates = new String[data.size()];
        for (int i = 0; i < data.size(); i++) {
            Object[] row = data.get(i);
            dates[i] = (row[0] != null) ? row[0].toString() : "N/A";
            series.addData((Number) row[1]);
        }

        xAxis.setCategories(dates);
        conf.addSeries(series);

        layout.add(chart);
        return layout;
    }

    // --- GENERIC PIE CHART BUILDER ---
    private Component createPieChart(String title, List<Object[]> data) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setAlignItems(Alignment.CENTER);

        Chart pieChart = new Chart(ChartType.PIE);
        Configuration conf = pieChart.getConfiguration();
        conf.setTitle(title);

        DataSeries series = new DataSeries();
        for (Object[] row : data) {
            String label = (row[0] != null) ? row[0].toString() : "Unknown";
            Number count = (Number) row[1];
            series.add(new DataSeriesItem(label, count));
        }

        conf.setSeries(series);
        layout.add(pieChart);
        return layout;
    }

    // --- TOP CUSTOMERS BAR CHART ---
    private Component createTopCustomersChart() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        Chart chart = new Chart(ChartType.BAR);
        Configuration conf = chart.getConfiguration();
        conf.setTitle("Top 5 Customers by Spend");

        XAxis xAxis = new XAxis();
        YAxis yAxis = new YAxis();
        yAxis.setTitle(new AxisTitle("Total Spend ($)"));
        conf.addyAxis(yAxis);

        DataSeries series = new DataSeries();
        series.setName("Lifetime Value");

        List<Object[]> data = orderRepository.findTop5Customers();
        String[] categories = new String[data.size()];

        for (int i = 0; i < data.size(); i++) {
            Object[] row = data.get(i);
            String name = row[0].toString();
            Number spend = (Number) row[1];
            categories[i] = name;
            series.add(new DataSeriesItem(name, spend));
        }

        xAxis.setCategories(categories);
        conf.addxAxis(xAxis);
        conf.addSeries(series);

        layout.add(chart);
        return layout;
    }

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
}