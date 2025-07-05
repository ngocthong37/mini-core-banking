
package com.bank.authservice.repository;

import com.bank.authservice.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByNationalId(String nationalId);
    List<Customer> findByFullNameContainingIgnoreCase(String name);
}
