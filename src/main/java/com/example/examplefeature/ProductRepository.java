package com.example.examplefeature;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // This empty interface automatically gives you:
    // .findAll(), .save(), .delete(), .findById()

    // This is a custom JPQL Wildcard Query!
    // It says: "Find products where the name looks like the text I typed (ignoring case)"
    @Query("select p from Product p " +
            "where lower(p.productName) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(p.sku) like lower(concat('%', :searchTerm, '%'))")
    List<Product> search(@Param("searchTerm") String searchTerm);
}