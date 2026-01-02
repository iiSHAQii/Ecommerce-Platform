package com.example.examplefeature;

import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

//@Component
public class
DataSeeder implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final Faker faker = new Faker();
    private final Random random = new Random();

    public DataSeeder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println(">>> STARTING GOD-MODE SEEDER (40 TABLES)...");

        // 1. NUKE EVERYTHING (Reverse dependency order to avoid FK errors)
        String[] tables = {
                "audit_logs", "notifications", "seller_payments", "search_history", "product_views",
                "coupon_usage", "coupons", "wishlist_items", "wishlists", "cart_items", "shopping_carts",
                "promotion_products", "promotions", "inventory_transactions", "inventory",
                "product_images", "product_attributes", "review_responses", "review_helpful_votes", "product_reviews",
                "return_items", "returns", "shipment_tracking", "shipments", "order_items", "orders",
                "payment_methods", "support_tickets", "support_agents", "seller_ratings", "sellers",
                "customer_addresses", "customers", "tax_rates", "shipping_zones", "shipping_methods",
                "products", "categories", "countries"
        };

        for (String table : tables) {
            jdbcTemplate.execute("TRUNCATE TABLE " + table + " CASCADE");
        }

        // ============================================
        // A. LOCATION & CONFIG
        // ============================================
        jdbcTemplate.update("INSERT INTO countries (country_id, country_code, country_name, currency_code) VALUES (1, 'USA', 'United States', 'USD')");
        jdbcTemplate.update("INSERT INTO countries (country_id, country_code, country_name, currency_code) VALUES (2, 'PAK', 'Pakistan', 'PKR')");

        jdbcTemplate.update("INSERT INTO shipping_zones (zone_name, country_id, postal_code_pattern) VALUES (?, ?, ?)", "East Coast", 1, "100%");
        jdbcTemplate.update("INSERT INTO tax_rates (country_id, state_province, tax_type, rate, effective_from) VALUES (1, 'NY', 'Sales', 0.08, NOW())");

        String[] methods = {"Standard Ground", "Express Air", "Overnight", "Drone Delivery"};
        for (int i = 0; i < methods.length; i++) {
            jdbcTemplate.update("INSERT INTO shipping_methods (method_id, method_name, base_cost, carrier) VALUES (?, ?, ?, ?)",
                    i + 1, methods[i], (i + 1) * 10.50, faker.company().name());
        }

        // ============================================
        // B. USERS
        // ============================================
        // Sellers
        for (int i = 1; i <= 20; i++) {
            jdbcTemplate.update("INSERT INTO sellers (seller_id, business_name, email, password_hash, phone, account_status) VALUES (?, ?, ?, ?, ?, ?)",
                    i, faker.company().name(), i + "_" + faker.internet().emailAddress(), "hash123", faker.phoneNumber().cellPhone(), "approved");
        }

        // Customers & Addresses & Payment Methods & Carts & Wishlists
        for (int i = 1; i <= 100; i++) {
            String email = i + "_" + faker.internet().emailAddress();
            jdbcTemplate.update("INSERT INTO customers (customer_id, email, password_hash, first_name, last_name, phone) VALUES (?, ?, ?, ?, ?, ?)",
                    i, email, "pass123", faker.name().firstName(), faker.name().lastName(), faker.phoneNumber().cellPhone());

            jdbcTemplate.update("INSERT INTO customer_addresses (customer_id, street_address, city, postal_code, country_id, address_type) VALUES (?, ?, ?, ?, ?, ?)",
                    i, faker.address().streetAddress(), faker.address().city(), faker.address().zipCode(), 1, "shipping");

            jdbcTemplate.update("INSERT INTO payment_methods (customer_id, method_type, card_last_four, card_brand) VALUES (?, 'credit_card', ?, ?)",
                    i, String.valueOf(random.nextInt(9000)+1000), faker.business().creditCardType());

            // Empty Cart for everyone
            jdbcTemplate.update("INSERT INTO shopping_carts (customer_id) VALUES (?)", i);

            // Wishlist
            jdbcTemplate.update("INSERT INTO wishlists (customer_id, wishlist_name) VALUES (?, ?)", i, "My Favorites");
        }

        // Support Agents
        for(int i = 1; i <= 5; i++) {
            jdbcTemplate.update("INSERT INTO support_agents (agent_id, email, password_hash, full_name, department) VALUES (?, ?, ?, ?, ?)",
                    i, i + "_agent@support.com", "pass", faker.name().fullName(), "Support");
        }

        // ============================================
        // C. CATALOG
        // ============================================
        String[] cats = {"Electronics", "Fashion", "Home", "Books", "Automotive"};
        for (int i = 0; i < cats.length; i++) {
            jdbcTemplate.update("INSERT INTO categories (category_id, category_name) VALUES (?, ?)", i + 1, cats[i]);
        }

        // Products, Images, Attributes, Inventory
        for (int i = 1; i <= 300; i++) {
            String name = (i % 10 == 0) ? "Premium Leather " + faker.commerce().productName() : faker.commerce().productName();
            String sku = (i % 20 == 0) ? "SKU-99-" + i : faker.code().ean13();

            jdbcTemplate.update("INSERT INTO products (product_id, seller_id, category_id, product_name, sku, base_price, status) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    i, random.nextInt(20) + 1, random.nextInt(5) + 1, name, sku, Double.parseDouble(faker.commerce().price()), "active");

            // Images
            jdbcTemplate.update("INSERT INTO product_images (product_id, image_url, is_primary) VALUES (?, ?, ?)", i, "http://img.com/" + sku + ".jpg", true);

            // Attributes
            jdbcTemplate.update("INSERT INTO product_attributes (product_id, attribute_name, attribute_value) VALUES (?, 'Color', ?)", i, faker.color().name());

            // Inventory
            jdbcTemplate.update("INSERT INTO inventory (product_id, quantity_available, warehouse_location) VALUES (?, ?, ?)",
                    i, random.nextInt(100), "Warehouse " + (char)('A' + random.nextInt(5)));
        }

        // ============================================
        // D. MARKETING
        // ============================================
        jdbcTemplate.update("INSERT INTO coupons (coupon_code, discount_value, start_date, end_date) VALUES ('WELCOME20', 20.00, NOW(), NOW() + INTERVAL '1 year')");
        jdbcTemplate.update("INSERT INTO promotions (promotion_name, discount_value, start_date, end_date) VALUES ('Summer Sale', 15.00, NOW(), NOW() + INTERVAL '1 month')");

        // ============================================
        // E. ORDERS & POST-ORDER
        // ============================================
        for (int i = 1; i <= 200; i++) {
            String orderNum = (i < 40) ? "ORD-" + faker.number().digits(5) : faker.business().creditCardNumber();
            String status = (i % 5 == 0) ? "pending" : "completed";
            int custId = random.nextInt(100) + 1;

            jdbcTemplate.update("INSERT INTO orders (order_id, customer_id, order_number, subtotal, total_amount, order_status) VALUES (?, ?, ?, ?, ?, ?)",
                    i, custId, orderNum, 100.00, 120.00, status);

            // Order Items
            jdbcTemplate.update("INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)",
                    i, random.nextInt(300) + 1, 1, 100.00, 100.00);

            // Shipments
            jdbcTemplate.update("INSERT INTO shipments (order_id, shipping_method_id, tracking_number, status, carrier) VALUES (?, ?, ?, ?, ?)",
                    i, random.nextInt(4) + 1, "TRK-" + faker.bothify("??####??"), "shipped", faker.company().name());

            // Returns (for some orders)
            if (i % 10 == 0) {
                jdbcTemplate.update("INSERT INTO returns (order_id, customer_id, return_number, reason, status) VALUES (?, ?, ?, ?, ?)",
                        i, custId, "RET-" + i, "Defective", "requested");
            }
        }

        // ============================================
        // F. SOCIAL & SYSTEM
        // ============================================
        for(int i=0; i<100; i++) {
            // Reviews
            jdbcTemplate.update("INSERT INTO product_reviews (product_id, customer_id, rating, review_text) VALUES (?, ?, ?, ?)",
                    random.nextInt(300) + 1, random.nextInt(100) + 1, random.nextInt(5) + 1, faker.lorem().sentence());

            // Support Tickets
            jdbcTemplate.update("INSERT INTO support_tickets (customer_id, agent_id, subject, description, status) VALUES (?, ?, ?, ?, ?)",
                    random.nextInt(100) + 1, random.nextInt(5) + 1, "Issue with " + faker.commerce().productName(), faker.lorem().paragraph(), "open");

            // Audit Logs
            jdbcTemplate.update("INSERT INTO audit_logs (action, table_name, record_id) VALUES (?, ?, ?)", "LOGIN", "customers", random.nextInt(100)+1);

            // Notifications
            jdbcTemplate.update("INSERT INTO notifications (customer_id, notification_type, title, message) VALUES (?, 'Order', 'Shipped', 'Your order is on the way')", random.nextInt(100)+1);

            // Search History
            jdbcTemplate.update("INSERT INTO search_history (customer_id, search_query) VALUES (?, ?)", random.nextInt(100)+1, faker.commerce().productName());
        }

        System.out.println(">>> SUCCESS: 40-TABLE ECOSYSTEM GENERATED.");
    }
}