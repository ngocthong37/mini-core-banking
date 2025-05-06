package com.bank.accountservice.consumer;

import com.bank.accountservice.entity.Account;
import com.bank.accountservice.repository.AccountRepository;
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
    private final AccountRepository accountRepository;

    @KafkaListener(topics = "user-events", groupId = "account-service")
    public void handleUserCreatedEvent(String message) {
        try {
            UserCreatedEvent event = objectMapper.readValue(message, UserCreatedEvent.class);
            log.info("Received event from Kafka: {}", event);

            Account account = new Account();
            account.setBalance(0.0);
            account.setCreatedAt(event.getCreatedAt());
            account.setUserId(event.getId());
            account.setIsActive(true);
            account.setAccountNumber("AC" + System.currentTimeMillis());

            accountRepository.save(account);
            log.info("Created account for user {}", event.getUsername());
        } catch (Exception e) {
            log.error("Failed to process user-created event", e);
        }
    }
}
