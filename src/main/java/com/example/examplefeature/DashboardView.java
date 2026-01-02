package com.example.examplefeature;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.charts.model.style.SolidColor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.PostConstruct;

//@Route("dashboard")

@Route(value = "", layout = MainLayout.class)
public class DashboardView extends VerticalLayout {

    private final OrderRepository orderRepository;

    public DashboardView(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
        addClassName(LumoUtility.Padding.LARGE);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        add(new H2("Executive Dashboard"));
    }

    @PostConstruct
    public void init() {
        Chart chart = new Chart(ChartType.PIE);
        Configuration conf = chart.getConfiguration();
        conf.setTitle("Order Status Breakdown");

        // Tooltip
        Tooltip tooltip = new Tooltip();
        tooltip.setPointFormat("<b>{point.name}</b>: {point.y} orders");
        conf.setTooltip(tooltip);

        // Data Series
        DataSeries series = new DataSeries();

        for (Object[] row : orderRepository.countOrdersByStatus()) {
            String status = (String) row[0];
            Long count = (Long) row[1];

            DataSeriesItem item = new DataSeriesItem(status, count);

            // COLOR LOGIC: Red for Pending, Green for everything else
            if ("pending".equalsIgnoreCase(status)) {
                item.setColor(new SolidColor("#ff4d4d")); // Red
                item.setSliced(true); // Pop it out slightly
            } else {
                item.setColor(new SolidColor("#2eb82e")); // Green
            }

            series.add(item);
        }

        conf.addSeries(series);
        add(chart);
    }
}