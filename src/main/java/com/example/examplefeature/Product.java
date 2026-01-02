package com.example.examplefeature;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    private String productName;
    private Double basePrice;
    private String sku;
    private String status;

    // Getters and Setters
    public Long getId() { return productId; }
    public void setId(Long id) { this.productId = id; }

    public String getProductName() { return productName; }
    public void setProductName(String name) { this.productName = name; }

    public Double getBasePrice() { return basePrice; }
    public void setBasePrice(Double price) { this.basePrice = price; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
