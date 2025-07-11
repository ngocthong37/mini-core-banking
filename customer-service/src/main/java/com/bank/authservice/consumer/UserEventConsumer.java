package com.bank.authservice.consumer;

import com.bank.authservice.entity.Customer;
import com.bank.authservice.repository.CustomerRepository;
import com.bank.common.event.UserCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer {

    private final ObjectMapper objectMapper;
    private final CustomerRepository customerRepository;

    @KafkaListener(topics = "user-registered", groupId = "customer-service")
    public void handleUserCreatedEvent(String message) {
        try {
            UserCreatedEvent event = objectMapper.readValue(message, UserCreatedEvent.class);
            log.info("Received event from Kafka: {}", event);
            Customer customer = new Customer();
            customer.setFullName(event.getUsername());
            customer.setEmail(event.getEmail());
            customer.setCreatedAt(event.getCreatedAt());
            customer.setUpdatedAt(event.getCreatedAt());

            customerRepository.save(customer);
            log.info("Created customer for user {}", event.getUsername());
        } catch (Exception e) {
            log.error("Failed to process user-created event", e);
        }
    }
}
