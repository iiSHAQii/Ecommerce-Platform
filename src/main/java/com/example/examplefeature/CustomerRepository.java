package com.example.examplefeature;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // We need this to search/filter in the UI if you want,
    // but standard findAll() is enough for the simple CRUD.
}