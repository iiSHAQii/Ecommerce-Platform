package com.example.examplefeature;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "customer_id")
    private Long customerId;

    // REQUIRED by DB: We auto-generate this below
    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "order_status")
    private String orderStatus;

    @Column(name = "subtotal")
    private BigDecimal subtotal;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // --- AUTO-GENERATION LOGIC ---
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        // Generate a random unique order number if missing
        if (orderNumber == null) {
            this.orderNumber = "ORD-" + System.currentTimeMillis();
        }
        // If subtotal is missing, just copy totalAmount to satisfy DB
        if (subtotal == null && totalAmount != null) {
            this.subtotal = totalAmount;
        }
    }

    // --- GETTERS & SETTERS ---
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    // Helper for UI (Double -> BigDecimal)
    public void setTotalAmount(Double amount) {
        this.totalAmount = BigDecimal.valueOf(amount);
        this.subtotal = BigDecimal.valueOf(amount); // Keep subtotal in sync
    }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}