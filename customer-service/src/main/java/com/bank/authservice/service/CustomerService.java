
package com.bank.authservice.service;

import com.bank.authservice.dto.CustomerRequest;
import com.bank.authservice.entity.Customer;
import java.util.List;
import java.util.UUID;

public interface CustomerService {
    Customer createCustomer(CustomerRequest request);
    Customer updateCustomer(UUID id, CustomerRequest request);
    Customer getCustomer(UUID id);
    void deleteCustomer(UUID id);
    List<Customer> searchByName(String keyword);
}
