package com.example.examplefeature;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 1. Total Revenue
    @Query(value = "SELECT COALESCE(SUM(total_amount), 0) FROM orders", nativeQuery = true)
    BigDecimal getTotalRevenue();

    // 2. Total Count
    long count();

    // 3. Real Order Status Data
    @Query(value = "SELECT order_status, COUNT(*) FROM orders GROUP BY order_status", nativeQuery = true)
    List<Object[]> countOrdersByStatus();

    // --- NEW: THE "WHALE" QUERY (Top 5 Customers by Spend) ---
    // Demonstrates: JOIN, SUM, GROUP BY, ORDER BY, LIMIT
    @Query(value = "SELECT c.first_name, SUM(o.total_amount) as total_spend " +
            "FROM orders o " +
            "JOIN customers c ON o.customer_id = c.customer_id " +
            "GROUP BY c.first_name " +
            "ORDER BY total_spend DESC " +
            "LIMIT 5", nativeQuery = true)
    List<Object[]> findTop5Customers();
}
    //Pie chart of every customer's order
//    @Query(value = "SELECT c.first_name, COUNT(o.order_id) " +
//            "FROM orders o " +
//            "JOIN customers c ON o.customer_id = c.customer_id " +
//            "GROUP BY c.first_name", nativeQuery = true)
//    List<Object[]> countOrdersByCustomer();
