
package com.bank.authservice.service;

import com.bank.authservice.dto.CustomerRequest;
import com.bank.authservice.entity.Customer;
import java.util.List;

public interface CustomerService {
    Customer createCustomer(CustomerRequest request);
    Customer updateCustomer(Long id, CustomerRequest request);
    Customer getCustomer(Long id);
    void deleteCustomer(Long id);
    List<Customer> searchByName(String keyword);
}
