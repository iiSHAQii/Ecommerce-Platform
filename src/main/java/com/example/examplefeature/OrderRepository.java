package com.example.examplefeature;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // --- KPI 1: REVENUE ---
    @Query(value = "SELECT COALESCE(SUM(total_amount), 0) FROM orders", nativeQuery = true)
    BigDecimal getTotalRevenue();

    // --- KPI 2: COUNT (Standard) ---
    long count();

    // --- CHART 1: REVENUE TREND LINE (Real Time Series) ---
    // Groups orders by Date (YYYY-MM-DD) and sums revenue for that day.
    // Result: [Date, DailyTotal]
    @Query(value = "SELECT CAST(created_at AS DATE) as order_date, SUM(total_amount) " +
            "FROM orders " +
            "WHERE created_at IS NOT NULL " +
            "GROUP BY CAST(created_at AS DATE) " +
            "ORDER BY order_date ASC", nativeQuery = true)
    List<Object[]> getRevenueTrend();

    // --- CHART 2: ORDER STATUS PIE (Real Counts) ---
    // Result: [Status, Count]
    @Query(value = "SELECT order_status, COUNT(*) FROM orders GROUP BY order_status", nativeQuery = true)
    List<Object[]> countOrdersByStatus();

    // --- CHART 3: TOP 5 CUSTOMERS BAR (Real Whales) ---
    // Result: [Name, TotalSpend]
    @Query(value = "SELECT c.first_name, SUM(o.total_amount) as total_spend " +
            "FROM orders o " +
            "JOIN customers c ON o.customer_id = c.customer_id " +
            "GROUP BY c.first_name " +
            "ORDER BY total_spend DESC " +
            "LIMIT 5", nativeQuery = true)
    List<Object[]> findTop5Customers();

    // --- CHART 4: ORDER SIZE DISTRIBUTION (Real Segmentation) ---
    // Replaces the fake "Category" chart. Categorizes orders by their actual dollar amount.
    // Result: [CategoryName, Count]
    @Query(value = "SELECT " +
            "CASE " +
            "  WHEN total_amount < 100 THEN 'Small (<$100)' " +
            "  WHEN total_amount < 1000 THEN 'Medium ($100-$1k)' " +
            "  ELSE 'Large (>$1k)' " +
            "END as size_category, " +
            "COUNT(*) " +
            "FROM orders " +
            "GROUP BY size_category", nativeQuery = true)
    List<Object[]> countOrdersByValueCategory();
}