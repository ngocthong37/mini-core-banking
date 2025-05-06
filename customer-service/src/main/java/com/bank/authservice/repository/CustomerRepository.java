
package com.bank.authservice.repository;

import com.bank.authservice.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByNationalId(String nationalId);
    List<Customer> findByFullNameContainingIgnoreCase(String name);
}
