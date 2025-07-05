
package com.bank.authservice.service.impl;

import com.bank.authservice.dto.CustomerRequest;
import com.bank.authservice.entity.Customer;
import com.bank.authservice.repository.CustomerRepository;
import com.bank.authservice.service.CustomerService;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public Customer createCustomer(CustomerRequest request) {
        Customer customer = new Customer();
        customer.setFullName(request.getFullName());
        customer.setNationalId(request.getNationalId());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setAddress(request.getAddress());
        customer.setIsActive(true);
        customer.setCreatedAt(new Date());
        return customerRepository.save(customer);
    }

    @Override
    public Customer updateCustomer(UUID id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        customer.setFullName(request.getFullName());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());
        customer.setUpdatedAt(new Date());
        return customerRepository.save(customer);
    }

    @Override
    public Customer getCustomer(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    @Override
    public void deleteCustomer(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        customer.setIsActive(false);
        customer.setUpdatedAt(new Date());
        customerRepository.save(customer);
    }

    @Override
    public List<Customer> searchByName(String keyword) {
        return customerRepository.findByFullNameContainingIgnoreCase(keyword);
    }
}
